package com.example.packratio.data

import com.example.packratio.model.AppSettings
import com.example.packratio.model.AppThemeMode
import com.example.packratio.model.HistoryFilterOption
import com.example.packratio.model.HistorySortOption
import com.example.packratio.model.PackHistoryItem
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HistoryJsonSerializerTest {

    @Test
    fun testSerializationAndDeserialization() {
        val item = PackHistoryItem(
            id = "test-id-123",
            packageName = "Retail \"Small\" Pack\nWith Linebreak",
            notes = "Test notes, special chars: ,:{}",
            quantityType = QuantityType.ITEMS,
            bulkQuantity = BigDecimal("1000"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("30"),
            packageUnit = QuantityUnit.PIECES,
            completePackageCount = 33L,
            packagedQuantity = BigDecimal("990"),
            leftoverQuantity = BigDecimal("10"),
            utilization = BigDecimal("99.00"),
            leftoverPercent = BigDecimal("1.00"),
            status = PackStatus.LEFTOVER,
            timestamp = 1700000000000L,
            isFavorite = true
        )

        val json = HistoryJsonSerializer.serializeItem(item)
        val deserialized = HistoryJsonSerializer.deserializeItem(json)

        assertNotNull(deserialized)
        assertEquals("test-id-123", deserialized?.id)
        assertEquals("Retail \"Small\" Pack\nWith Linebreak", deserialized?.packageName)
        assertEquals("Test notes, special chars: ,:{}", deserialized?.notes)
        assertEquals(QuantityType.ITEMS, deserialized?.quantityType)
        assertEquals(0, deserialized?.bulkQuantity?.compareTo(BigDecimal("1000")))
        assertEquals(QuantityUnit.PIECES, deserialized?.bulkUnit)
        assertEquals(0, deserialized?.packageQuantity?.compareTo(BigDecimal("30")))
        assertEquals(QuantityUnit.PIECES, deserialized?.packageUnit)
        assertEquals(33L, deserialized?.completePackageCount)
        assertEquals(0, deserialized?.packagedQuantity?.compareTo(BigDecimal("990")))
        assertEquals(0, deserialized?.leftoverQuantity?.compareTo(BigDecimal("10")))
        assertEquals(0, deserialized?.utilization?.compareTo(BigDecimal("99.00")))
        assertEquals(0, deserialized?.leftoverPercent?.compareTo(BigDecimal("1.00")))
        assertEquals(PackStatus.LEFTOVER, deserialized?.status)
        assertEquals(1700000000000L, deserialized?.timestamp)
        assertTrue(deserialized?.isFavorite == true)
    }

    @Test
    fun testListSerialization() {
        val items = listOf(
            PackHistoryItem(
                id = "1",
                packageName = "Batch A",
                quantityType = QuantityType.WEIGHT,
                bulkQuantity = BigDecimal("10"),
                bulkUnit = QuantityUnit.KG,
                packageQuantity = BigDecimal("500"),
                packageUnit = QuantityUnit.G,
                completePackageCount = 20L,
                packagedQuantity = BigDecimal("10000"),
                leftoverQuantity = BigDecimal.ZERO,
                utilization = BigDecimal("100.00"),
                leftoverPercent = BigDecimal.ZERO,
                status = PackStatus.FULLY_PACKED,
                isFavorite = false
            ),
            PackHistoryItem(
                id = "2",
                packageName = "Batch B",
                quantityType = QuantityType.VOLUME,
                bulkQuantity = BigDecimal("5"),
                bulkUnit = QuantityUnit.L,
                packageQuantity = BigDecimal("250"),
                packageUnit = QuantityUnit.ML,
                completePackageCount = 20L,
                packagedQuantity = BigDecimal("5000"),
                leftoverQuantity = BigDecimal.ZERO,
                utilization = BigDecimal("100.00"),
                leftoverPercent = BigDecimal.ZERO,
                status = PackStatus.FULLY_PACKED,
                isFavorite = true
            )
        )

        val json = HistoryJsonSerializer.serializeList(items)
        val deserializedList = HistoryJsonSerializer.deserializeList(json)

        assertEquals(2, deserializedList.size)
        assertEquals("1", deserializedList[0].id)
        assertFalse(deserializedList[0].isFavorite)
        assertEquals("2", deserializedList[1].id)
        assertTrue(deserializedList[1].isFavorite)
    }

    @Test
    fun testEmptyJsonList() {
        val emptyList = HistoryJsonSerializer.deserializeList("[]")
        assertTrue(emptyList.isEmpty())

        val emptyString = HistoryJsonSerializer.deserializeList("")
        assertTrue(emptyString.isEmpty())
    }

    @Test
    fun testMalformedJsonDoesNotCrash() {
        val result = HistoryJsonSerializer.deserializeList("{malformed}")
        assertTrue(result.isEmpty())

        val item = HistoryJsonSerializer.deserializeItem("not a json")
        assertEquals(null, item)
    }

    @Test
    fun testAppSettingsDefaults() {
        val settings = AppSettings()
        assertEquals(AppThemeMode.SYSTEM, settings.themeMode)
        assertTrue(settings.dynamicColor)
        assertEquals(QuantityType.ITEMS, settings.defaultQuantityType)
        assertEquals("1000", settings.defaultBulkQuantity)
        assertEquals("25", settings.defaultPackageQuantity)
        assertEquals(QuantityUnit.PIECES, settings.defaultItemUnit)
        assertEquals(QuantityUnit.KG, settings.defaultWeightUnit)
        assertEquals(QuantityUnit.L, settings.defaultVolumeUnit)
        assertTrue(settings.showUtilization)
        assertTrue(settings.showLeftoverPercentage)
        assertTrue(settings.showFormula)
        assertTrue(settings.showPackageVisualization)
        assertTrue(settings.showNotes)
        assertTrue(settings.hapticFeedback)
        assertTrue(settings.confirmDelete)
        assertEquals(100, settings.historyLimit)
        assertEquals(HistorySortOption.RECENTLY_UPDATED, settings.defaultHistorySort)
    }
}
