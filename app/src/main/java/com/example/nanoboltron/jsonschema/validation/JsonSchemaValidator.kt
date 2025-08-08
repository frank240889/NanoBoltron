package com.example.nanoboltron.jsonschema.validation

import com.example.nanoboltron.jsonschema.validation.JsonSchema

interface JsonSchemaValidator {
    fun validate(
        schema: JsonSchema,
        data: Map<String, Any?>
    ): Map<String, String?> // field -> error
}