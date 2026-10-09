package com.example.packratio.model

enum class QuantityUnit(
    val type: QuantityType,
    val symbol: String,
    val label: String,
    val description: String
) {
    // Items
    PIECES(QuantityType.ITEMS, "pieces", "Pieces", "Pieces (Count)"),

    // Weight
    MG(QuantityType.WEIGHT, "mg", "Milligrams", "Milligrams (mg)"),
    G(QuantityType.WEIGHT, "g", "Grams", "Grams (g)"),
    KG(QuantityType.WEIGHT, "kg", "Kilograms", "Kilograms (kg)"),
    OZ(QuantityType.WEIGHT, "oz", "Ounces", "Ounces (oz)"),
    LB(QuantityType.WEIGHT, "lb", "Pounds", "Pounds (lb)"),

    // Volume (US Customary & Metric)
    ML(QuantityType.VOLUME, "ml", "Milliliters", "Milliliters (ml)"),
    L(QuantityType.VOLUME, "L", "Liters", "Liters (L)"),
    FL_OZ(QuantityType.VOLUME, "fl oz", "US Fluid Ounces", "US Fluid Ounces (fl oz)"),
    US_GAL(QuantityType.VOLUME, "gal", "US Gallons", "US Gallons (gal)");

    companion object {
        fun defaultForType(type: QuantityType): QuantityUnit = when (type) {
            QuantityType.ITEMS -> PIECES
            QuantityType.WEIGHT -> KG
            QuantityType.VOLUME -> L
        }

        fun unitsForType(type: QuantityType): List<QuantityUnit> =
            entries.filter { it.type == type }
    }
}
