package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.core.JsonNode
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonDataParser : JsonParser {
    private val moshi = Moshi
        .Builder()
        .build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any>>(type)

    override fun parse(json: String): JsonNode? {
        val nodes: Map<String, Any?> = mapAdapter.fromJson(json) ?: mapOf()
        return parseData(value = nodes)
    }

    private fun parseData(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): FormDataNode? {

        val children = value.mapNotNull { (childKey, childValue) ->
            parseValue(
                key = childKey,
                value = childValue,
                path = if (path.isEmpty()) childKey else "$path.$childKey"
            )
        }

        return FormDataNode.ObjectData(
            type = "object",
            key = key,
            path = path.ifEmpty { "" },
            children = children
        )
    }

    private fun parseValue(
        key: String?,
        value: Any?,
        path: String
    ): FormDataNode? {
        return when (value) {
            is String -> FormDataNode.StringData(
                type = "string",
                key = key,
                path = path,
                value = value
            )

            is Number -> FormDataNode.NumberData(
                type = "number",
                key = key,
                path = path,
                value = value
            )

            is Boolean -> FormDataNode.BooleanData(
                type = "boolean",
                key = key,
                path = path,
                value = value
            )

            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                parseData(
                    key = key,
                    value = value as Map<String, Any?>,
                    path = path
                )
            }

            is List<*> -> {
                val items = value.mapIndexedNotNull { index, item ->
                    val itemPath = "$path.$index"
                    parseValue(
                        key = null,
                        value = item,
                        path = itemPath
                    )
                }

                FormDataNode.ArrayData(
                    type = "array",
                    key = key,
                    path = path,
                    items = items
                )
            }

            else -> null // Unknown or null types are ignored
        }
    }

}