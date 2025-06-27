package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import org.junit.Test
import org.junit.Assert.*

class JsonSchemaRealWorldTest {

    private val parser = JsonSchemaParser()

    @Test
    fun testUserProfileSchema() {
        // Real-world user profile with allOf merging contact info
        val userProfileSchema = """
        {
            "type": "object",
            "properties": {
                "user": {
                    "allOf": [
                        {
                            "properties": {
                                "name": {"type": "string", "title": "Full Name"},
                                "email": {"type": "string", "title": "Email"}
                            }
                        },
                        {
                            "properties": {
                                "age": {"type": "number", "title": "Age"},
                                "verified": {"type": "boolean", "title": "Verified Account"}
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val result = parser.parse(userProfileSchema)
        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        val groupNode = result as DescriptorNode.GroupNode
        assertEquals(1, groupNode.nodes?.size)

        // The user property should be merged from allOf
        val userNode = groupNode.nodes?.first()
        assertTrue(userNode is DescriptorNode.GroupNode)

        val userGroup = userNode as DescriptorNode.GroupNode
        assertEquals(4, userGroup.nodes?.size) // name, email, age, verified merged

        println("=== User Profile Schema ===")
        println(result.toString())
        println("===========================")
    }

    @Test
    fun testPaymentMethodSchema() {
        // Real-world payment method using oneOf for different payment types
        val paymentSchema = """
        {
            "properties": {
                "payment": {
                    "oneOf": [
                        {
                            "properties": {
                                "type": {"const": "credit_card"},
                                "cardNumber": {"type": "string"},
                                "expiryDate": {"type": "string"}
                            }
                        },
                        {
                            "properties": {
                                "type": {"const": "paypal"},
                                "email": {"type": "string"}
                            }
                        },
                        {
                            "properties": {
                                "type": {"const": "bank_transfer"},
                                "accountNumber": {"type": "string"},
                                "routingNumber": {"type": "string"}
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val result = parser.parse(paymentSchema)
        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        val groupNode = result as DescriptorNode.GroupNode
        val paymentNode = groupNode.nodes?.first()
        assertTrue(paymentNode is DescriptorNode.CompositionNode)

        val compositionNode = paymentNode as DescriptorNode.CompositionNode
        assertEquals("oneOf", compositionNode.compositionType)
        assertEquals(3, compositionNode.schemas.size) // 3 payment methods

        println("=== Payment Method Schema ===")
        println(result.toString())
        println("==============================")
    }

    @Test
    fun testAddressSchema() {
        // Real-world address using anyOf for different address formats
        val addressSchema = """
        {
            "properties": {
                "address": {
                    "anyOf": [
                        {
                            "properties": {
                                "street": {"type": "string"},
                                "city": {"type": "string"},
                                "zipCode": {"type": "string"}
                            }
                        },
                        {
                            "properties": {
                                "poBox": {"type": "string"},
                                "city": {"type": "string"},
                                "zipCode": {"type": "string"}
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val result = parser.parse(addressSchema)
        assertNotNull(result)

        val groupNode = result as DescriptorNode.GroupNode
        val addressNode = groupNode.nodes?.first()
        assertTrue(addressNode is DescriptorNode.CompositionNode)

        val compositionNode = addressNode as DescriptorNode.CompositionNode
        assertEquals("anyOf", compositionNode.compositionType)
        assertEquals(2, compositionNode.schemas.size)

        println("=== Address Schema ===")
        println(result.toString())
        println("=======================")
    }

    @Test
    fun testConditionalShippingSchema() {
        // Real-world conditional shipping using if-then-else
        val shippingSchema = """
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
                            "zipCode": {"type": "string", "title": "ZIP Code"},
                            "state": {"type": "string", "title": "State"}
                        }
                    },
                    "else": {
                        "properties": {
                            "postalCode": {"type": "string", "title": "Postal Code"},
                            "province": {"type": "string", "title": "Province"}
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val result = parser.parse(shippingSchema)
        assertNotNull(result)

        val groupNode = result as DescriptorNode.GroupNode
        val shippingNode = groupNode.nodes?.first()
        assertTrue(shippingNode is DescriptorNode.ConditionalNode)

        val conditionalNode = shippingNode as DescriptorNode.ConditionalNode
        assertNotNull(conditionalNode.ifSchema)
        assertNotNull(conditionalNode.thenSchema)
        assertNotNull(conditionalNode.elseSchema)

        println("=== Conditional Shipping Schema ===")
        println(result.toString())
        println("====================================")
    }

    @Test
    fun testComplexNestedCompositionSchema() {
        // Complex real-world example with nested compositions
        val complexSchema = """
        {
            "properties": {
                "registration": {
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
                                "contactMethod": {
                                    "oneOf": [
                                        {
                                            "properties": {
                                                "email": {"type": "string"}
                                            }
                                        },
                                        {
                                            "properties": {
                                                "phone": {"type": "string"}
                                            }
                                        }
                                    ]
                                }
                            }
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val result = parser.parse(complexSchema)
        assertNotNull(result)
        assertTrue(result is DescriptorNode.GroupNode)

        println("=== Complex Nested Composition ===")
        println(result.toString())
        println("===================================")

        // Verify the structure
        val rootGroup = result as DescriptorNode.GroupNode
        assertEquals(1, rootGroup.nodes?.size)

        val registrationNode = rootGroup.nodes?.first()
        assertNotNull(registrationNode)

        // Should have merged the allOf into a single group with personalInfo and contactMethod
        assertTrue(registrationNode is DescriptorNode.GroupNode)
        val regGroup = registrationNode as DescriptorNode.GroupNode
        assertEquals(2, regGroup.nodes?.size) // personalInfo and contactMethod
    }

    @Test
    fun testApiResponseSchema() {
        // API response schema with complex composition
        val apiResponseSchema = """
        {
            "oneOf": [
                {
                    "properties": {
                        "success": {"const": true},
                        "data": {
                            "anyOf": [
                                {"type": "string"},
                                {"type": "number"},
                                {
                                    "type": "array",
                                    "items": {"type": "string"}
                                }
                            ]
                        }
                    }
                },
                {
                    "properties": {
                        "success": {"const": false},
                        "error": {
                            "properties": {
                                "code": {"type": "number"},
                                "message": {"type": "string"}
                            }
                        }
                    }
                }
            ]
        }
        """.trimIndent()

        val result = parser.parse(apiResponseSchema)
        assertNotNull(result)
        assertTrue(result is DescriptorNode.CompositionNode)

        val compositionNode = result as DescriptorNode.CompositionNode
        assertEquals("oneOf", compositionNode.compositionType)
        assertEquals(2, compositionNode.schemas.size) // success and error responses

        println("=== API Response Schema ===")
        println(result.toString())
        println("============================")
    }

    @Test
    fun testFormValidationSchema() {
        // Form with conditional validation
        val formSchema = """
        {
            "allOf": [
                {
                    "properties": {
                        "name": {"type": "string"},
                        "email": {"type": "string"},
                        "age": {"type": "number"}
                    }
                },
                {
                    "if": {
                        "properties": {
                            "age": {"minimum": 18}
                        }
                    },
                    "then": {
                        "properties": {
                            "driversLicense": {"type": "string"}
                        }
                    },
                    "else": {
                        "properties": {
                            "parentConsent": {"type": "boolean"}
                        }
                    }
                }
            ]
        }
        """.trimIndent()

        val result = parser.parse(formSchema)
        assertNotNull(result)

        // Should create a CompositionNode since it contains if-then-else which can't be merged
        assertTrue(result is DescriptorNode.CompositionNode)

        val compositionNode = result as DescriptorNode.CompositionNode
        assertEquals("allOf", compositionNode.compositionType)
        assertEquals(2, compositionNode.schemas.size)

        println("=== Form Validation Schema ===")
        println(result.toString())
        println("===============================")
    }
}