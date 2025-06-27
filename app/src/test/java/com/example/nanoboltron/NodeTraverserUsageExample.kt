package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.NodeTraverser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import org.junit.Test

/**
 * Practical usage examples for the NodeTraverser component
 */
class NodeTraverserUsageExample {

    private val parser = JsonSchemaParser()
    private val traverser = NodeTraverser()

    @Test
    fun demonstrateBasicUsage() {
        // Parse a complex schema
        val complexSchema = """
        {
            "properties": {
                "user": {
                    "allOf": [
                        {"properties": {"name": {"type": "string"}}},
                        {"properties": {"email": {"type": "string"}}}
                    ]
                },
                "preferences": {
                    "anyOf": [
                        {"properties": {"theme": {"type": "string"}}},
                        {"properties": {"language": {"type": "string"}}}
                    ]
                },
                "payment": {
                    "oneOf": [
                        {"properties": {"creditCard": {"type": "string"}}},
                        {"properties": {"paypal": {"type": "string"}}}
                    ]
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(complexSchema)!!

        println("🚀 NodeTraverser Usage Examples")
        println("================================")

        // 1. Get all paths in the schema
        println("\n📍 All paths in the schema:")
        val allPaths = traverser.getAllPaths(rootNode)
        allPaths.forEach { path ->
            println("   ${path.path} -> ${path.nodeType} (depth: ${path.depth})")
        }

        // 2. Find all form fields (leaf nodes)
        println("\n📝 Form fields (leaf nodes):")
        val formFields = traverser.getLeafNodes(rootNode)
        formFields.forEach { field ->
            val title = when (val node = field.node) {
                is DescriptorNode.StringNode -> node.title
                is DescriptorNode.NumberNode -> node.title
                is DescriptorNode.BooleanNode -> node.title
                else -> null
            }
            println("   Field: ${field.key} | Type: ${field.nodeType} | Title: $title")
        }

        // 3. Find all choice points (composition nodes)
        println("\n🔀 Choice points (composition nodes):")
        val choicePoints = traverser.getCompositionNodes(rootNode)
        choicePoints.forEach { choice ->
            val compositionNode = choice.node as DescriptorNode.CompositionNode
            println("   ${choice.key} -> ${compositionNode.compositionType} (${compositionNode.schemas.size} options)")
        }

        // 4. Analyze schema complexity
        println("\n📊 Schema Analysis:")
        println("   Total nodes: ${allPaths.size}")
        println("   Maximum depth: ${traverser.getMaxDepth(rootNode)}")
        println("   Form fields: ${formFields.size}")
        println("   Choice points: ${choicePoints.size}")
        println("   Conditional logic: ${traverser.getConditionalNodes(rootNode).size}")

        // 5. Print beautiful tree structure
        println("\n🌳 Schema Tree Structure:")
        println(traverser.printTree(rootNode))
    }

    @Test
    fun demonstrateFormGeneration() {
        val formSchema = """
        {
            "properties": {
                "personalInfo": {
                    "properties": {
                        "firstName": {"type": "string", "title": "First Name"},
                        "lastName": {"type": "string", "title": "Last Name"},
                        "age": {"type": "number", "title": "Age"}
                    }
                },
                "contactMethod": {
                    "oneOf": [
                        {"properties": {"email": {"type": "string", "title": "Email Address"}}},
                        {"properties": {"phone": {"type": "string", "title": "Phone Number"}}}
                    ]
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(formSchema)!!

        println("\n📝 Form Generation Example")
        println("===========================")

        // Generate form sections
        generateFormSections(rootNode)
    }

    @Test
    fun demonstrateValidationRules() {
        val validationSchema = """
        {
            "properties": {
                "registration": {
                    "if": {
                        "properties": {
                            "age": {"minimum": 18}
                        }
                    },
                    "then": {
                        "properties": {
                            "driverLicense": {"type": "string", "title": "Driver License"}
                        }
                    },
                    "else": {
                        "properties": {
                            "parentConsent": {"type": "boolean", "title": "Parent Consent"}
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val rootNode = parser.parse(validationSchema)!!

        println("\n✅ Validation Rules Example")
        println("============================")

        // Extract validation logic
        extractValidationRules(rootNode)
    }

    private fun generateFormSections(rootNode: DescriptorNode) {
        val allPaths = traverser.getAllPaths(rootNode)
        val sections = mutableMapOf<String, MutableList<NodeTraverser.NodePath>>()

        // Group nodes by their parent section
        allPaths.forEach { nodePath ->
            val parentSection = nodePath.path.split(".").getOrNull(1) ?: "root"
            sections.getOrPut(parentSection) { mutableListOf() }.add(nodePath)
        }

        sections.forEach { (sectionName, nodes) ->
            println("\n📋 Section: $sectionName")
            nodes.forEach { node ->
                when (val descriptorNode = node.node) {
                    is DescriptorNode.StringNode -> {
                        println("   📝 Text Input: ${descriptorNode.title ?: node.key}")
                    }

                    is DescriptorNode.NumberNode -> {
                        println("   🔢 Number Input: ${descriptorNode.title ?: node.key}")
                    }

                    is DescriptorNode.BooleanNode -> {
                        println("   ☑️ Checkbox: ${descriptorNode.title ?: node.key}")
                    }

                    is DescriptorNode.CompositionNode -> {
                        println("   🔀 Choice Group: ${descriptorNode.compositionType} with ${descriptorNode.schemas.size} options")
                    }

                    is DescriptorNode.GroupNode -> {
                        println("   📁 Group: ${descriptorNode.title ?: node.key}")
                    }

                    is DescriptorNode.ConditionalNode -> {
                        println("   🔀 Conditional Logic: ${node.key}")
                    }
                }
            }
        }
    }

    private fun extractValidationRules(rootNode: DescriptorNode) {
        val conditionalNodes = traverser.getConditionalNodes(rootNode)

        conditionalNodes.forEach { conditionalPath ->
            val conditionalNode = conditionalPath.node as DescriptorNode.ConditionalNode

            println("🔍 Conditional Rule at: ${conditionalPath.path}")

            conditionalNode.ifSchema?.let { ifSchema ->
                println("   Condition: Based on ${getNodeDescription(ifSchema)}")
            }

            conditionalNode.thenSchema?.let { thenSchema ->
                println("   Then: Show ${getNodeDescription(thenSchema)}")
            }

            conditionalNode.elseSchema?.let { elseSchema ->
                println("   Else: Show ${getNodeDescription(elseSchema)}")
            }
        }
    }

    private fun getNodeDescription(node: DescriptorNode): String {
        return when (node) {
            is DescriptorNode.GroupNode -> {
                val fieldCount = node.nodes?.size ?: 0
                "$fieldCount fields"
            }

            is DescriptorNode.StringNode -> "string field '${node.title ?: node.key}'"
            is DescriptorNode.NumberNode -> "number field '${node.title ?: node.key}'"
            is DescriptorNode.BooleanNode -> "boolean field '${node.title ?: node.key}'"
            is DescriptorNode.CompositionNode -> "${node.compositionType} with ${node.schemas.size} options"
            is DescriptorNode.ConditionalNode -> "conditional logic"
        }
    }
}
