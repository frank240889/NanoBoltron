package com.example.nanoboltron.jsonschema.processor

interface JsonProcessor {
    fun loadSchema(name: String, jsonSchemaString: String)
    fun loadData(name: String, jsonDataString: String?)
    fun updateValue(key: String, value: Any, path: String?): Result
    fun validate(): Result
}