package com.example.packratio.calculator

import com.example.packratio.model.PackRatioInput
import com.example.packratio.model.PackRatioResult
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PackRatioCalculator {

    // Exact constants for weight (Base unit: Milligrams)
    private val FACTOR_MG = BigDecimal.ONE
    private val FACTOR_G = BigDecimal("1000")
    private val FACTOR_KG = BigDecimal("1000000")
    private val FACTOR_OZ = BigDecimal("28349.523125") // 28.349523125 g * 1000 mg/g
    private val FACTOR_LB = BigDecimal("453592.37")     // 453.59237 g * 1000 mg/g

    // Exact constants for volume (Base unit: Milliliters)
    private val FACTOR_ML = BigDecimal.ONE
    private val FACTOR_L = BigDecimal("1000")
    private val FACTOR_FL_OZ = BigDecimal("29.5735295625")
    private val FACTOR_US_GAL = BigDecimal("3785.411784")

    // Items (Base unit: Pieces)
    private val FACTOR_PIECES = BigDecimal.ONE

    private val MAX_LONG_BD = BigDecimal(Long.MAX_VALUE)

    fun getUnitFactor(unit: QuantityUnit): BigDecimal = when (unit) {
        QuantityUnit.PIECES -> FACTOR_PIECES
        QuantityUnit.MG -> FACTOR_MG
        QuantityUnit.G -> FACTOR_G
        QuantityUnit.KG -> FACTOR_KG
        QuantityUnit.OZ -> FACTOR_OZ
        QuantityUnit.LB -> FACTOR_LB
        QuantityUnit.ML -> FACTOR_ML
        QuantityUnit.L -> FACTOR_L
        QuantityUnit.FL_OZ -> FACTOR_FL_OZ
        QuantityUnit.US_GAL -> FACTOR_US_GAL
    }

    fun areUnitsCompatible(bulkUnit: QuantityUnit, packageUnit: QuantityUnit): Boolean {
        return bulkUnit.type == packageUnit.type
    }

    fun normalizeToBaseUnit(quantity: BigDecimal, unit: QuantityUnit): BigDecimal {
        val factor = getUnitFactor(unit)
        return quantity.multiply(factor)
    }

    fun convertFromBaseUnit(quantityInBase: BigDecimal, targetUnit: QuantityUnit): BigDecimal {
        val factor = getUnitFactor(targetUnit)
        if (factor.compareTo(BigDecimal.ONE) == 0) {
            return quantityInBase.stripTrailingZeros()
        }
        return quantityInBase.divide(factor, 12, RoundingMode.HALF_UP).stripTrailingZeros()
    }

    fun calculateCompletePackages(bulk: BigDecimal, packageSize: BigDecimal): Long {
        if (packageSize.compareTo(BigDecimal.ZERO) <= 0 || bulk.compareTo(packageSize) < 0) {
            return 0L
        }
        val quotient = bulk.divideToIntegralValue(packageSize)
        if (quotient > MAX_LONG_BD) {
            throw ArithmeticException("Package count is too large.")
        }
        return quotient.toLong()
    }

    fun calculatePackagedQuantity(completePackages: Long, packageSize: BigDecimal): BigDecimal {
        return packageSize.multiply(BigDecimal.valueOf(completePackages))
    }

    fun calculateLeftover(bulk: BigDecimal, packaged: BigDecimal): BigDecimal {
        val leftover = bulk.subtract(packaged)
        return if (leftover.compareTo(BigDecimal.ZERO) < 0) BigDecimal.ZERO else leftover
    }

    fun calculateUtilization(packaged: BigDecimal, bulk: BigDecimal): BigDecimal {
        if (bulk.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        return packaged.multiply(BigDecimal(100)).divide(bulk, 4, RoundingMode.HALF_UP)
    }

    fun calculateLeftoverPercent(leftover: BigDecimal, bulk: BigDecimal): BigDecimal {
        if (bulk.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO
        return leftover.multiply(BigDecimal(100)).divide(bulk, 4, RoundingMode.HALF_UP)
    }

    fun determineStatus(completePackages: Long, leftover: BigDecimal): PackStatus {
        return when {
            completePackages == 0L -> PackStatus.PACKAGE_TOO_LARGE
            leftover.compareTo(BigDecimal.ZERO) == 0 -> PackStatus.FULLY_PACKED
            else -> PackStatus.LEFTOVER
        }
    }

    fun generateStatusExplanation(
        status: PackStatus,
        completePackages: Long,
        leftoverInPackageUnit: BigDecimal,
        packageUnit: QuantityUnit
    ): String {
        return when (status) {
            PackStatus.FULLY_PACKED -> "All available quantity can be divided into complete packages."
            PackStatus.LEFTOVER -> {
                val formattedLeftover = formatQuantity(leftoverInPackageUnit)
                val formattedPackages = formatWholeNumber(completePackages)
                "$formattedLeftover ${packageUnit.symbol} remain after making $formattedPackages complete packages."
            }
            PackStatus.PACKAGE_TOO_LARGE -> "The bulk quantity is not enough to make one complete package."
        }
    }

    fun calculate(input: PackRatioInput): PackRatioResult {
        require(areUnitsCompatible(input.bulkUnit, input.packageUnit)) {
            "Bulk and package units must be compatible."
        }
        require(input.bulkQuantity.compareTo(BigDecimal.ZERO) > 0) {
            "Bulk quantity must be greater than zero."
        }
        require(input.packageQuantity.compareTo(BigDecimal.ZERO) > 0) {
            "Package quantity must be greater than zero."
        }

        // Normalize both to base units (mg for weight, ml for volume, pieces for items)
        val normalizedBulk = normalizeToBaseUnit(input.bulkQuantity, input.bulkUnit)
        val normalizedPackage = normalizeToBaseUnit(input.packageQuantity, input.packageUnit)

        val completePackages = calculateCompletePackages(normalizedBulk, normalizedPackage)
        val packagedNormalized = calculatePackagedQuantity(completePackages, normalizedPackage)
        val leftoverNormalized = calculateLeftover(normalizedBulk, packagedNormalized)

        val utilizationPercent = calculateUtilization(packagedNormalized, normalizedBulk)
        val leftoverPercent = calculateLeftoverPercent(leftoverNormalized, normalizedBulk)

        val status = determineStatus(completePackages, leftoverNormalized)

        // Convert packaged and leftover quantities to package unit for user-friendly display
        val packagedInPackageUnit = convertFromBaseUnit(packagedNormalized, input.packageUnit)
        val leftoverInPackageUnit = convertFromBaseUnit(leftoverNormalized, input.packageUnit)

        val statusExplanation = generateStatusExplanation(
            status = status,
            completePackages = completePackages,
            leftoverInPackageUnit = leftoverInPackageUnit,
            packageUnit = input.packageUnit
        )

        val formulaExplanation = generateFormulaExplanation(
            input = input,
            normalizedBulk = normalizedBulk,
            normalizedPackage = normalizedPackage,
            completePackages = completePackages,
            packagedInPackageUnit = packagedInPackageUnit,
            leftoverInPackageUnit = leftoverInPackageUnit,
            utilizationPercent = utilizationPercent,
            leftoverPercent = leftoverPercent,
            status = status
        )

        return PackRatioResult(
            completePackages = completePackages,
            normalizedBulkQuantity = normalizedBulk,
            normalizedPackageQuantity = normalizedPackage,
            packagedQuantity = packagedInPackageUnit,
            leftoverQuantity = leftoverInPackageUnit,
            utilizationPercent = utilizationPercent,
            leftoverPercent = leftoverPercent,
            status = status,
            statusExplanation = statusExplanation,
            bulkUnit = input.bulkUnit,
            packageUnit = input.packageUnit,
            bulkQuantityInput = input.bulkQuantity,
            packageQuantityInput = input.packageQuantity,
            packagedNormalized = packagedNormalized,
            leftoverNormalized = leftoverNormalized,
            formulaExplanation = formulaExplanation
        )
    }

    private fun generateFormulaExplanation(
        input: PackRatioInput,
        normalizedBulk: BigDecimal,
        normalizedPackage: BigDecimal,
        completePackages: Long,
        packagedInPackageUnit: BigDecimal,
        leftoverInPackageUnit: BigDecimal,
        utilizationPercent: BigDecimal,
        leftoverPercent: BigDecimal,
        status: PackStatus
    ): String {
        val sb = StringBuilder()
        sb.append("Bulk: ${formatQuantity(input.bulkQuantity)} ${input.bulkUnit.symbol}\n")
        sb.append("Package size: ${formatQuantity(input.packageQuantity)} ${input.packageUnit.symbol}\n\n")

        if (input.bulkUnit != input.packageUnit) {
            sb.append("Unit Normalization:\n")
            val baseUnitSymbol = when (input.quantityType) {
                QuantityType.ITEMS -> "pieces"
                QuantityType.WEIGHT -> "mg"
                QuantityType.VOLUME -> "ml"
            }
            sb.append("• Bulk: ${formatQuantity(input.bulkQuantity)} ${input.bulkUnit.symbol} = ${formatQuantity(normalizedBulk)} $baseUnitSymbol\n")
            sb.append("• Package: ${formatQuantity(input.packageQuantity)} ${input.packageUnit.symbol} = ${formatQuantity(normalizedPackage)} $baseUnitSymbol\n\n")
        }

        if (status == PackStatus.PACKAGE_TOO_LARGE) {
            sb.append("Package check:\n")
            sb.append("${formatQuantity(input.bulkQuantity)} < ${formatQuantity(input.packageQuantity)} (bulk is smaller than one complete package)\n\n")
            sb.append("Complete packages: 0\n")
            sb.append("Packaged quantity: 0 ${input.packageUnit.symbol}\n")
            sb.append("Leftover: ${formatQuantity(leftoverInPackageUnit)} ${input.packageUnit.symbol}\n")
            sb.append("Bulk utilization: 0%\n")
            sb.append("Leftover: 100%")
            return sb.toString()
        }

        sb.append("Complete packages:\n")
        if (input.bulkUnit == input.packageUnit) {
            sb.append("floor(${formatQuantity(input.bulkQuantity)} ÷ ${formatQuantity(input.packageQuantity)})\n= ${formatWholeNumber(completePackages)}\n\n")
        } else {
            sb.append("floor(${formatQuantity(normalizedBulk)} ÷ ${formatQuantity(normalizedPackage)})\n= ${formatWholeNumber(completePackages)}\n\n")
        }

        sb.append("Packaged quantity:\n")
        sb.append("${formatWholeNumber(completePackages)} × ${formatQuantity(input.packageQuantity)}\n= ${formatQuantity(packagedInPackageUnit)} ${input.packageUnit.symbol}\n\n")

        sb.append("Leftover:\n")
        if (input.bulkUnit == input.packageUnit) {
            sb.append("${formatQuantity(input.bulkQuantity)} − ${formatQuantity(packagedInPackageUnit)}\n= ${formatQuantity(leftoverInPackageUnit)} ${input.packageUnit.symbol}\n\n")
        } else {
            val bulkInPackageUnit = convertFromBaseUnit(normalizedBulk, input.packageUnit)
            sb.append("${formatQuantity(bulkInPackageUnit)} − ${formatQuantity(packagedInPackageUnit)}\n= ${formatQuantity(leftoverInPackageUnit)} ${input.packageUnit.symbol}\n\n")
        }

        sb.append("Bulk utilization:\n")
        sb.append("${formatQuantity(packagedInPackageUnit)} ÷ ${formatQuantity(if (input.bulkUnit == input.packageUnit) input.bulkQuantity else convertFromBaseUnit(normalizedBulk, input.packageUnit))} × 100\n= ${formatPercent(utilizationPercent)}\n\n")

        sb.append("Leftover percentage:\n")
        sb.append("${formatQuantity(leftoverInPackageUnit)} ÷ ${formatQuantity(if (input.bulkUnit == input.packageUnit) input.bulkQuantity else convertFromBaseUnit(normalizedBulk, input.packageUnit))} × 100\n= ${formatPercent(leftoverPercent)}")

        return sb.toString()
    }

    fun formatQuantity(value: BigDecimal, maxDecimals: Int = 4): String {
        val stripped = value.stripTrailingZeros()
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        symbols.decimalSeparator = '.'

        if (stripped.scale() <= 0) {
            val df = DecimalFormat("#,##0", symbols)
            return df.format(stripped.toBigInteger())
        }

        val scale = stripped.scale().coerceAtMost(maxDecimals)
        val rounded = stripped.setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros()

        if (rounded.scale() <= 0) {
            val df = DecimalFormat("#,##0", symbols)
            return df.format(rounded.toBigInteger())
        }

        val pattern = StringBuilder("#,##0.")
        repeat(rounded.scale()) { pattern.append("#") }
        val df = DecimalFormat(pattern.toString(), symbols)
        return df.format(rounded)
    }

    fun formatPercent(value: BigDecimal): String {
        val rounded = value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        val symbols = DecimalFormatSymbols(Locale.US)
        val df = DecimalFormat("#,##0.##", symbols)
        return "${df.format(rounded)}%"
    }

    fun formatWholeNumber(value: Long): String {
        val symbols = DecimalFormatSymbols(Locale.US)
        symbols.groupingSeparator = ','
        val df = DecimalFormat("#,##0", symbols)
        return df.format(value)
    }

    fun formatCopySummary(result: PackRatioResult, packageName: String): String {
        val title = packageName.ifBlank { "PackRatio" }
        return """
$title

Bulk: ${formatQuantity(result.bulkQuantityInput)} ${result.bulkUnit.symbol}
Package Size: ${formatQuantity(result.packageQuantityInput)} ${result.packageUnit.symbol}

Complete Packages: ${formatWholeNumber(result.completePackages)}
Packaged: ${formatQuantity(result.packagedQuantity)} ${result.packageUnit.symbol}
Leftover: ${formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol}
Utilization: ${formatPercent(result.utilizationPercent)}

Status: ${result.status.label}
""".trimIndent()
    }

    fun formatShareSummary(result: PackRatioResult, packageName: String): String {
        val title = packageName.ifBlank { "PackRatio" }
        return """
$title

Bulk Quantity: ${formatQuantity(result.bulkQuantityInput)} ${result.bulkUnit.symbol}
Package Size: ${formatQuantity(result.packageQuantityInput)} ${result.packageUnit.symbol}

Complete Packages: ${formatWholeNumber(result.completePackages)}
Packaged Quantity: ${formatQuantity(result.packagedQuantity)} ${result.packageUnit.symbol}
Leftover: ${formatQuantity(result.leftoverQuantity)} ${result.packageUnit.symbol}
Utilization: ${formatPercent(result.utilizationPercent)}

Status: ${result.status.label}
""".trimIndent()
    }
}
