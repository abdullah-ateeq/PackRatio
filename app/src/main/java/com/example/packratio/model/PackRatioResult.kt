package com.example.packratio.model

import java.math.BigDecimal

data class PackRatioResult(
    val completePackages: Long,
    val normalizedBulkQuantity: BigDecimal,
    val normalizedPackageQuantity: BigDecimal,
    val packagedQuantity: BigDecimal,
    val leftoverQuantity: BigDecimal,
    val utilizationPercent: BigDecimal,
    val leftoverPercent: BigDecimal,
    val status: PackStatus,
    val statusExplanation: String,
    val bulkUnit: QuantityUnit,
    val packageUnit: QuantityUnit,
    val bulkQuantityInput: BigDecimal,
    val packageQuantityInput: BigDecimal,
    val packagedNormalized: BigDecimal,
    val leftoverNormalized: BigDecimal,
    val formulaExplanation: String = ""
)
