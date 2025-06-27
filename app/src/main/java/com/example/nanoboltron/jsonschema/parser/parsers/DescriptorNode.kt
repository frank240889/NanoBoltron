package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.parser.JsonNode
import com.example.nanoboltron.jsonschema.parser.Key

/**
 * The next descriptor classes convey the idea of how should the node be represented in the UI according
 * to the data type they hold.
 * [https://json-schema.org/understanding-json-schema/reference/type]
 */
sealed class DescriptorNode : JsonNode {

    /**
     * The key of the node. Useful to match it against the TypedUISchema. Its value is null for the
     * root node.
     */
    abstract val key: Key?
    /**
     * The path to get to the node in the format path.to.the.node where every word between the dots
     * represent one level down in the data structure, commonly a map, null indicates the root node.
     */
    abstract val path: String?
    abstract val title: String?
    abstract val description: String?

    /**
     * Helper function to create indented string representation
     */
    fun toStringIndented(indentLevel: Int = 0): String {
        val indent = "  ".repeat(indentLevel)
        return when (this) {
            is GroupNode -> {
                buildString {
                    append("GroupNode(")
                    append("\n${indent}  type: $type")
                    key?.let { append("\n${indent}  key: $it") }
                    path?.let { append("\n${indent}  path: $it") }
                    title?.let { append("\n${indent}  title: \"$it\"") }
                    description?.let { append("\n${indent}  description: \"$it\"") }
                    readOnly?.let { append("\n${indent}  readOnly: $it") }
                    writeOnly?.let { append("\n${indent}  writeOnly: $it") }

                    nodes?.let { nodeList ->
                        if (nodeList.isNotEmpty()) {
                            append("\n${indent}  nodes: [")
                            nodeList.forEachIndexed { index, node ->
                                append(
                                    "\n${indent}    ${index + 1}. ${
                                        node.toStringIndented(
                                            indentLevel + 2
                                        )
                                    }"
                                )
                            }
                            append("\n${indent}  ]")
                        } else {
                            append("\n${indent}  nodes: []")
                        }
                    }
                    append("\n${indent})")
                }
            }

            is StringNode -> "${indent}StringNode(key: $key, type: $type, title: \"$title\")"
            is NumberNode -> "${indent}NumberNode(key: $key, type: $type, title: \"$title\")"
            is BooleanNode -> "${indent}BooleanNode(key: $key, type: $type, title: \"$title\")"
            is CompositionNode -> {
                buildString {
                    append("${indent}CompositionNode(")
                    append("\n${indent}  compositionType: $compositionType")
                    key?.let { append("\n${indent}  key: $it") }
                    title?.let { append("\n${indent}  title: \"$it\"") }
                    append("\n${indent}  schemas: [")
                    schemas.forEachIndexed { index, schema ->
                        append("\n${indent}    ${index + 1}. ${schema.toStringIndented(indentLevel + 2)}")
                    }
                    append("\n${indent}  ]")
                    append("\n${indent})")
                }
            }

            is ConditionalNode -> {
                buildString {
                    append("${indent}ConditionalNode(")
                    key?.let { append("\n${indent}  key: $it") }
                    title?.let { append("\n${indent}  title: \"$it\"") }
                    ifSchema?.let { append("\n${indent}  if: ${it.toStringIndented(indentLevel + 1)}") }
                    thenSchema?.let { append("\n${indent}  then: ${it.toStringIndented(indentLevel + 1)}") }
                    elseSchema?.let { append("\n${indent}  else: ${it.toStringIndented(indentLevel + 1)}") }
                    append("\n${indent})")
                }
            }
        }
    }

    /**
     * Represents a section/group of fields, generally the root node belongs to this type
     */
    data class GroupNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = GROUP,
        override val title: String? = null,
        override val description: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        val nodes: List<DescriptorNode>? = null
    ) : DescriptorNode() {
        override fun toString(): String {
            return toStringIndented(0)
        }
    }


    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class StringNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = STRING_NODE,
        override val title: String? = null,
        override val description: String? = null,
        val default: String? = null,
        val placeholder: String? = null,
        val format: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        /**
         * A set of predefined values
         */
        val enum: List<String>? = null,
        val contentMediaType: String? = null,
        val contentEncoding: String? = null
    ) : DescriptorNode()

    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class NumberNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = NUMBER_NODE,
        override val title: String? = null,
        override val description: String? = null,
        val default: Number? = null,
        val placeholder: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        /**
         * A set of predefined values
         */
        val enum: List<Number>? = null,
        /**
         * Can define the steps in a Slider-like component.
         */
        val multipleOf: Number? = null
    ) : DescriptorNode()

    /**
     * Represents a single 2-state component.
     */
    data class BooleanNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = BOOLEAN_NODE,
        override val title: String? = null,
        override val description: String? = null,
        val default: Boolean? = null,
        val readOnly: Boolean? = null,
    ) : DescriptorNode()

    /**
     * Represents a composition of multiple schemas using allOf, anyOf, or oneOf
     */
    data class CompositionNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String,
        override val title: String? = null,
        override val description: String? = null,
        val compositionType: String, // "allOf", "anyOf", "oneOf"
        val schemas: List<DescriptorNode>
    ) : DescriptorNode()

    /**
     * Represents conditional schema logic using if-then-else
     */
    data class ConditionalNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = "conditional",
        override val title: String? = null,
        override val description: String? = null,
        val ifSchema: DescriptorNode?,
        val thenSchema: DescriptorNode?,
        val elseSchema: DescriptorNode?
    ) : DescriptorNode()
}
