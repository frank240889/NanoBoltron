package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

sealed class UiDataNode : JsonNode {
    abstract val descriptorNode: DescriptorNode

    data class StringDataNode(
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: String
    ) : UiDataNode()

    data class NumberDataNode(
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: Number
    ) : UiDataNode()

    data class BooleanDataNode(
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: Boolean
    ) : UiDataNode()

    data class ObjectDataNode(
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val children: List<UiDataNode>
    ) : UiDataNode()

    data class ArrayDataNode(
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val items: List<UiDataNode>
    ) : UiDataNode()
}