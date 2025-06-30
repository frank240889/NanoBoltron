package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.parser.Key

interface NodeParser {
    fun parse(
        type: String,
        key: Key? = null,
        path: String? = null,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode
}