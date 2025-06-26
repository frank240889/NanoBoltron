package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.parser.parsers.BooleanDescriptorParser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import com.example.nanoboltron.jsonschema.parser.parsers.GroupDescriptorParser
import com.example.nanoboltron.jsonschema.parser.parsers.NodeParser
import com.example.nanoboltron.jsonschema.parser.parsers.NumberDescriptorParser
import com.example.nanoboltron.jsonschema.parser.parsers.StringDescriptorParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaParser : JsonParser {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)
    private val groupDescriptorParser: NodeParser = GroupDescriptorParser(
        StringDescriptorParser(),
        NumberDescriptorParser(),
        BooleanDescriptorParser()
    )

    override fun parse(schema: String): DescriptorNode? {
        val model: Map<String, Any?> = mapAdapter.fromJson(schema) ?: mapOf()
        return parse(value = model)
    }

    private fun parse(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode {
        return groupDescriptorParser.parse(GROUP,key, value, path)
    }
}