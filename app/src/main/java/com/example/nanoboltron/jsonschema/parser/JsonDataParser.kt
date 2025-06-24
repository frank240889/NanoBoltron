package com.example.nanoboltron.jsonschema.parser

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.IOException

class JsonDataParser : JsonParser {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any>>(type)
    private var data: Map<String, Any>? = null

    override fun parse(json: String): JsonParserResult {
        return try {
            data = mapAdapter.fromJson(json)
            if (data != null) {
                JsonParserResult.Success(data!!)
            } else {
                JsonParserResult.Error("Failed to parse JSON")
            }

        } catch (exception: IOException) {
            JsonParserResult.Error(exception.message?.orEmpty())
        }
    }
}