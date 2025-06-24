package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.BOOLEAN
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.INTEGER
import com.example.nanoboltron.jsonschema.NUMBER
import com.example.nanoboltron.jsonschema.REPEATABLE
import com.example.nanoboltron.jsonschema.STRING

/**
 * The next descriptor classes convey the idea of how should the node be represented in the UI according
 * to the data type they hold.
 * [https://json-schema.org/understanding-json-schema/reference/type]
 */
sealed class UiDescriptorNode : JsonSchemaNode {

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
        val properties: List<UiDescriptorNode>? = null
    ) : UiDescriptorNode()

    /**
     * Represent a repeatable set of items
     */
    data class RepeatingGroupNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = REPEATABLE,
        override val title: String? = null,
        override val description: String? = null,
        val default: List<UiDescriptorNode>? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        val items: List<UiDescriptorNode>? = null
    ) : UiDescriptorNode()


    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class StringNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = STRING,
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
    ) : UiDescriptorNode()

    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class NumberNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = NUMBER,
        override val title: String? = null,
        override val description: String? = null,
        val default: Double? = null,
        val placeholder: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        /**
         * A set of predefined values
         */
        val enum: List<Double>? = null,
        /**
         * Can define the steps in a Slider-like component.
         */
        val multipleOf: Double? = null
    ) : UiDescriptorNode()

    /**
     * Represents a single input field or a custom component with predefined values if enum is not
     * null
     */
    data class IntegerNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = INTEGER,
        override val title: String? = null,
        override val description: String? = null,
        val default: Int? = null,
        val placeholder: String? = null,
        val readOnly: Boolean? = null,
        val writeOnly: Boolean? = null,
        /**
         * A set of predefined values
         */
        val enum: List<Int>? = null,
        /**
         * Can define the steps in a Slider-like component.
         */
        val multipleOf: Int? = null
    ) : UiDescriptorNode()

    /**
     * Represents a single 2-state component.
     */
    data class BooleanNode(
        override val key: Key? = null,
        override val path: String? = null,
        override val type: String = BOOLEAN,
        override val title: String? = null,
        override val description: String? = null,
        val default: Boolean? = null,
        val readOnly: Boolean? = null,
    ) : UiDescriptorNode()
}