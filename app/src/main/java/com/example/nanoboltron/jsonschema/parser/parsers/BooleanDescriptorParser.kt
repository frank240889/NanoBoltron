package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.DEFAULT
import com.example.nanoboltron.jsonschema.DESCRIPTION
import com.example.nanoboltron.jsonschema.READ_ONLY
import com.example.nanoboltron.jsonschema.TITLE
import com.example.nanoboltron.jsonschema.core.Key

class BooleanDescriptorParser : NodeParser {
    override fun parse(
        type: String,
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean
    ): DescriptorNode.BooleanNode {
        return DescriptorNode.BooleanNode(
            key = key,
            path = path,
            type = type,
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            default = value[DEFAULT] as? Boolean,
            readOnly = value[READ_ONLY] as? Boolean
        )
    }
}