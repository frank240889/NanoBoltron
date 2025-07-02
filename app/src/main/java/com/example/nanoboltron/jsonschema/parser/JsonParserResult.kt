package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.JsonNode

sealed class JsonParserResult {
    data class Success(val rootNode: JsonNode) : JsonParserResult()
    data class Error(val message: String) : JsonParserResult()
}