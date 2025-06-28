package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

/**
 * Component responsible for traversing DescriptorNode trees and collecting information
 * about nodes and their paths.
 */
class NodeTraverser {

    /**
     * Data class representing a node and its path in the tree
     */
    data class NodePath(
        val node: DescriptorNode,
        val path: String,
        val key: String?,
        val depth: Int,
        val nodeType: String
    )

    /**
     * Traverses the entire tree and applies a transformation to each node.
     * Similar to Kotlin's map function but for descriptor node trees.
     * Returns a list of transformation results.
     */
    fun <T> mapNodes(
        rootNode: DescriptorNode,
        transform: (node: DescriptorNode, path: String, key: String?, depth: Int) -> T
    ): List<T> {
        val results = mutableListOf<T>()
        traverseAndTransform(rootNode, "", null, 0, transform, results)
        return results
    }

    /**
     * Traverses the tree and applies a transformation, but only returns non-null results.
     * Useful when you want to filter and transform in one operation.
     */
    fun <T> mapNotNull(
        rootNode: DescriptorNode,
        transform: (node: DescriptorNode, path: String, key: String?, depth: Int) -> T?
    ): List<T> {
        val results = mutableListOf<T>()
        traverseAndTransformNotNull(rootNode, "", null, 0, transform, results)
        return results
    }

    /**
     * Traverses the tree and applies a side-effect operation to each node.
     * Returns Unit - used when you want to perform operations on nodes without collecting results.
     */
    fun forEachNode(
        rootNode: DescriptorNode,
        action: (node: DescriptorNode, path: String, key: String?, depth: Int) -> Unit
    ) {
        traverseAndApply(rootNode, "", null, 0, action)
    }

    /**
     * Traverses the tree and applies a conditional transformation.
     * Only transforms nodes that match the predicate.
     */
    fun <T> mapWhere(
        rootNode: DescriptorNode,
        predicate: (node: DescriptorNode, path: String, key: String?, depth: Int) -> Boolean,
        transform: (node: DescriptorNode, path: String, key: String?, depth: Int) -> T
    ): List<T> {
        val results = mutableListOf<T>()
        traverseAndTransformWhere(rootNode, "", null, 0, predicate, transform, results)
        return results
    }

    /**
     * Finds the first node that matches the predicate and applies transformation.
     * Returns null if no matching node is found.
     */
    fun <T> findAndTransform(
        rootNode: DescriptorNode,
        predicate: (node: DescriptorNode, path: String, key: String?, depth: Int) -> Boolean,
        transform: (node: DescriptorNode, path: String, key: String?, depth: Int) -> T
    ): T? {
        return findFirstMatch(rootNode, "", null, 0, predicate, transform)
    }

    /**
     * Traverses the entire node tree and returns all nodes with their paths
     */
    fun getAllPaths(rootNode: DescriptorNode): List<NodePath> {
        val paths = mutableListOf<NodePath>()
        traverseNode(rootNode, "", null, 0, paths)
        return paths
    }

    /**
     * Gets all leaf nodes (nodes with no children)
     */
    fun getLeafNodes(rootNode: DescriptorNode): List<NodePath> {
        return getAllPaths(rootNode).filter { nodePath ->
            when (val node = nodePath.node) {
                is DescriptorNode.StringNode,
                is DescriptorNode.NumberNode,
                is DescriptorNode.BooleanNode -> true

                is DescriptorNode.GroupNode -> node.nodes.isNullOrEmpty()
                is DescriptorNode.CompositionNode -> node.schemas.isEmpty()
                is DescriptorNode.ConditionalNode -> false // Conditional nodes are not leaf nodes
            }
        }
    }

    /**
     * Gets all composition nodes (allOf, anyOf, oneOf)
     */
    fun getCompositionNodes(rootNode: DescriptorNode): List<NodePath> {
        return getAllPaths(rootNode).filter { it.node is DescriptorNode.CompositionNode }
    }

    /**
     * Gets all conditional nodes (if-then-else)
     */
    fun getConditionalNodes(rootNode: DescriptorNode): List<NodePath> {
        return getAllPaths(rootNode).filter { it.node is DescriptorNode.ConditionalNode }
    }

    /**
     * Gets all nodes at a specific depth level
     */
    fun getNodesAtDepth(rootNode: DescriptorNode, targetDepth: Int): List<NodePath> {
        return getAllPaths(rootNode).filter { it.depth == targetDepth }
    }

    /**
     * Finds a node by its path
     */
    fun findNodeByPath(rootNode: DescriptorNode, targetPath: String): NodePath? {
        return getAllPaths(rootNode).firstOrNull { it.path == targetPath }
    }

