package com.example.packratio.model

import java.math.BigDecimal

data class PackRatioInput(
    val packageName: String = "",
    val notes: String = "",
    val quantityType: QuantityType = QuantityType.ITEMS,
    val bulkQuantity: BigDecimal = BigDecimal.ZERO,
    val bulkUnit: QuantityUnit = QuantityUnit.PIECES,
    val packageQuantity: BigDecimal = BigDecimal.ZERO,
    val packageUnit: QuantityUnit = QuantityUnit.PIECES
)
