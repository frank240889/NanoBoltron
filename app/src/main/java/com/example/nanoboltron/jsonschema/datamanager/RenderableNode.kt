package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.UiDescriptorNode

data class RenderableNode(
    val data: Any,
    val uiDescriptorNode: UiDescriptorNode
)