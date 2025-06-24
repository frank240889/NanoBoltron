package com.example.nanoboltron.jsonschema.parser

sealed class JsonParserResult {
    data class Success(val rootNode: JsonNode) : JsonParserResult()
    data class Error(val message: String) : JsonParserResult()
}