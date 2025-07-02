package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import com.example.nanoboltron.jsonschema.core.JsonNode

interface JsonProcessor {
    fun loadSchema(name: String, jsonSchemaString: String): JsonNode?
    fun loadData(name: String, jsonDataString: String?)
    fun updateValue(key: String, value: Any, path: String?): JsonProcessorResult
    fun validate(): JsonProcessorResult
    fun addContext(context: Context)
}