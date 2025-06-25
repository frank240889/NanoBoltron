package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.DescriptorNode

sealed class SimpleRenderableNode {
    abstract val descriptorNode: DescriptorNode

    data class SingleStringValueNode(
        override val descriptorNode: DescriptorNode,
        val value: String
    ) : SimpleRenderableNode()

    data class SingleIntegerValueNode(
        override val descriptorNode: DescriptorNode,
        val value: Int
    ) : SimpleRenderableNode()

    data class SingleNumberValueNode(
        override val descriptorNode: DescriptorNode,
        val value: Double
    ) : SimpleRenderableNode()

    data class SingleBooleanValueNode(
        override val descriptorNode: DescriptorNode,
        val value: Boolean
    ) : SimpleRenderableNode()
}