    /**
     * Gets the maximum depth of the tree
     */
    fun getMaxDepth(rootNode: DescriptorNode): Int {
        return getAllPaths(rootNode).maxOfOrNull { it.depth } ?: 0
    }

    /**
     * Prints a tree-like structure of all nodes
     */
    fun printTree(rootNode: DescriptorNode): String {
        val paths = getAllPaths(rootNode)
        return buildString {
            paths.forEach { nodePath ->
                val indent = "  ".repeat(nodePath.depth)
                val key = nodePath.key?.let { "[$it]" } ?: "[root]"
                appendLine("$indent$key ${nodePath.nodeType} -> ${nodePath.path}")
            }
        }
    }

    // Private helper methods for the traversal operations

    private fun <T> traverseAndTransform(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        transform: (DescriptorNode, String, String?, Int) -> T,
        results: MutableList<T>
    ) {
        val fullPath = getFullPath(node, currentPath)

        // Apply transformation to current node
        results.add(transform(node, fullPath, key, depth))

        // Traverse children
        traverseChildren(node, fullPath, depth + 1) { childNode, childPath, childKey, childDepth ->
            traverseAndTransform(childNode, childPath, childKey, childDepth, transform, results)
        }
    }

    private fun <T> traverseAndTransformNotNull(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        transform: (DescriptorNode, String, String?, Int) -> T?,
        results: MutableList<T>
    ) {
        val fullPath = getFullPath(node, currentPath)

        // Apply transformation to current node, add only if not null
        transform(node, fullPath, key, depth)?.let { results.add(it) }

        // Traverse children
        traverseChildren(node, fullPath, depth + 1) { childNode, childPath, childKey, childDepth ->
            traverseAndTransformNotNull(
                childNode,
                childPath,
                childKey,
                childDepth,
                transform,
                results
            )
        }
    }

    private fun traverseAndApply(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        action: (DescriptorNode, String, String?, Int) -> Unit
    ) {
        val fullPath = getFullPath(node, currentPath)

        // Apply action to current node
        action(node, fullPath, key, depth)

        // Traverse children
        traverseChildren(node, fullPath, depth + 1) { childNode, childPath, childKey, childDepth ->
            traverseAndApply(childNode, childPath, childKey, childDepth, action)
        }
    }

    private fun <T> traverseAndTransformWhere(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        predicate: (DescriptorNode, String, String?, Int) -> Boolean,
        transform: (DescriptorNode, String, String?, Int) -> T,
        results: MutableList<T>
    ) {
        val fullPath = getFullPath(node, currentPath)

        // Apply transformation only if predicate matches
        if (predicate(node, fullPath, key, depth)) {
            results.add(transform(node, fullPath, key, depth))
        }

        // Traverse children
        traverseChildren(node, fullPath, depth + 1) { childNode, childPath, childKey, childDepth ->
            traverseAndTransformWhere(
                childNode,
                childPath,
                childKey,
                childDepth,
                predicate,
                transform,
                results
            )
        }
    }

    private fun <T> findFirstMatch(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        predicate: (DescriptorNode, String, String?, Int) -> Boolean,
        transform: (DescriptorNode, String, String?, Int) -> T
    ): T? {
        val fullPath = getFullPath(node, currentPath)

        // Check if current node matches
        if (predicate(node, fullPath, key, depth)) {
            return transform(node, fullPath, key, depth)
        }

        // Search in children
        when (node) {
            is DescriptorNode.GroupNode -> {
                node.nodes?.forEach { childNode ->
                    val childKey = childNode.key
                    val childPath = if (fullPath == "root") {
                        childKey ?: "unknown"
                    } else {
                        childNode.path ?: "$fullPath.${childKey ?: "unknown"}"
                    }
                    findFirstMatch(
                        childNode,
                        childPath,
                        childKey,
                        depth + 1,
                        predicate,
                        transform
                    )?.let {
                        return it
                    }
                }
            }

            is DescriptorNode.CompositionNode -> {
                node.schemas.forEachIndexed { index, childNode ->
                    val compositionType = node.compositionType
                    val childPath = "$fullPath.$compositionType.$index"
                    findFirstMatch(
                        childNode,
                        childPath,
                        null,
                        depth + 1,
                        predicate,
                        transform
                    )?.let {
                        return it
                    }
                }
            }

            is DescriptorNode.ConditionalNode -> {
                node.ifSchema?.let { ifNode ->
                    findFirstMatch(
                        ifNode,
                        "$fullPath.if",
                        null,
                        depth + 1,
                        predicate,
                        transform
                    )?.let {
                        return it
                    }
                }
                node.thenSchema?.let { thenNode ->
                    findFirstMatch(
                        thenNode,
                        "$fullPath.then",
                        null,
                        depth + 1,
                        predicate,
                        transform
                    )?.let {
                        return it
                    }
                }
                node.elseSchema?.let { elseNode ->
                    findFirstMatch(
                        elseNode,
                        "$fullPath.else",
                        null,
                        depth + 1,
                        predicate,
                        transform
                    )?.let {
                        return it
                    }
                }
            }

            // Leaf nodes have no children
            else -> {}
        }

        return null
    }

