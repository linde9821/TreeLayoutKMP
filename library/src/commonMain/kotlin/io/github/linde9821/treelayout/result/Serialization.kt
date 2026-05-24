package io.github.linde9821.treelayout.result

import io.github.linde9821.treelayout.Point

/**
 * Serializes a [TreeLayoutResult] to a JSON string.
 *
 * @param nodeToKey Converts a node to its string key for serialization.
 * @return A JSON string representing the layout result.
 */
public fun <T> TreeLayoutResult<T>.toJson(nodeToKey: (T) -> String): String {
    val entries = getPositions().entries.joinToString(",") { (node, point) ->
        val key = nodeToKey(node).escapeJson()
        """{"k":"$key","px":${point.x},"py":${point.y}}"""
    }
    return """{"maxDepth":${getMaxDepth()},"positions":[$entries]}"""
}

/**
 * Deserializes a [TreeLayoutResult] from a JSON string previously produced by [toJson].
 *
 * @param json The JSON string to parse.
 * @param keyToNode Converts a string key back to the node instance.
 * @return The deserialized [TreeLayoutResult].
 */
public fun <T> TreeLayoutResult.Companion.fromJson(
    json: String,
    keyToNode: (String) -> T,
): TreeLayoutResult<T> {
    val maxDepth = json.extractInt("maxDepth")
    val positions = HashMap<T, Point>()
    val positionsArray = json.substringAfter("\"positions\":[").substringBeforeLast("]")

    if (positionsArray.isNotBlank()) {
        var remaining = positionsArray
        while (remaining.isNotBlank()) {
            val obj = remaining.substringAfter("{").substringBefore("}")
            remaining = remaining.substringAfter("}")
            if (remaining.startsWith(",")) remaining = remaining.substring(1)

            val key = obj.extractString("k").unescapeJson()
            val x = obj.extractFloat("px")
            val y = obj.extractFloat("py")
            positions[keyToNode(key)] = Point(x, y)
        }
    }

    return TreeLayoutResult(positions, maxDepth)
}

private fun String.extractInt(key: String): Int {
    val after = substringAfter("\"$key\":")
    return after.takeWhile { it.isDigit() || it == '-' }.toInt()
}

private fun String.extractFloat(key: String): Float {
    val after = substringAfter("\"$key\":")
    return after.takeWhile { it.isDigit() || it == '-' || it == '.' || it == 'E' || it == 'e' || it == '+' }.toFloat()
}

private fun String.extractString(key: String): String {
    val after = substringAfter("\"$key\":\"")
    val sb = StringBuilder()
    var i = 0
    while (i < after.length) {
        if (after[i] == '\\' && i + 1 < after.length) {
            sb.append(after[i])
            sb.append(after[i + 1])
            i += 2
        } else if (after[i] == '"') {
            break
        } else {
            sb.append(after[i])
            i++
        }
    }
    return sb.toString()
}

private fun String.escapeJson(): String = replace("\\", "\\\\").replace("\"", "\\\"")

private fun String.unescapeJson(): String = replace("\\\"", "\"").replace("\\\\", "\\")
