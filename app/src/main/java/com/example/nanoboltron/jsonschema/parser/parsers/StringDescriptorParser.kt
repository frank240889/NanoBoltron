package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.parser.Key

class StringDescriptorParser : NodeParser {
    override fun parse(
        type: String,
        key: Key?,
        value: Map<String, Any?>,
        path: String,
        isRootNode: Boolean
    ): DescriptorNode.StringNode {
        return DescriptorNode.StringNode(
            key = key,
            path = path,
            type = type,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = value["default"] as? String,
            placeholder = value["placeholder"] as? String,
            format = value["format"] as? String,
            readOnly = value["readOnly"] as? Boolean,
            writeOnly = value["writeOnly"] as? Boolean,
            enum = value["enum"] as? List<String>
        )
    }
}