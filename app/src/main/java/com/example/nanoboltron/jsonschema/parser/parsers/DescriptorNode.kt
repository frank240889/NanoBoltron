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
     * Represent a section/group of fields, generally the root node belongs to this type
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
    ) : DescriptorNode()


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
}