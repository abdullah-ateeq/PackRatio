package com.example.packratio.ui.screens.calculator

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.packratio.calculator.PackRatioCalculator
import com.example.packratio.model.PackRatioResult
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import com.example.packratio.ui.theme.PackStatusColors
import com.example.packratio.util.HapticUtil
import com.example.packratio.viewmodel.PackRatioViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: PackRatioViewModel
) {
    val state by viewModel.calculatorState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearCalculatorSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 700.dp

            if (isWideScreen) {
                // Two-column layout for tablets / wide landscape
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        CalculatorHeader()
                        Spacer(modifier = Modifier.height(16.dp))
                        PresetsRow(
                            onSelectPreset = { preset ->
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                when (preset) {
                                    1 -> viewModel.applyRetailPreset()
                                    2 -> viewModel.applyWeightPreset()
                                    3 -> viewModel.applyVolumePreset()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        QuantityTypeSelector(
                            selected = state.quantityType,
                            onSelect = {
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                viewModel.onQuantityTypeChanged(it)
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        InputsCard(
                            state = state,
                            showNotes = settings.showNotes,
                            onBulkChange = viewModel::onBulkQuantityChanged,
                            onPackageChange = viewModel::onPackageQuantityChanged,
                            onBulkUnitChange = viewModel::onBulkUnitChanged,
                            onPackageUnitChange = viewModel::onPackageUnitChanged,
                            onPackageNameChange = viewModel::onPackageNameChanged,
                            onNotesChange = viewModel::onNotesChanged
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        ResultsAndActions(
                            state = state,
                            settings = settings,
                            onToggleFormula = viewModel::toggleFormulaExpanded,
                            onCopyResult = {
                                state.result?.let { res ->
                                    clipboardManager.setText(
                                        AnnotatedString(
                                            PackRatioCalculator.formatCopySummary(res, state.packageName)
                                        )
                                    )
                                    HapticUtil.performLight(haptic, settings.hapticFeedback)
                                    viewModel.onCopiedResult()
                                }
                            },
                            onCopyCapacity = {
                                state.result?.let { res ->
                                    val countText = "${PackRatioCalculator.formatWholeNumber(res.completePackages)} packages"
                                    clipboardManager.setText(AnnotatedString(countText))
                                    HapticUtil.performLight(haptic, settings.hapticFeedback)
                                    viewModel.onCopiedCapacity()
                                }
                            },
                            onShare = {
                                state.result?.let { res ->
                                    val text = PackRatioCalculator.formatShareSummary(res, state.packageName)
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, text)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share PackRatio Calculation")
                                    context.startActivity(shareIntent)
                                }
                            },
                            onSave = {
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                viewModel.saveCurrentCalculation()
                            },
                            onReset = {
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                viewModel.resetCalculator()
                            }
                        )
                    }
                }
            } else {
                // Single-column layout for portrait phones
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    CalculatorHeader()
                    Spacer(modifier = Modifier.height(14.dp))
                    PresetsRow(
                        onSelectPreset = { preset ->
                            HapticUtil.performLight(haptic, settings.hapticFeedback)
                            when (preset) {
                                1 -> viewModel.applyRetailPreset()
                                2 -> viewModel.applyWeightPreset()
                                3 -> viewModel.applyVolumePreset()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    QuantityTypeSelector(
                        selected = state.quantityType,
                        onSelect = {
                            HapticUtil.performLight(haptic, settings.hapticFeedback)
                            viewModel.onQuantityTypeChanged(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    InputsCard(
                        state = state,
                        showNotes = settings.showNotes,
                        onBulkChange = viewModel::onBulkQuantityChanged,
                        onPackageChange = viewModel::onPackageQuantityChanged,
                        onBulkUnitChange = viewModel::onBulkUnitChanged,
                        onPackageUnitChange = viewModel::onPackageUnitChanged,
                        onPackageNameChange = viewModel::onPackageNameChanged,
                        onNotesChange = viewModel::onNotesChanged
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ResultsAndActions(
                        state = state,
                        settings = settings,
                        onToggleFormula = viewModel::toggleFormulaExpanded,
                        onCopyResult = {
                            state.result?.let { res ->
                                clipboardManager.setText(
                                    AnnotatedString(
                                        PackRatioCalculator.formatCopySummary(res, state.packageName)
                                    )
                                )
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                viewModel.onCopiedResult()
                            }
                        },
                        onCopyCapacity = {
                            state.result?.let { res ->
                                val countText = "${PackRatioCalculator.formatWholeNumber(res.completePackages)} packages"
                                clipboardManager.setText(AnnotatedString(countText))
                                HapticUtil.performLight(haptic, settings.hapticFeedback)
                                viewModel.onCopiedCapacity()
                            }
                        },
                        onShare = {
                            state.result?.let { res ->
                                val text = PackRatioCalculator.formatShareSummary(res, state.packageName)
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share PackRatio Calculation")
                                context.startActivity(shareIntent)
                            }
                        },
                        onSave = {
                            HapticUtil.performLight(haptic, settings.hapticFeedback)
                            viewModel.saveCurrentCalculation()
                        },
                        onReset = {
                            HapticUtil.performLight(haptic, settings.hapticFeedback)
                            viewModel.resetCalculator()
                        }
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun CalculatorHeader() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "PACKRATIO",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Calculate complete packages from bulk quantity.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PresetsRow(onSelectPreset: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = false,
            onClick = { onSelectPreset(1) },
            label = { Text("Retail Pieces") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        FilterChip(
            selected = false,
            onClick = { onSelectPreset(2) },
            label = { Text("Weight (kg→g)") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
        FilterChip(
            selected = false,
            onClick = { onSelectPreset(3) },
            label = { Text("Volume (L→ml)") },
            colors = FilterChipDefaults.filterChipColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityTypeSelector(
    selected: QuantityType,
    onSelect: (QuantityType) -> Unit
) {
    val types = QuantityType.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = types.size),
                onClick = { onSelect(type) },
                selected = selected == type,
                label = { Text(type.displayName) }
            )
        }
    }
}

@Composable
fun InputsCard(
    state: com.example.packratio.viewmodel.CalculatorUiState,
    showNotes: Boolean,
    onBulkChange: (String) -> Unit,
    onPackageChange: (String) -> Unit,
    onBulkUnitChange: (QuantityUnit) -> Unit,
    onPackageUnitChange: (QuantityUnit) -> Unit,
    onPackageNameChange: (String) -> Unit,
    onNotesChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Bulk Quantity Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.bulkQuantityInput,
                    onValueChange = onBulkChange,
                    label = { Text("BULK QUANTITY") },
                    placeholder = { Text("1000") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (state.quantityType == QuantityType.ITEMS) KeyboardType.Number else KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Bulk quantity ${state.bulkQuantityInput} ${state.bulkUnit.symbol}"
                        }
                )

                UnitDropdown(
                    selectedUnit = state.bulkUnit,
                    quantityType = state.quantityType,
                    onUnitSelected = onBulkUnitChange
                )
            }

            // Package Quantity Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.packageQuantityInput,
                    onValueChange = onPackageChange,
                    label = { Text("QUANTITY PER PACKAGE") },
                    placeholder = { Text("25") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = if (state.quantityType == QuantityType.ITEMS) KeyboardType.Number else KeyboardType.Decimal,
                        imeAction = if (showNotes) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .semantics {
                            contentDescription = "Package size ${state.packageQuantityInput} ${state.packageUnit.symbol}"
                        }
                )

                UnitDropdown(
                    selectedUnit = state.packageUnit,
                    quantityType = state.quantityType,
                    onUnitSelected = onPackageUnitChange
                )
            }

            // Validation Error if any
            if (state.validationError != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.errorContainer,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Validation error",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = state.validationError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            HorizontalDivider()

            // Package Name (Optional)
            OutlinedTextField(
                value = state.packageName,
                onValueChange = onPackageNameChange,
                label = { Text("PACKAGE NAME (OPTIONAL)") },
                placeholder = { Text("e.g. Retail Pack, Sample Bag") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Notes (Optional, if enabled in settings)
            if (showNotes) {
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = onNotesChange,
                    label = { Text("NOTES (OPTIONAL)") },
                    placeholder = { Text("e.g. Prepare for delivery...") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun UnitDropdown(
    selectedUnit: QuantityUnit,
    quantityType: QuantityType,
    onUnitSelected: (QuantityUnit) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableUnits = QuantityUnit.unitsForType(quantityType)

    Box {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .height(56.dp)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp),
            tonalElevation = 2.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedUnit.symbol,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select unit"
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableUnits.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "${unit.label} (${unit.symbol})",
                            fontWeight = if (unit == selectedUnit) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onUnitSelected(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun ResultsAndActions(
    state: com.example.packratio.viewmodel.CalculatorUiState,
    settings: com.example.packratio.model.AppSettings,
    onToggleFormula: () -> Unit,
    onCopyResult: () -> Unit,
    onCopyCapacity: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    val result = state.result

    if (result != null) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            // Primary Result Card
            PrimaryResultCard(result = result)

            // Secondary Metrics
            SecondaryMetricsCard(result = result, settings = settings)

            // Visual Package Representation
            if (settings.showPackageVisualization) {
                PackageVisualizationCard(result = result)
            }

            // Dynamic Formula Section
            if (settings.showFormula) {
                FormulaCard(
                    isExpanded = state.isFormulaExpanded,
                    formulaText = result.formulaExplanation,
                    onToggle = onToggleFormula
                )
            }

            // Action Buttons
            ActionButtonsRow(
                onCopyResult = onCopyResult,
                onCopyCapacity = onCopyCapacity,
                onShare = onShare,
                onSave = onSave,
                onReset = onReset
            )
        }
    } else {
        // Empty / prompt state
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Enter bulk and package quantities to calculate.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PrimaryResultCard(result: PackRatioResult) {
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val statusColors = PackStatusColors.forStatus(result.status, isDark)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "${PackRatioCalculator.formatWholeNumber(result.completePackages)} complete packages, ${PackRatioCalculator.formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol} leftover"
            },
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMPLETE PACKAGES",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColors.container
                ) {
                    Text(
                        text = result.status.label,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = statusColors.onContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visually Dominant Package Count
            Text(
                text = PackRatioCalculator.formatWholeNumber(result.completePackages),
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    lineHeight = 52.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Pack size & Leftover Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${PackRatioCalculator.formatQuantity(result.packageQuantityInput)} ${result.packageUnit.symbol} each",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "${PackRatioCalculator.formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol} leftover",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (result.leftoverQuantity.compareTo(BigDecimal.ZERO) == 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        statusColors.text
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic explanation text
            Text(
                text = result.statusExplanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SecondaryMetricsCard(
    result: PackRatioResult,
    settings: com.example.packratio.model.AppSettings
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "BULK",
                    value = "${PackRatioCalculator.formatQuantity(result.bulkQuantityInput)} ${result.bulkUnit.symbol}"
                )
                MetricItem(
                    label = "PACKAGED",
                    value = "${PackRatioCalculator.formatQuantity(result.packagedQuantity)} ${result.packageUnit.symbol}"
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "LEFTOVER",
                    value = "${PackRatioCalculator.formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol}"
                )

                if (settings.showUtilization) {
                    MetricItem(
                        label = "BULK UTILIZATION",
                        value = PackRatioCalculator.formatPercent(result.utilizationPercent)
                    )
                }

                if (settings.showLeftoverPercentage) {
                    MetricItem(
                        label = "LEFTOVER %",
                        value = PackRatioCalculator.formatPercent(result.leftoverPercent)
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun PackageVisualizationCard(result: PackRatioResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PACKAGE DISTRIBUTION",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Proportional horizontal bar
            val utilizationFloat = result.utilizationPercent.toFloat() / 100f
            val leftoverFloat = (1f - utilizationFloat).coerceAtLeast(0f)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (utilizationFloat > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(utilizationFloat.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (utilizationFloat >= 0.2f) {
                            Text(
                                text = "Packaged ${PackRatioCalculator.formatPercent(result.utilizationPercent)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                maxLines = 1
                            )
                        }
                    }
                }

                if (leftoverFloat > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(leftoverFloat.coerceAtLeast(0.01f))
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftoverFloat >= 0.2f) {
                            Text(
                                text = "Leftover",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Proportional package blocks visualization
            if (result.completePackages in 1..20) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(result.completePackages.toInt()) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                        )
                    }
                    if (result.leftoverQuantity.compareTo(BigDecimal.ZERO) > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                        )
                    }
                }
            } else if (result.completePackages > 20) {
                // Compact proportional representation for large quantities (safe for >100,000)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${PackRatioCalculator.formatWholeNumber(result.completePackages)} PACKAGES",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${PackRatioCalculator.formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol} LEFTOVER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun FormulaCard(
    isExpanded: Boolean,
    formulaText: String,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "HOW IT WAS CALCULATED",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                ) {
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = formulaText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    onCopyResult: () -> Unit,
    onCopyCapacity: () -> Unit,
    onShare: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Main action: Save and Copy Result
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalButton(
                onClick = onCopyResult,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("COPY RESULT")
            }

            FilledTonalButton(
                onClick = onSave,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE")
            }
        }

        // Secondary actions: Copy Capacity, Share, Reset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onCopyCapacity,
                modifier = Modifier.weight(1f)
            ) {
                Text("COPY CAPACITY")
            }

            OutlinedButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("SHARE")
            }

            OutlinedButton(
                onClick = onReset
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
