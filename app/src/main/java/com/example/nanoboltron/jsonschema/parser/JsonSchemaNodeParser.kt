package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.DescriptorNode
import com.example.nanoboltron.jsonschema.core.Key

interface JsonSchemaNodeParser {
    fun parse(
        type: String,
        key: Key? = null,
        path: String? = null,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode
}