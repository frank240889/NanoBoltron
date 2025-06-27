package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import org.junit.Test
import org.junit.Assert.*

class JsonSchemaParserTest {

    private val parser = JsonSchemaParser()

    @Test
    fun testSimpleObjectSchema() {
        val schema = """
        {
            "type": "object",
            "properties": {
                "name": {"type": "string"},
                "age": {"type": "number"}
            }
        }
        """.trimIndent()

        val result = parser.parse(schema)

        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        val groupNode = result as DescriptorNode.GroupNode
        assertEquals(2, groupNode.nodes?.size)
    }

    @Test
    fun testRootSchemaWithoutType() {
        val schema = """
        {
            "properties": {
                "username": {"type": "string"},
                "active": {"type": "boolean"}
            }
        }
        """.trimIndent()

        val result = parser.parse(schema)

        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        val groupNode = result as DescriptorNode.GroupNode
        assertEquals("object", groupNode.type)
        assertEquals(2, groupNode.nodes?.size)
    }

    @Test
    fun testArraySchema() {
        val schema = """
        {
            "type": "array",
            "items": {
                "type": "string"
            }
        }
        """.trimIndent()

        val result = parser.parse(schema)

        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        val groupNode = result as DescriptorNode.GroupNode
        assertEquals("repeatable", groupNode.type)
        assertEquals(1, groupNode.nodes?.size)
    }

    @Test
    fun testToStringOutput() {
        val schema = """
        {
            "properties": {
                "user": {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string", "title": "Full Name"},
                        "age": {"type": "number", "title": "Age"}
                    }
                },
                "active": {"type": "boolean", "title": "Is Active"}
            }
        }
        """.trimIndent()

        val result = parser.parse(schema)

        assertNotNull(result)

        // Print the beautiful output
        println("=== JSON Schema Parser Output ===")
        println(result.toString())
        println("=================================")
    }

    @Test
    fun testPrimitiveRootNodes() {
        // Test string root
        val stringSchema = """{"type": "string", "title": "Name"}"""
        val stringResult = parser.parse(stringSchema)
        assertNotNull(stringResult)
        assertTrue(stringResult is DescriptorNode.StringNode)
        assertEquals("Name", (stringResult as DescriptorNode.StringNode).title)

        // Test number root
        val numberSchema = """{"type": "number", "title": "Age"}"""
        val numberResult = parser.parse(numberSchema)
        assertNotNull(numberResult)
        assertTrue(numberResult is DescriptorNode.NumberNode)
        assertEquals("Age", (numberResult as DescriptorNode.NumberNode).title)

        // Test boolean root
        val booleanSchema = """{"type": "boolean", "title": "Active"}"""
        val booleanResult = parser.parse(booleanSchema)
        assertNotNull(booleanResult)
        assertTrue(booleanResult is DescriptorNode.BooleanNode)
        assertEquals("Active", (booleanResult as DescriptorNode.BooleanNode).title)
    }

    @Test
    fun testInferredPrimitiveTypes() {
        // Since we removed validation keywords, test simple type inference
        // Without explicit type or structural indicators, should default to object
        val simpleSchema = """{"title": "Simple"}"""
        val simpleResult = parser.parse(simpleSchema)
        assertNotNull(simpleResult)
        assertTrue(simpleResult is DescriptorNode.GroupNode)
        assertEquals("object", (simpleResult as DescriptorNode.GroupNode).type)
    }

    @Test
    fun testSpecialNodesOnly() {
        // Test that our special composition nodes work correctly

        // Test allOf
        val allOfSchema = """
        {
            "allOf": [
                {"properties": {"name": {"type": "string"}}},
                {"properties": {"age": {"type": "number"}}}
            ]
        }
        """.trimIndent()

        val allOfResult = parser.parse(allOfSchema)
        assertNotNull(allOfResult)
        // Should be merged into a single object
        assertTrue(allOfResult is DescriptorNode.GroupNode)

        // Test anyOf
        val anyOfSchema = """
        {
            "anyOf": [
                {"type": "string"},
                {"type": "number"}
            ]
        }
        """.trimIndent()

        val anyOfResult = parser.parse(anyOfSchema)
        assertNotNull(anyOfResult)
        assertTrue(anyOfResult is DescriptorNode.CompositionNode)
        assertEquals("anyOf", (anyOfResult as DescriptorNode.CompositionNode).compositionType)

        // Test oneOf
        val oneOfSchema = """
        {
            "oneOf": [
                {"type": "string"},
                {"type": "boolean"}
            ]
        }
        """.trimIndent()

        val oneOfResult = parser.parse(oneOfSchema)
        assertNotNull(oneOfResult)
        assertTrue(oneOfResult is DescriptorNode.CompositionNode)
        assertEquals("oneOf", (oneOfResult as DescriptorNode.CompositionNode).compositionType)

        // Test if-then-else
        val ifThenElseSchema = """
        {
            "if": {"properties": {"country": {"const": "US"}}},
            "then": {"properties": {"zipCode": {"type": "string"}}},
            "else": {"properties": {"postalCode": {"type": "string"}}}
        }
        """.trimIndent()

        val ifThenElseResult = parser.parse(ifThenElseSchema)
        assertNotNull(ifThenElseResult)
        assertTrue(ifThenElseResult is DescriptorNode.ConditionalNode)

        val conditionalNode = ifThenElseResult as DescriptorNode.ConditionalNode
        assertNotNull(conditionalNode.ifSchema)
        assertNotNull(conditionalNode.thenSchema)
        assertNotNull(conditionalNode.elseSchema)

        println("=== Special Nodes Test Complete ===")
        println("✅ allOf: ${if (allOfResult is DescriptorNode.GroupNode) "Merged" else "Composition"}")
        println("✅ anyOf: ${if (anyOfResult is DescriptorNode.CompositionNode) "CompositionNode" else "Other"}")
        println("✅ oneOf: ${if (oneOfResult is DescriptorNode.CompositionNode) "CompositionNode" else "Other"}")
        println("✅ if-then-else: ${if (ifThenElseResult is DescriptorNode.ConditionalNode) "ConditionalNode" else "Other"}")
        println("====================================")
    }
}
