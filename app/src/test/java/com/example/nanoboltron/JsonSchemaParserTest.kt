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
    fun testPathGeneration() {
        val schema = """
        {
            "properties": {
                "character": {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "characteristics": {
                            "type": "object",
                            "properties": {
                                "external": {
                                    "type": "array",
                                    "items": {
                                        "type": "object",
                                        "properties": {
                                            "object1": {"type": "string"}
                                        }
                                    }
                                },
                                "internal": {
                                    "type": "array",
                                    "items": {
                                        "type": "object",
                                        "properties": {
                                            "object2": {"type": "string"}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        """.trimIndent()
        val result = parser.parse(schema)
        assertNotNull(result)

        // Verify root node
        assertTrue(result is DescriptorNode.GroupNode)
        val rootNode = result as DescriptorNode.GroupNode
        assertNull(rootNode.key)
        assertNull(rootNode.path)

        // Find character node
        val characterNode =
            rootNode.nodes?.find { it.key == "character" } as? DescriptorNode.GroupNode
        assertNotNull(characterNode)
        assertEquals("character", characterNode!!.key)
        assertEquals("properties", characterNode.path)

        // Find characteristics node
        val characteristicsNode =
            characterNode.nodes?.find { it.key == "characteristics" } as? DescriptorNode.GroupNode
        assertNotNull(characteristicsNode)
        assertEquals("characteristics", characteristicsNode!!.key)
        assertEquals("properties.character", characteristicsNode.path)

        // Find external array node
        val externalNode =
            characteristicsNode.nodes?.find { it.key == "external" } as? DescriptorNode.GroupNode
        assertNotNull(externalNode)
        assertEquals("external", externalNode!!.key)
        assertEquals("properties.character.characteristics", externalNode.path)

        // Find array items
        val arrayItems = externalNode.nodes
        assertNotNull(arrayItems)
        assertEquals(1, arrayItems!!.size)

        // Check first array item (object with properties)
        val firstItem = arrayItems[0] as? DescriptorNode.GroupNode
        assertNotNull(firstItem)
        assertNull(firstItem!!.key)  // Container object has no key
        assertEquals("properties.character.characteristics.external.items", firstItem.path)

        val object1Property = firstItem.nodes?.find { it.key == "object1" }
        assertNotNull(object1Property)
        assertEquals("object1", object1Property!!.key)
        assertEquals("properties.character.characteristics.external.items", object1Property.path)

        // Find internal array node
        val internalNode =
            characteristicsNode.nodes?.find { it.key == "internal" } as? DescriptorNode.GroupNode
        assertNotNull(internalNode)
        assertEquals("internal", internalNode!!.key)
        assertEquals("properties.character.characteristics", internalNode.path)

        val internalItems = internalNode.nodes!!
        assertEquals(1, internalItems.size)

        val firstInternalItem = internalItems[0] as? DescriptorNode.GroupNode
        assertNotNull(firstInternalItem)
        assertNull(firstInternalItem!!.key)  // Container object has no key
        assertEquals("properties.character.characteristics.internal.items", firstInternalItem.path)

        val object2Property = firstInternalItem.nodes?.find { it.key == "object2" }
        assertNotNull(object2Property)
        assertEquals("object2", object2Property!!.key)
        assertEquals("properties.character.characteristics.internal.items", object2Property.path)
    }

    @Test
    fun testComplexPathGeneration() {
        val schema = """
        {
            "properties": {
                "character": {
                    "type": "object",
                    "properties": {
                        "name": {"type": "string"},
                        "characteristics": {
                            "type": "object",
                            "properties": {
                                "external": {
                                    "type": "array",
                                    "items": [
                                        {
                                            "type": "object",
                                            "properties": {
                                                "object1": {"type": "string"}
                                            }
                                        },
                                        {
                                            "type": "object",
                                            "properties": {
                                                "object2": {"type": "string"}
                                            }
                                        }
                                    ]
                                },
                                "internal": {
                                    "type": "array",
                                    "items": [
                                        {
                                            "type": "object",
                                            "properties": {
                                                "object3": {"type": "string"}
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    }
                }
            }
        }
        """.trimIndent()
        val result = parser.parse(schema)
        assertNotNull(result)

        // Verify root node
        assertTrue(result is DescriptorNode.GroupNode)
        val rootNode = result as DescriptorNode.GroupNode
        assertNull(rootNode.key)
        assertNull(rootNode.path)

        // Find character node
        val characterNode =
            rootNode.nodes?.find { it.key == "character" } as? DescriptorNode.GroupNode
        assertNotNull(characterNode)
        assertEquals("character", characterNode!!.key)
        assertEquals("properties", characterNode.path)

        // Find characteristics node
        val characteristicsNode =
            characterNode.nodes?.find { it.key == "characteristics" } as? DescriptorNode.GroupNode
        assertNotNull(characteristicsNode)
        assertEquals("characteristics", characteristicsNode!!.key)
        assertEquals("properties.character", characteristicsNode.path)

        // Find external array node
        val externalNode =
            characteristicsNode.nodes?.find { it.key == "external" } as? DescriptorNode.GroupNode
        assertNotNull(externalNode)
        assertEquals("external", externalNode!!.key)
        assertEquals("properties.character.characteristics", externalNode.path)

        // Find array items
        val arrayItems = externalNode.nodes
        assertNotNull(arrayItems)
        assertEquals(2, arrayItems!!.size)

        // Check first array item (object1)
        val firstItem = arrayItems[0] as? DescriptorNode.GroupNode
        assertNotNull(firstItem)
        assertNull(firstItem!!.key)  // Container object has no key
        assertEquals("properties.character.characteristics.external[0]", firstItem.path)

        val object1Property = firstItem.nodes?.find { it.key == "object1" }
        assertNotNull(object1Property)
        assertEquals("object1", object1Property!!.key)
        assertEquals("properties.character.characteristics.external[0]", object1Property.path)

        // Check second array item (object2)
        val secondItem = arrayItems[1] as? DescriptorNode.GroupNode
        assertNotNull(secondItem)
        assertNull(secondItem!!.key)  // Container object has no key
        assertEquals("properties.character.characteristics.external[1]", secondItem.path)

        val object2Property = secondItem.nodes?.find { it.key == "object2" }
        assertNotNull(object2Property)
        assertEquals("object2", object2Property!!.key)
        assertEquals("properties.character.characteristics.external[1]", object2Property.path)

        // Verify internal array
        val internalNode =
            characteristicsNode.nodes?.find { it.key == "internal" } as? DescriptorNode.GroupNode
        assertNotNull(internalNode)
        assertEquals("internal", internalNode!!.key)
        assertEquals("properties.character.characteristics", internalNode.path)

        val internalItems = internalNode.nodes!!
        assertEquals(1, internalItems.size)

        val firstInternalItem = internalItems[0] as? DescriptorNode.GroupNode
        assertNotNull(firstInternalItem)
        assertNull(firstInternalItem!!.key)  // Container object has no key
        assertEquals("properties.character.characteristics.internal[0]", firstInternalItem.path)

        val object3Property = firstInternalItem.nodes?.find { it.key == "object3" }
        assertNotNull(object3Property)
        assertEquals("object3", object3Property!!.key)
        assertEquals("properties.character.characteristics.internal[0]", object3Property.path)
    }

    @Test
    fun testAllOfPathGeneration() {
        val schema = """
        {
            "properties": {
                "users": {
                    "type": "array",
                    "items": {
                        "allOf": [
                            {
                                "type": "object",
                                "properties": {
                                    "id": {"type": "string"},
                                    "email": {"type": "string"}
                                }
                            },
                            {
                                "oneOf": [
                                    {"properties": {"role": {"type": "string"}}}
                                ]
                            }
                        ]
                    }
                }
            }
        }
        """.trimIndent()
        val result = parser.parse(schema)
        assertNotNull(result)

        // Navigate to the allOf node
        val rootNode = result as DescriptorNode.GroupNode
        val usersNode = rootNode.nodes?.find { it.key == "users" } as? DescriptorNode.GroupNode
        assertNotNull(usersNode)

        // Should find allOf composition node
        val usersItems = usersNode?.nodes
        assertNotNull(usersItems)
        usersItems!!.forEachIndexed { index, item ->
            println("Item $index: key='${item.key}', path='${item.path}', type=${item.javaClass.simpleName}")
        }

        // Find the allOf composition
        val allOfComposition = usersItems[0] as? DescriptorNode.CompositionNode
        assertNotNull(allOfComposition)
        assertEquals("allOf", allOfComposition!!.key)
        assertEquals("properties.users.items", allOfComposition.path)

        // Check the second schema in allOf (should contain oneOf)
        val secondAllOfSchema = allOfComposition!!.schemas[1] as? DescriptorNode.CompositionNode
        assertNotNull(secondAllOfSchema)
        assertEquals("oneOf", secondAllOfSchema!!.key)

        // Check the oneOf[0] schema
        val oneOfSchema = secondAllOfSchema.schemas[0] as? DescriptorNode.GroupNode
        assertNotNull(oneOfSchema)
        assertEquals("oneOf[0]", oneOfSchema!!.key)
        assertEquals("properties.users.items.allOf[1].oneOf[0]", oneOfSchema.path)

        // Check the role property inside oneOf[0]
        val roleProperty = oneOfSchema.nodes?.find { it.key == "role" }
        assertNotNull(roleProperty)
        assertEquals("role", roleProperty!!.key)
        assertEquals("properties.users.items.allOf[1].oneOf[0].properties", roleProperty.path)
    }

    @Test
    fun testActualJsonSchemaFile() {
        // Test parsing a realistic schema structure
        val jsonSchemaContent = """{
          "title": "UserSystem",
          "type": "object",
          "properties": {
            "metadata": {
              "type": "object", 
              "properties": {
                "version": {"type": "string"},
                "generatedAt": {"type": "string"}
              }
            },
            "users": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "id": {"type": "string"},
                  "email": {"type": "string"},
                  "role": {"type": "string"}
                }
              }
            }
          }
        }""".trimIndent()

        val result = parser.parse(jsonSchemaContent)
        assertNotNull(result)

        // Test key paths are correctly generated
        val usersNode =
            (result as DescriptorNode.GroupNode).nodes?.find { it.key == "users" } as? DescriptorNode.GroupNode
        assertNotNull(usersNode)
        assertEquals("users", usersNode!!.key)
        assertEquals("properties", usersNode.path)

        // Test array item properties
        val userItems = usersNode?.nodes
        assertNotNull(userItems)
        assertEquals(1, userItems!!.size)

        val userItem = userItems[0] as? DescriptorNode.GroupNode
        assertNotNull(userItem)

        // Check that we can find the properties inside the user object
        val idProperty = userItem!!.nodes?.find { it.key == "id" }
        assertNotNull(idProperty)
        assertEquals("id", idProperty!!.key)
        assertEquals("properties.users.items", idProperty.path)
    }
}
