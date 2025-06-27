package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.NodeTraverser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import org.junit.Test
import org.junit.Assert.*

class NodeTraverserTest {

    private val parser = JsonSchemaParser()
    private val traverser = NodeTraverser()

    @Test
    fun testSimpleSchemaTraversal() {
        val schema = """
        {
            "properties": {
                "name": {"type": "string", "title": "Name"},
                "age": {"type": "number", "title": "Age"},
                "active": {"type": "boolean", "title": "Active"}
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!
        val allPaths = traverser.getAllPaths(rootNode)

        println("=== Simple Schema Traversal ===")
        allPaths.forEach { nodePath ->
            println("Path: ${nodePath.path} | Key: ${nodePath.key} | Type: ${nodePath.nodeType} | Depth: ${nodePath.depth}")
        }
        println("================================")

        // Verify structure
        assertEquals(4, allPaths.size) // root + 3 properties
        assertEquals(0, allPaths[0].depth) // root at depth 0
        assertEquals(1, allPaths[1].depth) // properties at depth 1
    }

    @Test
    fun testCompositionSchemaTraversal() {
        val schema = """
        {
            "anyOf": [
                {"type": "string"},
                {"type": "number"},
                {
                    "properties": {
                        "nested": {"type": "boolean"}
                    }
                }
            ]
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!
        val allPaths = traverser.getAllPaths(rootNode)

        println("=== Composition Schema Traversal ===")
        println(traverser.printTree(rootNode))
        println("====================================")

        // Test specific queries
        val compositionNodes = traverser.getCompositionNodes(rootNode)
        assertEquals(1, compositionNodes.size)
        assertEquals(
            "anyOf",
            (compositionNodes[0].node as DescriptorNode.CompositionNode).compositionType
        )

        val leafNodes = traverser.getLeafNodes(rootNode)
        assertEquals(3, leafNodes.size) // string, number, and boolean nodes

        val maxDepth = traverser.getMaxDepth(rootNode)
        assertTrue(maxDepth >= 2) // At least 2 levels deep
    }

    @Test
    fun testConditionalSchemaTraversal() {
        val schema = """
        {
            "properties": {
                "shipping": {
                    "if": {
                        "properties": {
                            "country": {"const": "US"}
                        }
                    },
                    "then": {
                        "properties": {
                            "zipCode": {"type": "string"}
                        }
                    },
                    "else": {
                        "properties": {
                            "postalCode": {"type": "string"}
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!
        val allPaths = traverser.getAllPaths(rootNode)

        println("=== Conditional Schema Traversal ===")
        println(traverser.printTree(rootNode))
        println("====================================")

        // Test conditional node detection
        val conditionalNodes = traverser.getConditionalNodes(rootNode)
        assertEquals(1, conditionalNodes.size)

        val conditionalNode = conditionalNodes[0].node as DescriptorNode.ConditionalNode
        assertNotNull(conditionalNode.ifSchema)
        assertNotNull(conditionalNode.thenSchema)
        assertNotNull(conditionalNode.elseSchema)

        // Test depth queries
        val depthOneNodes = traverser.getNodesAtDepth(rootNode, 1)
        assertTrue(depthOneNodes.isNotEmpty())
    }

    @Test
    fun testComplexNestedSchemaTraversal() {
        val schema = """
        {
            "properties": {
                "user": {
                    "allOf": [
                        {
                            "properties": {
                                "personalInfo": {
                                    "anyOf": [
                                        {
                                            "properties": {
                                                "firstName": {"type": "string"},
                                                "lastName": {"type": "string"}
                                            }
                                        },
                                        {
                                            "properties": {
                                                "displayName": {"type": "string"}
                                            }
                                        }
                                    ]
                                }
                            }
                        },
                        {
                            "properties": {
                                "contact": {
                                    "oneOf": [
                                        {"properties": {"email": {"type": "string"}}},
                                        {"properties": {"phone": {"type": "string"}}}
                                    ]
                                }
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!
        val allPaths = traverser.getAllPaths(rootNode)

        println("=== Complex Nested Schema Traversal ===")
        println(traverser.printTree(rootNode))
        println("=======================================")

        // Verify complex structure
        val maxDepth = traverser.getMaxDepth(rootNode)
        println("Max depth: $maxDepth")
        assertTrue(maxDepth >= 3) // Should be deeply nested

        // Count different node types
        val compositionNodes = traverser.getCompositionNodes(rootNode)
        val leafNodes = traverser.getLeafNodes(rootNode)

        println("Composition nodes: ${compositionNodes.size}")
        println("Leaf nodes: ${leafNodes.size}")

        assertTrue(compositionNodes.size >= 2) // Should have multiple compositions
        assertTrue(leafNodes.size >= 4) // Should have multiple leaf nodes
    }

    @Test
    fun testPathFinding() {
        val schema = """
        {
            "properties": {
                "address": {
                    "properties": {
                        "street": {"type": "string"},
                        "city": {"type": "string"}
                    }
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!

        // Test finding specific paths
        val allPaths = traverser.getAllPaths(rootNode)

        println("=== Path Finding Test ===")
        allPaths.forEach { nodePath ->
            println("Found path: ${nodePath.path} -> ${nodePath.nodeType}")
        }
        println("=========================")

        // Test path-based node finding
        val addressPath = allPaths.find { it.key == "address" }
        assertNotNull("Address node should be found", addressPath)

        // Test finding by path
        val foundNode = traverser.findNodeByPath(rootNode, addressPath!!.path)
        assertNotNull("Should find node by path", foundNode)
        assertEquals(addressPath.nodeType, foundNode!!.nodeType)
    }

    @Test
    fun testTraverserStatistics() {
        val schema = """
        {
            "oneOf": [
                {
                    "properties": {
                        "type": {"const": "user"},
                        "data": {
                            "allOf": [
                                {"properties": {"name": {"type": "string"}}},
                                {"properties": {"email": {"type": "string"}}}
                            ]
                        }
                    }
                },
                {
                    "properties": {
                        "type": {"const": "admin"},
                        "data": {
                            "properties": {
                                "permissions": {
                                    "type": "array",
                                    "items": {"type": "string"}
                                }
                            }
                        }
                    }
                }
            ]
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!!

        println("=== Traverser Statistics ===")
        println("Total nodes: ${traverser.getAllPaths(rootNode).size}")
        println("Leaf nodes: ${traverser.getLeafNodes(rootNode).size}")
        println("Composition nodes: ${traverser.getCompositionNodes(rootNode).size}")
        println("Conditional nodes: ${traverser.getConditionalNodes(rootNode).size}")
        println("Max depth: ${traverser.getMaxDepth(rootNode)}")

        // Print tree structure
        println("\nTree structure:")
        println(traverser.printTree(rootNode))
        println("=============================")

        // Verify statistics make sense
        val totalNodes = traverser.getAllPaths(rootNode).size
        val leafNodes = traverser.getLeafNodes(rootNode).size
        val compositionNodes = traverser.getCompositionNodes(rootNode).size

        assertTrue("Should have nodes", totalNodes > 0)
        assertTrue("Should have leaf nodes", leafNodes > 0)
        assertTrue("Should have composition nodes", compositionNodes > 0)
        assertTrue("Leaf nodes should be subset of total", leafNodes <= totalNodes)
    }
}