package com.example.packratio.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.packratio.calculator.PackRatioCalculator
import com.example.packratio.data.PackRatioPreferences
import com.example.packratio.model.AppSettings
import com.example.packratio.model.HistoryFilterOption
import com.example.packratio.model.HistorySortOption
import com.example.packratio.model.PackHistoryItem
import com.example.packratio.model.PackRatioInput
import com.example.packratio.model.PackRatioResult
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CalculatorUiState(
    val packageName: String = "",
    val notes: String = "",
    val quantityType: QuantityType = QuantityType.ITEMS,
    val bulkQuantityInput: String = "1000",
    val bulkUnit: QuantityUnit = QuantityUnit.PIECES,
    val packageQuantityInput: String = "25",
    val packageUnit: QuantityUnit = QuantityUnit.PIECES,
    val result: PackRatioResult? = null,
    val validationError: String? = null,
    val isFormulaExpanded: Boolean = false,
    val snackbarMessage: String? = null
)

data class HistoryUiState(
    val searchQuery: String = "",
    val selectedFilter: HistoryFilterOption = HistoryFilterOption.ALL,
    val selectedSort: HistorySortOption = HistorySortOption.RECENTLY_UPDATED,
    val items: List<PackHistoryItem> = emptyList(),
    val itemToDelete: PackHistoryItem? = null,
    val showClearAllDialog: Boolean = false,
    val snackbarMessage: String? = null
)

class PackRatioViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = PackRatioPreferences(application)

    private val _calculatorState = MutableStateFlow(CalculatorUiState())
    val calculatorState: StateFlow<CalculatorUiState> = _calculatorState.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    private val _historyFilter = MutableStateFlow(HistoryFilterOption.ALL)
    private val _historySort = MutableStateFlow(HistorySortOption.RECENTLY_UPDATED)
    private val _itemToDelete = MutableStateFlow<PackHistoryItem?>(null)
    private val _showClearAllDialog = MutableStateFlow(false)
    private val _historySnackbar = MutableStateFlow<String?>(null)

    val settings: StateFlow<AppSettings> = preferences.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSettings()
    )

    private val rawHistory: StateFlow<List<PackHistoryItem>> = preferences.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val processedHistoryFlow = combine(
        rawHistory,
        _historySearchQuery,
        _historyFilter,
        _historySort
    ) { allItems, query, filter, sort ->
        // 1. Search
        val searched = if (query.isBlank()) {
            allItems
        } else {
            val q = query.trim().lowercase()
            allItems.filter {
                it.packageName.lowercase().contains(q) || it.notes.lowercase().contains(q)
            }
        }

        // 2. Filter
        val filtered = when (filter) {
            HistoryFilterOption.ALL -> searched
            HistoryFilterOption.FULLY_PACKED -> searched.filter { it.status == PackStatus.FULLY_PACKED }
            HistoryFilterOption.LEFTOVER -> searched.filter { it.status == PackStatus.LEFTOVER }
            HistoryFilterOption.PACKAGE_TOO_LARGE -> searched.filter { it.status == PackStatus.PACKAGE_TOO_LARGE }
        }

        // 3. Sort (Favorites always pinned first)
        filtered.sortedWith(
            compareByDescending<PackHistoryItem> { it.isFavorite }
                .then(
                    when (sort) {
                        HistorySortOption.RECENTLY_UPDATED -> compareByDescending { it.timestamp }
                        HistorySortOption.PACKAGE_COUNT -> compareByDescending { it.completePackageCount }
                        HistorySortOption.LARGEST_LEFTOVER -> compareByDescending {
                            PackRatioCalculator.normalizeToBaseUnit(it.leftoverQuantity, it.packageUnit)
                        }
                        HistorySortOption.PACKAGE_NAME -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.packageName }
                    }
                )
        )
    }

    val historyState: StateFlow<HistoryUiState> = combine(
        processedHistoryFlow,
        _itemToDelete,
        _showClearAllDialog,
        _historySnackbar
    ) { sorted, toDelete, showClearDialog, snackbar ->
        HistoryUiState(
            searchQuery = _historySearchQuery.value,
            selectedFilter = _historyFilter.value,
            selectedSort = _historySort.value,
            items = sorted,
            itemToDelete = toDelete,
            showClearAllDialog = showClearDialog,
            snackbarMessage = snackbar
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    init {
        // Perform initial calculation
        recalculate()
    }

    // --- Calculator Actions ---

    fun onQuantityTypeChanged(type: QuantityType) {
        val defaultBulkUnit = QuantityUnit.defaultForType(type)
        val defaultPackageUnit = QuantityUnit.defaultForType(type)

        _calculatorState.update { current ->
            val updatedBulk = if (type == QuantityType.ITEMS) {
                current.bulkQuantityInput.substringBefore(".")
            } else current.bulkQuantityInput

            val updatedPackage = if (type == QuantityType.ITEMS) {
                current.packageQuantityInput.substringBefore(".")
            } else current.packageQuantityInput

            current.copy(
                quantityType = type,
                bulkUnit = defaultBulkUnit,
                packageUnit = defaultPackageUnit,
                bulkQuantityInput = updatedBulk.ifEmpty { "1000" },
                packageQuantityInput = updatedPackage.ifEmpty { "25" }
            )
        }
        recalculate()
    }

    fun onBulkQuantityChanged(input: String) {
        val sanitized = sanitizeNumericInput(input, _calculatorState.value.quantityType)
        _calculatorState.update { it.copy(bulkQuantityInput = sanitized) }
        recalculate()
    }

    fun onPackageQuantityChanged(input: String) {
        val sanitized = sanitizeNumericInput(input, _calculatorState.value.quantityType)
        _calculatorState.update { it.copy(packageQuantityInput = sanitized) }
        recalculate()
    }

    fun onBulkUnitChanged(unit: QuantityUnit) {
        _calculatorState.update { it.copy(bulkUnit = unit) }
        recalculate()
    }

    fun onPackageUnitChanged(unit: QuantityUnit) {
        _calculatorState.update { it.copy(packageUnit = unit) }
        recalculate()
    }

    fun onPackageNameChanged(name: String) {
        if (name.length <= 60) {
            _calculatorState.update { it.copy(packageName = name) }
        }
    }

    fun onNotesChanged(notes: String) {
        if (notes.length <= 300) {
            _calculatorState.update { it.copy(notes = notes) }
        }
    }

    fun toggleFormulaExpanded() {
        _calculatorState.update { it.copy(isFormulaExpanded = !it.isFormulaExpanded) }
    }

    fun clearCalculatorSnackbar() {
        _calculatorState.update { it.copy(snackbarMessage = null) }
    }

    fun applyRetailPreset() {
        _calculatorState.update {
            it.copy(
                quantityType = QuantityType.ITEMS,
                bulkQuantityInput = "1000",
                bulkUnit = QuantityUnit.PIECES,
                packageQuantityInput = "25",
                packageUnit = QuantityUnit.PIECES,
                packageName = "Retail Pieces",
                notes = ""
            )
        }
        recalculate()
    }

    fun applyWeightPreset() {
        _calculatorState.update {
            it.copy(
                quantityType = QuantityType.WEIGHT,
                bulkQuantityInput = "10",
                bulkUnit = QuantityUnit.KG,
                packageQuantityInput = "500",
                packageUnit = QuantityUnit.G,
                packageName = "Weight Batch",
                notes = ""
            )
        }
        recalculate()
    }

    fun applyVolumePreset() {
        _calculatorState.update {
            it.copy(
                quantityType = QuantityType.VOLUME,
                bulkQuantityInput = "5",
                bulkUnit = QuantityUnit.L,
                packageQuantityInput = "250",
                packageUnit = QuantityUnit.ML,
                packageName = "Volume Bottling",
                notes = ""
            )
        }
        recalculate()
    }

    fun resetCalculator() {
        _calculatorState.update {
            it.copy(
                quantityType = QuantityType.ITEMS,
                bulkQuantityInput = "1000",
                bulkUnit = QuantityUnit.PIECES,
                packageQuantityInput = "25",
                packageUnit = QuantityUnit.PIECES,
                packageName = "",
                notes = "",
                isFormulaExpanded = false
            )
        }
        recalculate()
    }

    fun saveCurrentCalculation() {
        val state = _calculatorState.value
        val result = state.result
        if (result == null || state.validationError != null) {
            _calculatorState.update { it.copy(snackbarMessage = "Cannot save invalid calculation.") }
            return
        }

        val name = state.packageName.trim().ifEmpty { "Untitled Package" }
        val item = PackHistoryItem(
            packageName = name,
            notes = state.notes.trim(),
            quantityType = state.quantityType,
            bulkQuantity = result.bulkQuantityInput,
            bulkUnit = result.bulkUnit,
            packageQuantity = result.packageQuantityInput,
            packageUnit = result.packageUnit,
            completePackageCount = result.completePackages,
            packagedQuantity = result.packagedQuantity,
            leftoverQuantity = result.leftoverQuantity,
            utilization = result.utilizationPercent,
            leftoverPercent = result.leftoverPercent,
            status = result.status,
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )

        viewModelScope.launch {
            val saveResult = preferences.saveCalculation(item)
            if (saveResult.isSuccess) {
                _calculatorState.update { it.copy(snackbarMessage = "Calculation saved") }
            } else {
                _calculatorState.update {
                    it.copy(snackbarMessage = saveResult.exceptionOrNull()?.message ?: "Failed to save calculation")
                }
            }
        }
    }

    fun onCopiedResult() {
        _calculatorState.update { it.copy(snackbarMessage = "Result copied") }
    }

    fun onCopiedCapacity() {
        _calculatorState.update { it.copy(snackbarMessage = "Package count copied") }
    }

    private fun recalculate() {
        val state = _calculatorState.value

        if (!PackRatioCalculator.areUnitsCompatible(state.bulkUnit, state.packageUnit)) {
            _calculatorState.update {
                it.copy(result = null, validationError = "Bulk and package units must be compatible.")
            }
            return
        }

        val bulkClean = state.bulkQuantityInput.trim()
        val packageClean = state.packageQuantityInput.trim()

        if (bulkClean.isEmpty() || packageClean.isEmpty()) {
            _calculatorState.update { it.copy(result = null, validationError = null) }
            return
        }

        val bulkDecimal: BigDecimal
        val packageDecimal: BigDecimal

        try {
            bulkDecimal = BigDecimal(bulkClean)
            packageDecimal = BigDecimal(packageClean)
        } catch (_: NumberFormatException) {
            _calculatorState.update {
                it.copy(result = null, validationError = "Invalid number format.")
            }
            return
        }

        if (bulkDecimal.compareTo(BigDecimal.ZERO) < 0 || packageDecimal.compareTo(BigDecimal.ZERO) < 0) {
            _calculatorState.update {
                it.copy(result = null, validationError = "Negative values not allowed.")
            }
            return
        }

        if (bulkDecimal.compareTo(BigDecimal.ZERO) == 0) {
            _calculatorState.update {
                it.copy(result = null, validationError = "Bulk quantity must be greater than zero.")
            }
            return
        }

        if (packageDecimal.compareTo(BigDecimal.ZERO) == 0) {
            _calculatorState.update {
                it.copy(result = null, validationError = "Package quantity must be greater than zero.")
            }
            return
        }

        if (state.quantityType == QuantityType.ITEMS) {
            if (bulkClean.contains(".") || packageClean.contains(".")) {
                _calculatorState.update {
                    it.copy(result = null, validationError = "Items mode only allows whole numbers.")
                }
                return
            }
        }

        try {
            val input = PackRatioInput(
                packageName = state.packageName,
                notes = state.notes,
                quantityType = state.quantityType,
                bulkQuantity = bulkDecimal,
                bulkUnit = state.bulkUnit,
                packageQuantity = packageDecimal,
                packageUnit = state.packageUnit
            )
            val result = PackRatioCalculator.calculate(input)
            _calculatorState.update {
                it.copy(result = result, validationError = null)
            }
        } catch (e: ArithmeticException) {
            _calculatorState.update {
                it.copy(result = null, validationError = e.message ?: "Calculation overflow.")
            }
        } catch (e: Exception) {
            _calculatorState.update {
                it.copy(result = null, validationError = e.message ?: "Calculation error.")
            }
        }
    }

    private fun sanitizeNumericInput(input: String, type: QuantityType): String {
        val filtered = input.filter { it.isDigit() || it == '.' }
        if (type == QuantityType.ITEMS) {
            return filtered.filter { it.isDigit() }
        }
        val firstDot = filtered.indexOf('.')
        if (firstDot == -1) return filtered
        val integerPart = filtered.substring(0, firstDot)
        val decimalPart = filtered.substring(firstDot + 1).replace(".", "").take(4)
        return "$integerPart.$decimalPart"
    }

    // --- History Actions ---

    fun onHistorySearchQueryChanged(query: String) {
        _historySearchQuery.value = query
    }

    fun onHistoryFilterChanged(filter: HistoryFilterOption) {
        _historyFilter.value = filter
    }

    fun onHistorySortChanged(sort: HistorySortOption) {
        _historySort.value = sort
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            preferences.toggleFavorite(id)
        }
    }

    fun duplicateHistoryItem(id: String) {
        viewModelScope.launch {
            val res = preferences.duplicateCalculation(id)
            if (res.isFailure) {
                _historySnackbar.value = res.exceptionOrNull()?.message ?: "Failed to duplicate"
            } else {
                _historySnackbar.value = "Calculation duplicated"
            }
        }
    }

    fun requestDeleteHistoryItem(item: PackHistoryItem) {
        val confirm = settings.value.confirmDelete
        if (confirm) {
            _itemToDelete.value = item
        } else {
            deleteHistoryItem(item.id)
        }
    }

    fun confirmDelete() {
        val item = _itemToDelete.value ?: return
        deleteHistoryItem(item.id)
        _itemToDelete.value = null
    }

    fun cancelDelete() {
        _itemToDelete.value = null
    }

    private fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            preferences.deleteCalculation(id)
            _historySnackbar.value = "Calculation deleted"
        }
    }

    fun requestClearAllHistory() {
        _showClearAllDialog.value = true
    }

    fun confirmClearAllHistory() {
        viewModelScope.launch {
            preferences.clearAllHistory()
            _showClearAllDialog.value = false
            _historySnackbar.value = "History cleared"
        }
    }

    fun cancelClearAllHistory() {
        _showClearAllDialog.value = false
    }

    fun clearHistorySnackbar() {
        _historySnackbar.value = null
    }

    fun openHistoryItem(item: PackHistoryItem) {
        _calculatorState.update {
            it.copy(
                packageName = item.packageName,
                notes = item.notes,
                quantityType = item.quantityType,
                bulkQuantityInput = PackRatioCalculator.formatQuantity(item.bulkQuantity),
                bulkUnit = item.bulkUnit,
                packageQuantityInput = PackRatioCalculator.formatQuantity(item.packageQuantity),
                packageUnit = item.packageUnit
            )
        }
        recalculate()
    }

    // --- Settings Actions ---

    fun updateSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            preferences.saveSettings(newSettings)
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            preferences.resetSettings()
        }
    }
}
