package com.example.nanoboltron.jsonschema.parser

import java.io.IOException
import kotlin.jvm.Throws

interface JsonParser {
    @Throws(IOException::class)
    fun parse(json: String): JsonNode?
}