package com.example.packratio.model

import java.math.BigDecimal
import java.util.UUID

data class PackHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String,
    val notes: String = "",
    val quantityType: QuantityType,
    val bulkQuantity: BigDecimal,
    val bulkUnit: QuantityUnit,
    val packageQuantity: BigDecimal,
    val packageUnit: QuantityUnit,
    val completePackageCount: Long,
    val packagedQuantity: BigDecimal,
    val leftoverQuantity: BigDecimal,
    val utilization: BigDecimal,
    val leftoverPercent: BigDecimal,
    val status: PackStatus,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false
)
