package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.parser.Key

interface NodeParser {
    fun parse(
        type: String,
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode
}