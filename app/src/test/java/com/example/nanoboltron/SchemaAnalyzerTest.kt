package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.analyzer.SchemaAnalyzer
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import org.junit.Test
import org.junit.Assert.*

class SchemaAnalyzerTest {

    private val parser = JsonSchemaParser()
    private val analyzer = SchemaAnalyzer()

    @Test
    fun testBasicSchemaAnalysis() {
        val schema = """
        {
            "properties": {
                "user": {
                    "properties": {
                        "name": {"type": "string", "title": "Full Name"},
                        "email": {"type": "string", "title": "Email"},
                        "age": {"type": "number", "title": "Age"}
                    }
                },
                "preferences": {
                    "anyOf": [
                        {"properties": {"theme": {"type": "string"}}},
                        {"properties": {"language": {"type": "string"}}}
                    ]
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!! as DescriptorNode
        val analysis = analyzer.analyzeSchema(rootNode, "testSchema")

        println("=== Basic Schema Analysis Test ===")
        // Skip logging to avoid Android Log dependency in unit tests
        println("Schema: ${analysis.schemaName}")
        println("Total nodes: ${analysis.totalNodes}")
        println("Form fields: ${analysis.formFields.size}")
        println("Composition nodes: ${analysis.compositionNodes.size}")
        println("Conditional nodes: ${analysis.conditionalNodes.size}")
        println("===================================")

        // Verify analysis results
        assertTrue("Should have nodes", analysis.totalNodes > 0)
        assertTrue("Should have form fields", analysis.formFields.isNotEmpty())
        assertTrue("Should have composition nodes", analysis.compositionNodes.isNotEmpty())
        assertEquals("Schema name should match", "testSchema", analysis.schemaName)
    }

    @Test
    fun testComplexityMetrics() {
        val complexSchema = """
        {
            "properties": {
                "registration": {
                    "allOf": [
                        {"properties": {"name": {"type": "string"}}},
                        {
                            "if": {"properties": {"age": {"minimum": 18}}},
                            "then": {"properties": {"license": {"type": "string"}}},
                            "else": {"properties": {"parentConsent": {"type": "boolean"}}}
                        }
                    ]
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(complexSchema)!! as DescriptorNode
        val analysis = analyzer.analyzeSchema(rootNode, "complexSchema")
        val metrics = analyzer.getComplexityMetrics(analysis)

        println("=== Complexity Metrics Test ===")
        println("Total nodes: ${metrics.totalNodes}")
        println("Leaf node ratio: ${metrics.leafNodeRatio}")
        println("Average depth: ${metrics.averageDepth}")
        println("Max depth: ${metrics.maxDepth}")
        println("Complexity score: ${metrics.complexityScore}")
        println("===============================")

        assertTrue("Should have complexity score", metrics.complexityScore > 0)
        assertTrue("Should have reasonable depth", metrics.maxDepth >= 2)
        assertTrue("Should have leaf nodes", metrics.leafNodeRatio > 0)
    }

    @Test
    fun testFormFieldsBySection() {
        val schema = """
        {
            "properties": {
                "personalInfo": {
                    "properties": {
                        "firstName": {"type": "string"},
                        "lastName": {"type": "string"}
                    }
                },
                "contactInfo": {
                    "properties": {
                        "email": {"type": "string"},
                        "phone": {"type": "string"}
                    }
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(schema)!! as DescriptorNode
        val analysis = analyzer.analyzeSchema(rootNode, "sectionedSchema")
        val fieldsBySection = analyzer.getFormFieldsBySection(analysis)

        println("=== Fields by Section Test ===")
        fieldsBySection.forEach { (section, fields) ->
            println("Section '$section':")
            fields.forEach { field ->
                println("  - ${field.key} (${field.nodeType})")
            }
        }
        println("==============================")

        assertTrue("Should have sections", fieldsBySection.isNotEmpty())
        assertTrue("Should have personalInfo section", fieldsBySection.containsKey("personalInfo"))
        assertTrue("Should have contactInfo section", fieldsBySection.containsKey("contactInfo"))
    }

    @Test
    fun testValidationRuleExtraction() {
        val schema = """
        {
            "properties": {
                "registration": {
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

        val rootNode = parser.parse(schema)!! as DescriptorNode
        val analysis = analyzer.analyzeSchema(rootNode, "validationSchema")
        val validationRules = analyzer.extractValidationRules(analysis)

        println("=== Validation Rules Test ===")
        validationRules.forEach { rule ->
            println("Rule at ${rule.path}:")
            println("  Condition: ${rule.condition}")
            println("  Then: ${rule.thenAction}")
            println("  Else: ${rule.elseAction}")
        }
        println("=============================")

        assertTrue("Should have validation rules", validationRules.isNotEmpty())
        assertEquals("Should have one conditional rule", 1, validationRules.size)

        val rule = validationRules.first()
        assertNotNull("Should have condition", rule.condition)
        assertNotNull("Should have then action", rule.thenAction)
        assertNotNull("Should have else action", rule.elseAction)
    }
}
