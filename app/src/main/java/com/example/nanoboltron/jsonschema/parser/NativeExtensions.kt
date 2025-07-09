package com.example.nanoboltron.jsonschema.parser

import android.util.Log
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.core.Native
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

private const val TAG = "SchemaPrinter"
fun printUiTree(node: DescriptorNode, indent: String = "") {
    Log.i(TAG, "$indent$node")
    Log.i(TAG, "\n")

    when (node) {
        is DescriptorNode.GroupNode -> {
            node.nodes?.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        else -> Unit
    }
}

private const val TAG2 = "DataPrinter"
fun printUiTree(node: FormDataNode, indent: String = "") {
    Log.i(TAG2, "$indent$node")

    when (node) {
        is FormDataNode.ObjectData -> {
            node.children.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        is FormDataNode.ArrayData-> {
            node.items.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        else -> Unit
    }
}

/**
 * Extension function to find a node by path and key in any Native
 * @param path The path to the parent container in the form "path.to.the.parent" or "path.to.array[0]"
 * @param key The key/name of the node to find within the parent container
 * @return The found JsonNode or null if not found
 */
fun Native.findNodeByPath(path: String?, key: String?): JsonNode? {
    // if key is null, we can't find a specific node
    if (key == null) return null

    // if path is null or empty, search in the current node
    val parentNode = if (path.isNullOrEmpty()) {
        this
    } else {
        // find the parent container first
        findParentNode(path)
    }

    // now find the specific node by key in the parent container
    return when (parentNode) {
        is Native.Object -> {
            parentNode.children?.get(key)
        }

        is Native.Array -> {
            // handle array index notation [0], [1], etc.
            if (key.startsWith("[") && key.endsWith("]")) {
                val indexStr = key.substring(1, key.length - 1)
                val index = indexStr.toIntOrNull()
                if (index != null && index >= 0) {
                    parentNode.children?.getOrNull(index)
                } else {
                    null
                }
            } else {
                null
            }
        }

        else -> null
    }
}

/**
 * Helper function to find the parent node by path
 */
private fun Native.findParentNode(path: String): JsonNode? {
    // parse the path to handle both dot notation and array indices
    val pathParts = parsePathParts(path)

    // start from current node
    var currentNode: JsonNode? = this

    // traverse the path
    for (pathPart in pathParts) {
        when (currentNode) {
            is Native.Object -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    // this shouldn't happen for object nodes
                    return null
                } else {
                    currentNode = currentNode.children?.get(pathPart)
                }
            }

            is Native.Array -> {
                // handle array index notation [0], [1], etc.
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode = currentNode.children?.getOrNull(index)
                    } else {
                        return null
                    }
                } else {
                    return null
                }
            }

            else -> {
                // if we haven't reached the end of the path but current node is not a container
                return null
            }
        }

        // if we couldn't find the next node, return null
        if (currentNode == null) return null
    }

    return currentNode
}

/**
 * Parse path parts to handle both dot notation and array indices
 * Example: "user.items[0].name" -> ["user", "items", "[0]", "name"]
 */
private fun parsePathParts(path: String): List<String> {
    val parts = mutableListOf<String>()
    var current = ""
    var i = 0

    while (i < path.length) {
        val char = path[i]
        when (char) {
            '.' -> {
                if (current.isNotEmpty()) {
                    parts.add(current)
                    current = ""
                }
            }

            '[' -> {
                if (current.isNotEmpty()) {
                    parts.add(current)
                    current = ""
                }
                // find the closing bracket
                val closingBracket = path.indexOf(']', i)
                if (closingBracket != -1) {
                    current = path.substring(i, closingBracket + 1)
                    i = closingBracket
                } else {
                    // malformed path
                    current += char
                }
            }

            else -> {
                current += char
            }
        }
        i++
    }

    if (current.isNotEmpty()) {
        parts.add(current)
    }

    return parts
}
