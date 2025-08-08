package com.example.nanoboltron.jsonschema.core

import com.example.nanoboltron.jsonschema.core.Native.Array
import com.example.nanoboltron.jsonschema.core.Native.Object
import com.example.nanoboltron.jsonschema.core.DescriptorNode

/**
 * Extension function to find a node by path and key in any JsonNode
 * @param path The path to the parent container in the form "path.to.the.parent" or "path.to.array[0]"
 * @param key The key/name of the node to find within the parent container
 * @return The found JsonNode or null if not found
 */
fun JsonNode.findNodeByPath(path: String?, key: String?): JsonNode? {
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
        is Object -> {
            parentNode.children?.get(key)
        }

        is Array -> {
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

        is DescriptorNode.GroupNode -> {
            parentNode.nodes?.find { it.key == key }
        }

        is DescriptorNode.CompositionNode -> {
            parentNode.schemas.find { it.key == key }
        }

        else -> null
    }
}

/**
 * Extension function to find a node by its complete path
 * @param targetPath The complete path to the node in the form "path.to.the.node"
 * @return The found JsonNode or null if not found
 */
fun JsonNode.findNodeByPath(targetPath: String): JsonNode? {
    if (targetPath.isEmpty()) return this

    val pathParts = parsePathParts(targetPath)
    var currentNode: JsonNode? = this

    for (pathPart in pathParts) {
        currentNode = when (currentNode) {
            is Object -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    null // Objects don't have array indices
                } else {
                    currentNode.children?.get(pathPart)
                }
            }

            is Array -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.children?.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            is DescriptorNode.GroupNode -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.nodes?.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    currentNode.nodes?.find { it.key == pathPart }
                }
            }

            is DescriptorNode.CompositionNode -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.schemas.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    currentNode.schemas.find { it.key == pathPart }
                }
            }

            is DescriptorNode.ConditionalNode -> {
                when (pathPart) {
                    "if" -> currentNode.ifSchema
                    "then" -> currentNode.thenSchema
                    "else" -> currentNode.elseSchema
                    else -> null
                }
            }

            else -> null
        }

        if (currentNode == null) return null
    }

    return currentNode
}

/**
 * Extension function to get all child nodes regardless of the node type
 * @return List of child JsonNodes
 */
fun JsonNode.getChildren(): List<JsonNode> {
    return when (this) {
        is Object -> children?.values?.toList() ?: emptyList()
        is Array -> children ?: emptyList()
        is DescriptorNode.GroupNode -> nodes ?: emptyList()
        is DescriptorNode.CompositionNode -> schemas
        is DescriptorNode.ConditionalNode -> listOfNotNull(ifSchema, thenSchema, elseSchema)
        else -> emptyList()
    }
}

/**
 * Extension function to traverse all nodes in the tree
 * @param action Function to execute on each node
 */
fun JsonNode.traverse(action: (JsonNode) -> Unit) {
    action(this)
    getChildren().forEach { it.traverse(action) }
}

/**
 * Extension function to find all nodes matching a predicate
 * @param predicate Function to test each node
 * @return List of matching nodes
 */
fun JsonNode.findAll(predicate: (JsonNode) -> Boolean): List<JsonNode> {
    val result = mutableListOf<JsonNode>()
    traverse { node ->
        if (predicate(node)) {
            result.add(node)
        }
    }
    return result
}

/**
 * Helper function to find the parent node by path
 */
private fun JsonNode.findParentNode(path: String): JsonNode? {
    val pathParts = parsePathParts(path)
    var currentNode: JsonNode? = this

    for (pathPart in pathParts) {
        currentNode = when (currentNode) {
            is Object -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    null // Objects don't have array indices
                } else {
                    currentNode.children?.get(pathPart)
                }
            }

            is Array -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.children?.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    null
                }
            }

            is DescriptorNode.GroupNode -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.nodes?.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    currentNode.nodes?.find { it.key == pathPart }
                }
            }

            is DescriptorNode.CompositionNode -> {
                if (pathPart.startsWith("[") && pathPart.endsWith("]")) {
                    val indexStr = pathPart.substring(1, pathPart.length - 1)
                    val index = indexStr.toIntOrNull()
                    if (index != null && index >= 0) {
                        currentNode.schemas.getOrNull(index)
                    } else {
                        null
                    }
                } else {
                    currentNode.schemas.find { it.key == pathPart }
                }
            }

            is DescriptorNode.ConditionalNode -> {
                when (pathPart) {
                    "if" -> currentNode.ifSchema
                    "then" -> currentNode.thenSchema
                    "else" -> currentNode.elseSchema
                    else -> null
                }
            }

            else -> null
        }

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