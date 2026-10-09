package com.example.packratio.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.packratio.model.AppSettings
import com.example.packratio.model.AppThemeMode
import com.example.packratio.model.HistorySortOption
import com.example.packratio.model.PackHistoryItem
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "packratio_preferences")

class PackRatioPreferences(private val context: Context) {

    companion object {
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_DEFAULT_QUANTITY_TYPE = stringPreferencesKey("default_quantity_type")
        val KEY_DEFAULT_BULK_QUANTITY = stringPreferencesKey("default_bulk_quantity")
        val KEY_DEFAULT_PACKAGE_QUANTITY = stringPreferencesKey("default_package_quantity")
        val KEY_DEFAULT_ITEM_UNIT = stringPreferencesKey("default_item_unit")
        val KEY_DEFAULT_WEIGHT_UNIT = stringPreferencesKey("default_weight_unit")
        val KEY_DEFAULT_VOLUME_UNIT = stringPreferencesKey("default_volume_unit")
        val KEY_SHOW_UTILIZATION = booleanPreferencesKey("show_utilization")
        val KEY_SHOW_LEFTOVER_PERCENT = booleanPreferencesKey("show_leftover_percent")
        val KEY_SHOW_FORMULA = booleanPreferencesKey("show_formula")
        val KEY_SHOW_PACKAGE_VISUALIZATION = booleanPreferencesKey("show_package_visualization")
        val KEY_SHOW_NOTES = booleanPreferencesKey("show_notes")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_CONFIRM_DELETE = booleanPreferencesKey("confirm_delete")
        val KEY_HISTORY_LIMIT = intPreferencesKey("history_limit")
        val KEY_DEFAULT_HISTORY_SORT = stringPreferencesKey("default_history_sort")

        val KEY_SAVED_CALCULATIONS = stringPreferencesKey("saved_calculations_json")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[KEY_THEME_MODE]?.let { runCatching { AppThemeMode.valueOf(it) }.getOrNull() }
                ?: AppThemeMode.SYSTEM,
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            defaultQuantityType = prefs[KEY_DEFAULT_QUANTITY_TYPE]?.let { runCatching { QuantityType.valueOf(it) }.getOrNull() }
                ?: QuantityType.ITEMS,
            defaultBulkQuantity = prefs[KEY_DEFAULT_BULK_QUANTITY] ?: "1000",
            defaultPackageQuantity = prefs[KEY_DEFAULT_PACKAGE_QUANTITY] ?: "25",
            defaultItemUnit = prefs[KEY_DEFAULT_ITEM_UNIT]?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
                ?: QuantityUnit.PIECES,
            defaultWeightUnit = prefs[KEY_DEFAULT_WEIGHT_UNIT]?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
                ?: QuantityUnit.KG,
            defaultVolumeUnit = prefs[KEY_DEFAULT_VOLUME_UNIT]?.let { runCatching { QuantityUnit.valueOf(it) }.getOrNull() }
                ?: QuantityUnit.L,
            showUtilization = prefs[KEY_SHOW_UTILIZATION] ?: true,
            showLeftoverPercentage = prefs[KEY_SHOW_LEFTOVER_PERCENT] ?: true,
            showFormula = prefs[KEY_SHOW_FORMULA] ?: true,
            showPackageVisualization = prefs[KEY_SHOW_PACKAGE_VISUALIZATION] ?: true,
            showNotes = prefs[KEY_SHOW_NOTES] ?: true,
            hapticFeedback = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
            confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true,
            historyLimit = prefs[KEY_HISTORY_LIMIT] ?: 100,
            defaultHistorySort = prefs[KEY_DEFAULT_HISTORY_SORT]?.let { runCatching { HistorySortOption.valueOf(it) }.getOrNull() }
                ?: HistorySortOption.RECENTLY_UPDATED
        )
    }

    val historyFlow: Flow<List<PackHistoryItem>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_SAVED_CALCULATIONS] ?: "[]"
        HistoryJsonSerializer.deserializeList(json)
    }

    suspend fun saveSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = settings.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = settings.dynamicColor
            prefs[KEY_DEFAULT_QUANTITY_TYPE] = settings.defaultQuantityType.name
            prefs[KEY_DEFAULT_BULK_QUANTITY] = settings.defaultBulkQuantity
            prefs[KEY_DEFAULT_PACKAGE_QUANTITY] = settings.defaultPackageQuantity
            prefs[KEY_DEFAULT_ITEM_UNIT] = settings.defaultItemUnit.name
            prefs[KEY_DEFAULT_WEIGHT_UNIT] = settings.defaultWeightUnit.name
            prefs[KEY_DEFAULT_VOLUME_UNIT] = settings.defaultVolumeUnit.name
            prefs[KEY_SHOW_UTILIZATION] = settings.showUtilization
            prefs[KEY_SHOW_LEFTOVER_PERCENT] = settings.showLeftoverPercentage
            prefs[KEY_SHOW_FORMULA] = settings.showFormula
            prefs[KEY_SHOW_PACKAGE_VISUALIZATION] = settings.showPackageVisualization
            prefs[KEY_SHOW_NOTES] = settings.showNotes
            prefs[KEY_HAPTIC_FEEDBACK] = settings.hapticFeedback
            prefs[KEY_CONFIRM_DELETE] = settings.confirmDelete
            prefs[KEY_HISTORY_LIMIT] = settings.historyLimit
            prefs[KEY_DEFAULT_HISTORY_SORT] = settings.defaultHistorySort.name
        }
    }

    suspend fun resetSettings() {
        saveSettings(AppSettings())
    }

    suspend fun saveCalculation(item: PackHistoryItem): Result<Unit> {
        val currentSettings = settingsFlow.first()
        val limit = currentSettings.historyLimit
        val currentHistory = historyFlow.first().toMutableList()

        if (currentHistory.size >= limit) {
            val oldestNonFavoriteIndex = currentHistory.indexOfLast { !it.isFavorite }
            if (oldestNonFavoriteIndex != -1) {
                currentHistory.removeAt(oldestNonFavoriteIndex)
            } else {
                return Result.failure(IllegalStateException("History limit reached. Remove an item before saving."))
            }
        }

        currentHistory.add(0, item)
        val json = HistoryJsonSerializer.serializeList(currentHistory)
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = json
        }
        return Result.success(Unit)
    }

    suspend fun toggleFavorite(id: String) {
        val currentHistory = historyFlow.first().map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
        val json = HistoryJsonSerializer.serializeList(currentHistory)
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = json
        }
    }

    suspend fun duplicateCalculation(id: String): Result<Unit> {
        val currentHistory = historyFlow.first()
        val itemToDuplicate = currentHistory.find { it.id == id }
            ?: return Result.failure(IllegalArgumentException("Item not found"))

        val duplicated = itemToDuplicate.copy(
            id = UUID.randomUUID().toString(),
            packageName = "Copy of ${itemToDuplicate.packageName}",
            timestamp = System.currentTimeMillis(),
            isFavorite = false
        )

        return saveCalculation(duplicated)
    }

    suspend fun deleteCalculation(id: String) {
        val currentHistory = historyFlow.first().filter { it.id != id }
        val json = HistoryJsonSerializer.serializeList(currentHistory)
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = json
        }
    }

    suspend fun clearAllHistory() {
        context.dataStore.edit { prefs ->
            prefs[KEY_SAVED_CALCULATIONS] = "[]"
        }
    }
}
