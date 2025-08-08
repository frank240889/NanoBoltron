package com.example.nanoboltron.jsonschema.core

import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.COMPOSITION_NODE
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.STRING_NODE

/**
 * The next descriptor classes convey the idea of how should the node be represented in the UI according
 * to the data type they hold, these represent strongly typed nodes that can be handled in Kotlin.
 * [https://json-schema.org/understanding-json-schema/reference/type]
 */
sealed class DescriptorNode : JsonNode {

    abstract val jsonSchemaType: Type
    abstract val title: String?
    abstract val description: String?

    /**
     * Represents a section/group of fields, generally the root node belongs to this type
     */
    data class GroupNode(
        override val jsonSchemaType: Type = GROUP,
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = GROUP,
        override val title: String? = null,
        override val description: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        val enum: List<DescriptorNode>? = null,
        val nodes: List<DescriptorNode>? = null
    ) : DescriptorNode()


    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class StringNode(
        override val jsonSchemaType: Type = STRING_NODE,
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
        override val jsonSchemaType: Type = NUMBER_NODE,
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
        override val jsonSchemaType: Type = BOOLEAN_NODE,
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
        override val jsonSchemaType: Type = COMPOSITION_NODE,
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
        override val jsonSchemaType: Type,
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