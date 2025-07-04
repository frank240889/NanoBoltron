package com.example.nanoboltron.jsonschema.walker

import android.util.Log
import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.CONST
import com.example.nanoboltron.jsonschema.IF
import com.example.nanoboltron.jsonschema.INTEGER_NODE
import com.example.nanoboltron.jsonschema.ITEMS
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.OBJECT_NODE
import com.example.nanoboltron.jsonschema.ONE_OF
import com.example.nanoboltron.jsonschema.PROPERTIES
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.TYPE
import com.example.nanoboltron.jsonschema.parser.Key
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaWalker : Walker {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)
    private lateinit var onEvent: (WalkerEvent) -> Unit

    override fun walk(
        json: String,
        onEvent: (WalkerEvent) -> Unit
    ) {
        this.onEvent = onEvent
        val data: Map<String, Any?> = mapAdapter.fromJson(json) ?: mapOf()
        parseNode(data = data, isRootNode = true)
    }

    private fun parseNode(
        key: Key? = null,
        path: String? = null,
        data: Map<String, Any?>,
        isRootNode: Boolean
    ) {
        data.entries.forEach { entry ->
            when (entry.value) {
                is Map<*, *> -> {
                    parseNode(
                        key,
                        path,
                        (entry.value as? Map<String, Any?>).orEmpty(),
                        isRootNode
                    )
                }
                is List<*> -> {
                    traverseArray(
                        key,
                        path,
                        (entry.value as? List<Any?>).orEmpty(),
                        isRootNode
                    )
                }
                else -> {
                    key?.let { nonNullableKey ->
                        handleTerminalNode(nonNullableKey, path, data, false)
                    }
                }
            }
        }
    }

    private fun traverseArray(
        key: Key?,
        path: String?,
        data: Any,
        isRootNode: Boolean
    ) {
        TODO("Not yet implemented")
    }

    private fun handleTerminalNode(
        key: Key,
        path: String?,
        data: Any,
        isRootNode: Boolean
    ) {
        onEvent.invoke(
            WalkerEvent.OnTraversingNode(
                key,
                buildPath(
                    key = key,
                    parentPath = path,
                    isRootNode = isRootNode
                ),
                data,
                isRootNode
            )
        )
    }

    private fun handleObjectNode(
        key: Key?,
        path: String?,
        data: Map<String, Any?>,
        isRootNode: Boolean
    ) {

    }

    private fun handleIfThenElse(
        key: Key?,
        path: String?,
        data: Map<String, Any?>,
        isRootNode: Boolean
    ) {

    }

    private fun inferNodeType(value: Map<String, Any?>): String? {
        val type = value[TYPE]
        if (type != null && type is String) return type

        return when {
            value.containsKey(PROPERTIES) -> OBJECT_NODE
            value.containsKey(ITEMS) -> ARRAY_NODE
            value.containsKey(CONST) -> checkValue(value[CONST])
            else -> null
        }
    }

    private fun checkValue(any: Any?): String? {
        return when (any) {
            is Int -> INTEGER_NODE
            is String -> STRING_NODE
            is Boolean -> BOOLEAN_NODE
            is Double -> NUMBER_NODE
            is List<*> -> ARRAY_NODE
            is Map<*, *> -> OBJECT_NODE
            else -> null
        }
    }

    private fun buildPath(
        key: Key?,
        parentPath: String?,
        isRootNode: Boolean
    ): String? {
        return when {
            isRootNode -> null
            parentPath.isNullOrBlank() && key != null -> key
            key != null -> "$parentPath.$key"
            else -> parentPath
        }
    }
}