    private fun traverseChildren(
        node: DescriptorNode,
        fullPath: String,
        childDepth: Int,
        action: (DescriptorNode, String, String?, Int) -> Unit
    ) {
        when (node) {
            is DescriptorNode.GroupNode -> {
                node.nodes?.forEach { childNode ->
                    val childKey = childNode.key
                    val childPath = if (fullPath == "root") {
                        childKey ?: "unknown"
                    } else {
                        childNode.path ?: "$fullPath.${childKey ?: "unknown"}"
                    }
                    action(childNode, childPath, childKey, childDepth)
                }
            }

            is DescriptorNode.CompositionNode -> {
                node.schemas.forEachIndexed { index, childNode ->
                    val compositionType = node.compositionType
                    val childPath = "$fullPath.$compositionType.$index"
                    action(childNode, childPath, null, childDepth)
                }
            }

            is DescriptorNode.ConditionalNode -> {
                node.ifSchema?.let { ifNode ->
                    action(ifNode, "$fullPath.if", null, childDepth)
                }
                node.thenSchema?.let { thenNode ->
                    action(thenNode, "$fullPath.then", null, childDepth)
                }
                node.elseSchema?.let { elseNode ->
                    action(elseNode, "$fullPath.else", null, childDepth)
                }
            }

            // Leaf nodes have no children
            else -> {}
        }
    }

    private fun traverseNode(
        node: DescriptorNode,
        currentPath: String,
        key: String?,
        depth: Int,
        paths: MutableList<NodePath>
    ) {
        // Add current node to paths
        val nodeType = getNodeTypeName(node)
        val fullPath = if (currentPath.isEmpty()) {
            node.path ?: "root"
        } else {
            node.path ?: currentPath
        }

        paths.add(NodePath(node, fullPath, key, depth, nodeType))

        // Traverse children based on node type
        when (node) {
            is DescriptorNode.GroupNode -> {
                node.nodes?.forEach { childNode ->
                    val childKey = childNode.key
                    val childPath = if (fullPath == "root") {
                        childKey ?: "unknown"
                    } else {
                        childNode.path ?: "$fullPath.${childKey ?: "unknown"}"
                    }
                    traverseNode(childNode, childPath, childKey, depth + 1, paths)
                }
            }

            is DescriptorNode.CompositionNode -> {
                node.schemas.forEachIndexed { index, childNode ->
                    val compositionType = node.compositionType
                    val childPath = "$fullPath.$compositionType.$index"
                    traverseNode(childNode, childPath, null, depth + 1, paths)
                }
            }

            is DescriptorNode.ConditionalNode -> {
                node.ifSchema?.let { ifNode ->
                    traverseNode(ifNode, "$fullPath.if", null, depth + 1, paths)
                }
                node.thenSchema?.let { thenNode ->
                    traverseNode(thenNode, "$fullPath.then", null, depth + 1, paths)
                }
                node.elseSchema?.let { elseNode ->
                    traverseNode(elseNode, "$fullPath.else", null, depth + 1, paths)
                }
            }

            // Leaf nodes (StringNode, NumberNode, BooleanNode) have no children
            is DescriptorNode.StringNode,
            is DescriptorNode.NumberNode,
            is DescriptorNode.BooleanNode -> {
                // No children to traverse
            }
        }
    }

    private fun getNodeTypeName(node: DescriptorNode): String {
        return when (node) {
            is DescriptorNode.GroupNode -> "GroupNode(${node.type})"
            is DescriptorNode.StringNode -> "StringNode"
            is DescriptorNode.NumberNode -> "NumberNode"
            is DescriptorNode.BooleanNode -> "BooleanNode"
            is DescriptorNode.CompositionNode -> "CompositionNode(${node.compositionType})"
            is DescriptorNode.ConditionalNode -> "ConditionalNode"
        }
    }

    private fun getFullPath(node: DescriptorNode, currentPath: String): String {
        return if (currentPath.isEmpty()) {
            node.path ?: "root"
        } else {
            node.path ?: currentPath
        }
    }
}
