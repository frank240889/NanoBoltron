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
}