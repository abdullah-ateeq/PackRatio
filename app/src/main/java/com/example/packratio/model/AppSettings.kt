package com.example.packratio.model

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

data class AppSettings(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val defaultQuantityType: QuantityType = QuantityType.ITEMS,
    val defaultBulkQuantity: String = "1000",
    val defaultPackageQuantity: String = "25",
    val defaultItemUnit: QuantityUnit = QuantityUnit.PIECES,
    val defaultWeightUnit: QuantityUnit = QuantityUnit.KG,
    val defaultVolumeUnit: QuantityUnit = QuantityUnit.L,
    val showUtilization: Boolean = true,
    val showLeftoverPercentage: Boolean = true,
    val showFormula: Boolean = true,
    val showPackageVisualization: Boolean = true,
    val showNotes: Boolean = true,
    val hapticFeedback: Boolean = true,
    val confirmDelete: Boolean = true,
    val historyLimit: Int = 100,
    val defaultHistorySort: HistorySortOption = HistorySortOption.RECENTLY_UPDATED
)
