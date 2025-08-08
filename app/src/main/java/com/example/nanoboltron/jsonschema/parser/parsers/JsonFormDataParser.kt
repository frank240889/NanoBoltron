package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.INTEGER_NODE
import com.example.nanoboltron.jsonschema.LONG_NODE
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.OBJECT_NODE
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.UNDEFINED_NODE
import com.example.nanoboltron.jsonschema.UNTYPED_NULL_NODE
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.core.Native.Array
import com.example.nanoboltron.jsonschema.core.Native.Object
import com.example.nanoboltron.jsonschema.core.Native.Primitive
import com.example.nanoboltron.jsonschema.core.Key
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.core.Path
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonFormDataParser : JsonParser {
    private val moshi = Moshi.Builder().build()
    private val objectType =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val listType = Types.newParameterizedType(List::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(objectType)
    private val listAdapter = moshi.adapter<List<Any?>>(listType)

    override fun parse(json: String): JsonNode? {
        val objectData = parseJsonObject(json)
        val listData = parseJsonList(json)
        val parsedData = when {
            objectData != null -> objectData
            listData != null -> listData
            else -> null
        }
        return if (parsedData == null) {
            null
        } else {
            buildTreeNode(parsedData, null, null)
        }
    }

    private fun parseJsonObject(json: String): Map<String, Any?>? {
        return try {
            mapAdapter.fromJson(json) ?: mapOf()
        } catch (e: JsonDataException) {
            null
        }

    }

    private fun parseJsonList(json: String): List<Any?>? {
        return try {
            listAdapter.fromJson(json) ?: listOf()
        } catch (e: JsonDataException) {
            null
        }
    }

    private fun buildTreeNode(value: Any?, key: Key?, path: Path?): JsonNode {
        val childPath = buildChildPath(key, path)
        val nodeType = inferNodeType(value)

        return when (value) {
            is Map<*, *> -> {
                val objectValue = value as? Map<String, Any?> ?: mapOf()
                val children = objectValue.entries.associate { entry ->
                    entry.key to buildTreeNode(entry.value, entry.key, childPath)
                }
                Object(
                    type = nodeType,
                    key = key,
                    path = path,
                    isInRoot = path == null,
                    children = children
                )
            }

            is List<*> -> {
                val arrayValue = value as? List<Any?> ?: listOf()
                val children = arrayValue.mapIndexed { index, childValue ->
                    buildTreeNode(childValue, "[$index]", childPath)
                }
                Array(
                    type = nodeType,
                    key = key,
                    path = path,
                    isInRoot = path == null,
                    children = children
                )
            }

            else -> {
                Primitive(
                    type = nodeType,
                    key = key,
                    value = value,
                    path = path,
                    isInRoot = path == null
                )
            }
        }
    }

    private fun buildChildPath(
        key: Key?,
        parentPath: String?,
    ): String? {
        return when {
            parentPath == null && key != null -> key
            parentPath != null && key != null -> "$parentPath.$key"
            else -> parentPath
        }
    }

    private fun inferNodeType(any: Any?): String {
        return when (any) {
            is Int -> INTEGER_NODE
            is String -> STRING_NODE
            is Boolean -> BOOLEAN_NODE
            is Double -> NUMBER_NODE
            is Long -> LONG_NODE
            is List<*> -> ARRAY_NODE
            is Map<*, *> -> OBJECT_NODE
            null -> UNTYPED_NULL_NODE
            else -> UNDEFINED_NODE
        }
    }
}