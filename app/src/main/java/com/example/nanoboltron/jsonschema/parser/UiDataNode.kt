package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.core.Key
import com.example.nanoboltron.jsonschema.core.Path
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

sealed class UiDataNode : JsonNode {
    abstract val descriptorNode: DescriptorNode

    data class StringDataNode(
        override val key: Key?,
        override val path: Path?,
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: String,
    ) : UiDataNode()

    data class NumberDataNode(
        override val key: Key?,
        override val path: Path?,
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: Number
    ) : UiDataNode()

    data class BooleanDataNode(
        override val key: Key?,
        override val path: Path?,
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val value: Boolean
    ) : UiDataNode()

    data class ObjectDataNode(
        override val key: Key?,
        override val path: Path?,
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val children: List<UiDataNode>
    ) : UiDataNode()

    data class ArrayDataNode(
        override val key: Key?,
        override val path: Path?,
        override val type: String?,
        override val descriptorNode: DescriptorNode,
        val items: List<UiDataNode>
    ) : UiDataNode()
}