package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.parser.Key

class BooleanDescriptorParser : NodeParser {
    override fun parse(
        type: String,
        key: Key?,
        value: Map<String, Any?>,
        path: String,
        isRootNode: Boolean
    ): DescriptorNode.BooleanNode {
        return DescriptorNode.BooleanNode(
            key = key,
            path = path,
            type = type,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = value["default"] as? Boolean,
            readOnly = value["readOnly"] as? Boolean
        )
    }
}