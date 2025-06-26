package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import com.example.nanoboltron.jsonschema.parser.JsonParserResult

interface JsonProcessor {
    fun loadSchema(name: String, jsonSchemaString: String)
    fun loadData(name: String, jsonDataString: String?)
    fun updateValue(key: String, value: Any, path: String?): JsonProcessorResult
    fun validate(): JsonProcessorResult
    fun addContext(context: Context)
}