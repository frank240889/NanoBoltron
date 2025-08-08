package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.core.DescriptorNode
import com.example.nanoboltron.jsonschema.core.Key
import com.example.nanoboltron.jsonschema.parser.JsonSchemaNodeParser

class NumberDescriptorParserJsonSchema : JsonSchemaNodeParser {
    override fun parse(
        type: String,
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean
    ): DescriptorNode.NumberNode {
        return DescriptorNode.NumberNode(
            key = key,
            path = path,
            type = type,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = value["default"] as? Number,
            placeholder = value["placeholder"] as? String,
            readOnly = value["readOnly"] as? Boolean,
            writeOnly = value["writeOnly"] as? Boolean,
            multipleOf = value["multipleOf"] as? Number,
            enum = value["enum"] as? List<Number>
        )
    }
}