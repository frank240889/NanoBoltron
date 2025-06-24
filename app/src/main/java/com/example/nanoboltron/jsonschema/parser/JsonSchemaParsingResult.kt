package com.example.nanoboltron.jsonschema.parser

sealed class JsonSchemaParsingResult {
    data class Success(val rootNode: UiDescriptorNode) : JsonSchemaParsingResult()
    data class Error(val message: String) : JsonSchemaParsingResult()
}