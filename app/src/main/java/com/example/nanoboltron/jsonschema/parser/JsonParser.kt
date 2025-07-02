package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.JsonNode
import java.io.IOException
import kotlin.jvm.Throws

interface JsonParser {
    @Throws(IOException::class)
    fun parse(json: String): JsonNode?
}