package com.example.packratio.calculator

import com.example.packratio.model.PackRatioInput
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PackRatioCalculatorTest {

    // 1. Exact division
    @Test
    fun testExactDivision() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1000"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("25"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(40L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("100.00")))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 2. Non-exact division
    @Test
    fun testNonExactDivision() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1000"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("30"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(33L, result.completePackages)
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("990")))
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("10")))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("99.00")))
        assertEquals(PackStatus.LEFTOVER, result.status)
    }

    // 3. Package larger than bulk
    @Test
    fun testPackageLargerThanBulk() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("20"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("25"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(0L, result.completePackages)
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("20")))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.leftoverPercent.compareTo(BigDecimal("100.00")))
        assertEquals(PackStatus.PACKAGE_TOO_LARGE, result.status)
    }

    // 4. One package exactly
    @Test
    fun testOnePackageExact() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("50"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("50"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(1L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 5. Multiple packages
    @Test
    fun testMultiplePackages() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("500"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("50"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(10L, result.completePackages)
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("500")))
    }

    // 6. Zero leftover
    @Test
    fun testZeroLeftover() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("100"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("20"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.leftoverPercent.compareTo(BigDecimal.ZERO))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 7. Positive leftover
    @Test
    fun testPositiveLeftover() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("105"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("20"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(5L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("5")))
        assertEquals(PackStatus.LEFTOVER, result.status)
    }

    // 8. Items mode
    @Test
    fun testItemsMode() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("120"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("10"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(12L, result.completePackages)
        assertEquals(QuantityUnit.PIECES, result.packageUnit)
    }

    // 9. Weight mode (Known example: 10 kg bulk, 500 g per package)
    @Test
    fun testWeightModeMetricKnownExample() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("10"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("500"),
            packageUnit = QuantityUnit.G,
            quantityType = QuantityType.WEIGHT
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(20L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("10000"))) // in grams: 10,000 g
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("100.00")))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 10. Volume mode (Known example: 5 L bulk, 250 ml per package)
    @Test
    fun testVolumeModeKnownExample() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("5"),
            bulkUnit = QuantityUnit.L,
            packageQuantity = BigDecimal("250"),
            packageUnit = QuantityUnit.ML,
            quantityType = QuantityType.VOLUME
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(20L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("5000"))) // in ml: 5,000 ml
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("100.00")))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 11. Metric weight conversion (1 kg bulk, 300 g package)
    @Test
    fun testMetricWeightConversionWithLeftover() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("300"),
            packageUnit = QuantityUnit.G,
            quantityType = QuantityType.WEIGHT
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(3L, result.completePackages)
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("900")))
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("100")))
        assertEquals(0, result.utilizationPercent.compareTo(BigDecimal("90.00")))
        assertEquals(0, result.leftoverPercent.compareTo(BigDecimal("10.00")))
        assertEquals(PackStatus.LEFTOVER, result.status)
    }

    // 12. Imperial weight conversion (1 lb = 16 oz = 453.59237 g)
    @Test
    fun testImperialWeightConversion() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1"),
            bulkUnit = QuantityUnit.LB,
            packageQuantity = BigDecimal("4"),
            packageUnit = QuantityUnit.OZ,
            quantityType = QuantityType.WEIGHT
        )
        val result = PackRatioCalculator.calculate(input)
        // 1 lb = 16 oz. 16 oz / 4 oz = 4 packages
        assertEquals(4L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
        assertEquals(PackStatus.FULLY_PACKED, result.status)
    }

    // 13. Metric volume conversion
    @Test
    fun testMetricVolumeConversion() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("2"),
            bulkUnit = QuantityUnit.L,
            packageQuantity = BigDecimal("500"),
            packageUnit = QuantityUnit.ML,
            quantityType = QuantityType.VOLUME
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(4L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 14. US volume conversion (1 US gal = 128 US fl oz)
    @Test
    fun testUSVolumeConversion() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1"),
            bulkUnit = QuantityUnit.US_GAL,
            packageQuantity = BigDecimal("16"),
            packageUnit = QuantityUnit.FL_OZ,
            quantityType = QuantityType.VOLUME
        )
        val result = PackRatioCalculator.calculate(input)
        // 1 US gallon = 3785.411784 ml, 16 fl oz = 16 * 29.5735295625 = 473.176473 ml
        // 3785.411784 / 473.176473 = 8
        assertEquals(8L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 15. Mixed compatible units
    @Test
    fun testMixedCompatibleUnits() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("0.5"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("50000"),
            packageUnit = QuantityUnit.MG,
            quantityType = QuantityType.WEIGHT
        )
        // 0.5 kg = 500 g = 500,000 mg. 500,000 / 50,000 = 10 packages
        val result = PackRatioCalculator.calculate(input)
        assertEquals(10L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 16. Incompatible units check
    @Test
    fun testIncompatibleUnits() {
        assertFalse(PackRatioCalculator.areUnitsCompatible(QuantityUnit.KG, QuantityUnit.L))
        assertFalse(PackRatioCalculator.areUnitsCompatible(QuantityUnit.PIECES, QuantityUnit.G))
        assertTrue(PackRatioCalculator.areUnitsCompatible(QuantityUnit.KG, QuantityUnit.LB))
        assertTrue(PackRatioCalculator.areUnitsCompatible(QuantityUnit.L, QuantityUnit.FL_OZ))

        val input = PackRatioInput(
            bulkQuantity = BigDecimal("10"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("500"),
            packageUnit = QuantityUnit.ML,
            quantityType = QuantityType.WEIGHT
        )
        assertThrows(IllegalArgumentException::class.java) {
            PackRatioCalculator.calculate(input)
        }
    }

    // 17. Decimal weight
    @Test
    fun testDecimalWeight() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("2.5"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("0.5"),
            packageUnit = QuantityUnit.KG,
            quantityType = QuantityType.WEIGHT
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(5L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 18. Decimal volume
    @Test
    fun testDecimalVolume() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1.75"),
            bulkUnit = QuantityUnit.L,
            packageQuantity = BigDecimal("0.5"),
            packageUnit = QuantityUnit.L,
            quantityType = QuantityType.VOLUME
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(3L, result.completePackages)
        assertEquals(0, result.packagedQuantity.compareTo(BigDecimal("1.5")))
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("0.25")))
    }

    // 19. Complete package floor behavior (Never round up!)
    @Test
    fun testFloorBehaviorNeverRoundsUp() {
        // 99 / 10 = 9.9 -> MUST BE 9, not 10
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("99"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("10"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(9L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal("9")))
    }

    // 20. Utilization percentage calculation
    @Test
    fun testUtilizationPercentage() {
        val packaged = BigDecimal("990")
        val bulk = BigDecimal("1000")
        val util = PackRatioCalculator.calculateUtilization(packaged, bulk)
        assertEquals(0, util.compareTo(BigDecimal("99.00")))
    }

    // 21. Leftover percentage calculation
    @Test
    fun testLeftoverPercentage() {
        val leftover = BigDecimal("10")
        val bulk = BigDecimal("1000")
        val pct = PackRatioCalculator.calculateLeftoverPercent(leftover, bulk)
        assertEquals(0, pct.compareTo(BigDecimal("1.00")))
    }

    // 22. Status detection
    @Test
    fun testStatusDetection() {
        assertEquals(PackStatus.PACKAGE_TOO_LARGE, PackRatioCalculator.determineStatus(0L, BigDecimal("20")))
        assertEquals(PackStatus.FULLY_PACKED, PackRatioCalculator.determineStatus(5L, BigDecimal.ZERO))
        assertEquals(PackStatus.LEFTOVER, PackRatioCalculator.determineStatus(5L, BigDecimal("2")))
    }

    // 23. Large quantities handling
    @Test
    fun testLargeQuantities() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1000000000"), // 1 Billion
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("50"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(20000000L, result.completePackages) // 20 Million packages
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 24. BigDecimal precision (no floating-point rounding errors)
    @Test
    fun testBigDecimalPrecision() {
        val val1 = BigDecimal("0.1").add(BigDecimal("0.2"))
        assertEquals("0.3", val1.toPlainString())

        val input = PackRatioInput(
            bulkQuantity = BigDecimal("0.3"),
            bulkUnit = QuantityUnit.KG,
            packageQuantity = BigDecimal("0.1"),
            packageUnit = QuantityUnit.KG,
            quantityType = QuantityType.WEIGHT
        )
        val result = PackRatioCalculator.calculate(input)
        assertEquals(3L, result.completePackages)
        assertEquals(0, result.leftoverQuantity.compareTo(BigDecimal.ZERO))
    }

    // 25. Number formatting with comma grouping and trimmed zeros
    @Test
    fun testFormatting() {
        assertEquals("1,000", PackRatioCalculator.formatQuantity(BigDecimal("1000.0000")))
        assertEquals("10.5", PackRatioCalculator.formatQuantity(BigDecimal("10.5000")))
        assertEquals("0.25", PackRatioCalculator.formatQuantity(BigDecimal("0.2500")))
        assertEquals("99%", PackRatioCalculator.formatPercent(BigDecimal("99.00")))
        assertEquals("83.33%", PackRatioCalculator.formatPercent(BigDecimal("83.3333")))
    }

    // 26. Safe overflow handling
    @Test
    fun testOverflowProtection() {
        val hugeBulk = BigDecimal("1e30")
        val tinyPackage = BigDecimal("1e-10")
        assertThrows(ArithmeticException::class.java) {
            PackRatioCalculator.calculateCompletePackages(hugeBulk, tinyPackage)
        }
    }

    // 27. Zero bulk quantity validation
    @Test
    fun testZeroBulkThrows() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal.ZERO,
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("10"),
            packageUnit = QuantityUnit.PIECES
        )
        assertThrows(IllegalArgumentException::class.java) {
            PackRatioCalculator.calculate(input)
        }
    }

    // 28. Zero package quantity validation
    @Test
    fun testZeroPackageThrows() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("100"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal.ZERO,
            packageUnit = QuantityUnit.PIECES
        )
        assertThrows(IllegalArgumentException::class.java) {
            PackRatioCalculator.calculate(input)
        }
    }

    // 29. Copy Summary text format
    @Test
    fun testCopySummaryFormat() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1000"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("30"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        val summary = PackRatioCalculator.formatCopySummary(result, "Retail Pack")
        assertTrue(summary.contains("Retail Pack"))
        assertTrue(summary.contains("Bulk: 1,000 pieces"))
        assertTrue(summary.contains("Package Size: 30 pieces"))
        assertTrue(summary.contains("Complete Packages: 33"))
        assertTrue(summary.contains("Packaged: 990 pieces"))
        assertTrue(summary.contains("Leftover: 10 pieces"))
        assertTrue(summary.contains("Utilization: 99%"))
        assertTrue(summary.contains("Status: LEFTOVER"))
    }

    // 30. Share Summary text format
    @Test
    fun testShareSummaryFormat() {
        val input = PackRatioInput(
            bulkQuantity = BigDecimal("1000"),
            bulkUnit = QuantityUnit.PIECES,
            packageQuantity = BigDecimal("25"),
            packageUnit = QuantityUnit.PIECES,
            quantityType = QuantityType.ITEMS
        )
        val result = PackRatioCalculator.calculate(input)
        val summary = PackRatioCalculator.formatShareSummary(result, "")
        assertTrue(summary.contains("PackRatio"))
        assertTrue(summary.contains("Bulk Quantity: 1,000 pieces"))
        assertTrue(summary.contains("Complete Packages: 40"))
        assertTrue(summary.contains("Status: FULLY PACKED"))
    }
}
