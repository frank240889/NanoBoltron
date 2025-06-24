package com.example.nanoboltron.jsonschema.parser

interface JsonParser {
    fun parse(json: String): JsonParserResult
}