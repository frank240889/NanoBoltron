package com.example.nanoboltron.jsonschema.processor

sealed class JsonProcessorResult {
    data object OnValueUpdated: JsonProcessorResult()
    data object OnDataBound: JsonProcessorResult()
}