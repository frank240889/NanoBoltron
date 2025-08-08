package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import com.example.nanoboltron.jsonschema.core.JsonNode

interface JsonProcessor {
    fun loadSchema(name: String, jsonSchemaString: String, context: Context): JsonNode?
    fun loadData(name: String, jsonDataString: String?, context: Context)
    fun updateValue(key: String, value: Any, path: String?): JsonProcessorResult
    fun validate(): JsonProcessorResult
}