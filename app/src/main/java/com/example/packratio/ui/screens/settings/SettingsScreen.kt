package com.example.packratio.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.packratio.model.AppThemeMode
import com.example.packratio.model.HistorySortOption
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import com.example.packratio.util.HapticUtil
import com.example.packratio.viewmodel.PackRatioViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: PackRatioViewModel
) {
    val settings by viewModel.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Settings") },
            text = { Text("Reset all preferences to default values?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSettingsToDefault()
                        showResetDialog = false
                        scope.launch { snackbarHostState.showSnackbar("Settings reset to defaults") }
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear All History") },
            text = { Text("Delete all saved packaging calculations? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.confirmClearAllHistory()
                        showClearHistoryDialog = false
                        scope.launch { snackbarHostState.showSnackbar("History cleared") }
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp
                )
            )
            Text(
                text = "Configure calculator defaults and behavior",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Appearance Section
            SettingsSectionHeader(title = "APPEARANCE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Theme selector
                    SettingsDropdownRow(
                        title = "Theme",
                        subtitle = settings.themeMode.displayName,
                        options = AppThemeMode.entries.map { it.name to it.displayName },
                        selectedKey = settings.themeMode.name,
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(themeMode = AppThemeMode.valueOf(key)))
                        }
                    )

                    HorizontalDivider()

                    // Dynamic Color
                    SettingsSwitchRow(
                        title = "Dynamic Color",
                        subtitle = "Use Material You wallpaper colors (Android 12+)",
                        checked = settings.dynamicColor,
                        onCheckedChange = {
                            viewModel.updateSettings(settings.copy(dynamicColor = it))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calculator Defaults Section
            SettingsSectionHeader(title = "CALCULATOR DEFAULTS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Default Quantity Type
                    SettingsDropdownRow(
                        title = "Default Quantity Type",
                        subtitle = settings.defaultQuantityType.displayName,
                        options = QuantityType.entries.map { it.name to it.displayName },
                        selectedKey = settings.defaultQuantityType.name,
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(defaultQuantityType = QuantityType.valueOf(key)))
                        }
                    )

                    HorizontalDivider()

                    // Default Bulk Quantity
                    OutlinedTextField(
                        value = settings.defaultBulkQuantity,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }
                            viewModel.updateSettings(settings.copy(defaultBulkQuantity = clean))
                        },
                        label = { Text("Default Bulk Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Default Package Quantity
                    OutlinedTextField(
                        value = settings.defaultPackageQuantity,
                        onValueChange = { input ->
                            val clean = input.filter { it.isDigit() || it == '.' }
                            viewModel.updateSettings(settings.copy(defaultPackageQuantity = clean))
                        },
                        label = { Text("Default Package Quantity") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider()

                    // Default Weight Unit
                    SettingsDropdownRow(
                        title = "Default Weight Unit",
                        subtitle = "${settings.defaultWeightUnit.label} (${settings.defaultWeightUnit.symbol})",
                        options = QuantityUnit.unitsForType(QuantityType.WEIGHT).map { it.name to "${it.label} (${it.symbol})" },
                        selectedKey = settings.defaultWeightUnit.name,
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(defaultWeightUnit = QuantityUnit.valueOf(key)))
                        }
                    )

                    HorizontalDivider()

                    // Default Volume Unit
                    SettingsDropdownRow(
                        title = "Default Volume Unit",
                        subtitle = "${settings.defaultVolumeUnit.label} (${settings.defaultVolumeUnit.symbol})",
                        options = QuantityUnit.unitsForType(QuantityType.VOLUME).map { it.name to "${it.label} (${it.symbol})" },
                        selectedKey = settings.defaultVolumeUnit.name,
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(defaultVolumeUnit = QuantityUnit.valueOf(key)))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Options Section
            SettingsSectionHeader(title = "DISPLAY & RESULTS")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsSwitchRow(
                        title = "Show Utilization",
                        subtitle = "Display percentage of bulk quantity packaged",
                        checked = settings.showUtilization,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showUtilization = it)) }
                    )

                    HorizontalDivider()

                    SettingsSwitchRow(
                        title = "Show Leftover Percentage",
                        subtitle = "Display percentage of bulk quantity remaining",
                        checked = settings.showLeftoverPercentage,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showLeftoverPercentage = it)) }
                    )

                    HorizontalDivider()

                    SettingsSwitchRow(
                        title = "Show Formula",
                        subtitle = "Display 'How it was calculated' breakdown",
                        checked = settings.showFormula,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showFormula = it)) }
                    )

                    HorizontalDivider()

                    SettingsSwitchRow(
                        title = "Show Package Visualization",
                        subtitle = "Display proportional package distribution bar",
                        checked = settings.showPackageVisualization,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showPackageVisualization = it)) }
                    )

                    HorizontalDivider()

                    SettingsSwitchRow(
                        title = "Show Notes",
                        subtitle = "Allow adding optional notes to calculations",
                        checked = settings.showNotes,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(showNotes = it)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Behavior & History Section
            SettingsSectionHeader(title = "BEHAVIOR & STORAGE")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Vibrate on save, copy, and delete actions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(hapticFeedback = it)) }
                    )

                    HorizontalDivider()

                    SettingsSwitchRow(
                        title = "Confirm Delete",
                        subtitle = "Show confirmation dialog when deleting history items",
                        checked = settings.confirmDelete,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(confirmDelete = it)) }
                    )

                    HorizontalDivider()

                    // History Limit (50 or 100)
                    SettingsDropdownRow(
                        title = "History Limit",
                        subtitle = "${settings.historyLimit} items max",
                        options = listOf("50" to "50 items", "100" to "100 items"),
                        selectedKey = settings.historyLimit.toString(),
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(historyLimit = key.toInt()))
                        }
                    )

                    HorizontalDivider()

                    // Default History Sort
                    SettingsDropdownRow(
                        title = "Default History Sort",
                        subtitle = settings.defaultHistorySort.displayName,
                        options = HistorySortOption.entries.map { it.name to it.displayName },
                        selectedKey = settings.defaultHistorySort.name,
                        onSelectKey = { key ->
                            viewModel.updateSettings(settings.copy(defaultHistorySort = HistorySortOption.valueOf(key)))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        HapticUtil.performLight(haptic, settings.hapticFeedback)
                        showResetDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("RESET SETTINGS", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = {
                        HapticUtil.performLight(haptic, settings.hapticFeedback)
                        showClearHistoryDialog = true
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CLEAR HISTORY", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsDropdownRow(
    title: String,
    subtitle: String,
    options: List<Pair<String, String>>,
    selectedKey: String,
    onSelectKey: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }

        Box {
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select $title")
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = label,
                                fontWeight = if (key == selectedKey) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onSelectKey(key)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
