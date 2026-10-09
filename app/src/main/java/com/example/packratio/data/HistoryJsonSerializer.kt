package com.example.packratio.data

import com.example.packratio.model.PackHistoryItem
import com.example.packratio.model.PackStatus
import com.example.packratio.model.QuantityType
import com.example.packratio.model.QuantityUnit
import java.math.BigDecimal

object HistoryJsonSerializer {

    fun serializeList(items: List<PackHistoryItem>): String {
        val sb = StringBuilder()
        sb.append("[")
        items.forEachIndexed { index, item ->
            if (index > 0) sb.append(",")
            sb.append(serializeItem(item))
        }
        sb.append("]")
        return sb.toString()
    }

    fun serializeItem(item: PackHistoryItem): String {
        return buildString {
            append("{")
            append("\"id\":").append(quote(item.id)).append(",")
            append("\"packageName\":").append(quote(item.packageName)).append(",")
            append("\"notes\":").append(quote(item.notes)).append(",")
            append("\"quantityType\":").append(quote(item.quantityType.name)).append(",")
            append("\"bulkQuantity\":").append(quote(item.bulkQuantity.toPlainString())).append(",")
            append("\"bulkUnit\":").append(quote(item.bulkUnit.name)).append(",")
            append("\"packageQuantity\":").append(quote(item.packageQuantity.toPlainString())).append(",")
            append("\"packageUnit\":").append(quote(item.packageUnit.name)).append(",")
            append("\"completePackageCount\":").append(item.completePackageCount).append(",")
            append("\"packagedQuantity\":").append(quote(item.packagedQuantity.toPlainString())).append(",")
            append("\"leftoverQuantity\":").append(quote(item.leftoverQuantity.toPlainString())).append(",")
            append("\"utilization\":").append(quote(item.utilization.toPlainString())).append(",")
            append("\"leftoverPercent\":").append(quote(item.leftoverPercent.toPlainString())).append(",")
            append("\"status\":").append(quote(item.status.name)).append(",")
            append("\"timestamp\":").append(item.timestamp).append(",")
            append("\"isFavorite\":").append(item.isFavorite)
            append("}")
        }
    }

    fun deserializeList(json: String): List<PackHistoryItem> {
        val trimmed = json.trim()
        if (trimmed.isEmpty() || trimmed == "[]" || !trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            return emptyList()
        }

        val items = mutableListOf<PackHistoryItem>()
        val objects = splitJsonObjects(trimmed.substring(1, trimmed.length - 1))

        for (objStr in objects) {
            deserializeItem(objStr)?.let { items.add(it) }
        }

        return items
    }

    fun deserializeItem(json: String): PackHistoryItem? {
        val trimmed = json.trim()
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) return null

        val map = parseJsonObject(trimmed)
        return try {
            val id = map["id"] ?: return null
            val packageName = map["packageName"] ?: ""
            val notes = map["notes"] ?: ""
            val quantityType = QuantityType.valueOf(map["quantityType"] ?: QuantityType.ITEMS.name)
            val bulkQuantity = BigDecimal(map["bulkQuantity"] ?: "0")
            val bulkUnit = QuantityUnit.valueOf(map["bulkUnit"] ?: QuantityUnit.PIECES.name)
            val packageQuantity = BigDecimal(map["packageQuantity"] ?: "0")
            val packageUnit = QuantityUnit.valueOf(map["packageUnit"] ?: QuantityUnit.PIECES.name)
            val completePackageCount = map["completePackageCount"]?.toLongOrNull() ?: 0L
            val packagedQuantity = BigDecimal(map["packagedQuantity"] ?: "0")
            val leftoverQuantity = BigDecimal(map["leftoverQuantity"] ?: "0")
            val utilization = BigDecimal(map["utilization"] ?: "0")
            val leftoverPercent = BigDecimal(map["leftoverPercent"] ?: "0")
            val status = PackStatus.valueOf(map["status"] ?: PackStatus.FULLY_PACKED.name)
            val timestamp = map["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
            val isFavorite = map["isFavorite"]?.toBooleanStrictOrNull() ?: false

            PackHistoryItem(
                id = id,
                packageName = packageName,
                notes = notes,
                quantityType = quantityType,
                bulkQuantity = bulkQuantity,
                bulkUnit = bulkUnit,
                packageQuantity = packageQuantity,
                packageUnit = packageUnit,
                completePackageCount = completePackageCount,
                packagedQuantity = packagedQuantity,
                leftoverQuantity = leftoverQuantity,
                utilization = utilization,
                leftoverPercent = leftoverPercent,
                status = status,
                timestamp = timestamp,
                isFavorite = isFavorite
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun quote(str: String): String {
        val sb = StringBuilder("\"")
        for (c in str) {
            when (c) {
                '\\' -> sb.append("\\\\")
                '"' -> sb.append("\\\"")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> sb.append(c)
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    private fun splitJsonObjects(content: String): List<String> {
        val result = mutableListOf<String>()
        var depth = 0
        var inString = false
        var escape = false
        var start = -1

        for (i in content.indices) {
            val c = content[i]
            if (escape) {
                escape = false
                continue
            }
            if (c == '\\' && inString) {
                escape = true
                continue
            }
            if (c == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                if (c == '{') {
                    if (depth == 0) start = i
                    depth++
                } else if (c == '}') {
                    depth--
                    if (depth == 0 && start != -1) {
                        result.add(content.substring(start, i + 1))
                        start = -1
                    }
                }
            }
        }
        return result
    }

    private fun unquote(str: String): String {
        val trimmed = str.trim()
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            val content = trimmed.substring(1, trimmed.length - 1)
            val sb = StringBuilder()
            var escape = false
            for (c in content) {
                if (escape) {
                    when (c) {
                        'n' -> sb.append('\n')
                        'r' -> sb.append('\r')
                        't' -> sb.append('\t')
                        '\\' -> sb.append('\\')
                        '"' -> sb.append('"')
                        else -> sb.append(c)
                    }
                    escape = false
                } else if (c == '\\') {
                    escape = true
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }
        return trimmed
    }

    private fun parseJsonObject(content: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val inner = content.trim().removeSurrounding("{", "}").trim()
        if (inner.isEmpty()) return map

        var inString = false
        var escape = false
        var currentToken = StringBuilder()
        var currentKey: String? = null

        for (i in inner.indices) {
            val c = inner[i]
            if (escape) {
                currentToken.append(c)
                escape = false
                continue
            }
            if (c == '\\' && inString) {
                currentToken.append(c)
                escape = true
                continue
            }
            if (c == '"') {
                inString = !inString
                currentToken.append(c)
                continue
            }
            if (!inString) {
                if (c == ':' && currentKey == null) {
                    currentKey = unquote(currentToken.toString().trim())
                    currentToken = StringBuilder()
                    continue
                } else if (c == ',') {
                    if (currentKey != null) {
                        map[currentKey] = unquote(currentToken.toString().trim())
                        currentKey = null
                        currentToken = StringBuilder()
                    }
                    continue
                }
            }
            currentToken.append(c)
        }

        if (currentKey != null) {
            map[currentKey] = unquote(currentToken.toString().trim())
        }

        return map
    }
}
