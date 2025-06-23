package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.JsonSchema

interface JsonSchemaParser {
    fun parse(json: String): JsonSchemaNode?
}