package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

class JsonSchemaParserWithWalker() : Parser {

    override fun parse(schema: String): DescriptorNode? {

        return DescriptorNode.GroupNode()
    }
}
