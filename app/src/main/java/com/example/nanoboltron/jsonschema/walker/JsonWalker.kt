package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.INTEGER_NODE
import com.example.nanoboltron.jsonschema.LONG_NODE
import com.example.nanoboltron.jsonschema.UNTYPED_NULL_NODE
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.OBJECT_NODE
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.UNDEFINED_NODE
import com.example.nanoboltron.jsonschema.core.Native
import com.example.nanoboltron.jsonschema.core.Key
import com.example.nanoboltron.jsonschema.core.Path
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonWalker : Walker {
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
        try {
            onEvent(WalkerEvent.OnStartParsing)
            val parsedData = parse(json)
            onEvent(WalkerEvent.OnParsed)
            onEvent(WalkerEvent.OnStartWalking)
            walkNode(
                key = null,
                value = parsedData,
                path = null
            )
            onEvent(WalkerEvent.OnEndWalking)
        } catch (e: JsonDataException) {
            onEvent(WalkerEvent.OnParsingError(e.message.orEmpty()))
        }
    }

    override fun flatListNodes(json: String): List<FlatNode> {
        val data = mutableListOf<FlatNode>()
        walk(json) { event ->
            if (event is WalkerEvent.OnTraversingNode) {
                data.add(
                    FlatNode(
                        key = event.key,
                        type = event.type,
                        path = event.path,
                        isInRoot = event.isInRoot,
                        data = event.value
                    )
                )
            }
        }
        return data
    }

    private fun parse(json: String): Map<String, Any?> {
        return mapAdapter.fromJson(json) ?: mapOf()
    }

    private fun walkNode(
        key: Key?,
        value: Any?,
        path: Path?,
    ) {
        val childPath = buildChildPath(key, path)

        when (value) {
            is Map<*, *> -> {
                walkObjectNode(
                    (value as? Map<String, Any?>).orEmpty(),
                    childPath,
                )
            }

            is List<*> -> {
                walkArrayNode(
                    (value as? List<Any?>).orEmpty(),
                    childPath,
                )
            }
        }
    }

    private fun walkObjectNode(
        value: Map<String, Any?>,
        path: Path?,
    ) {
        val isInRoot = path == null
        value.entries.forEach { entry ->
            val childKey = entry.key
            val childValue = entry.value
            onEvent.invoke(
                WalkerEvent.OnTraversingNode(
                    type = inferNodeType(childValue),
                    key = childKey,
                    value = value,
                    path = path,
                    isInRoot = isInRoot
                )
            )
            walkNode(
                key = childKey,
                value = childValue,
                path = path
            )
        }
    }

    private fun walkArrayNode(
        value: List<Any?>,
        path: String?,
    ) {
        val isInRoot = path == null
        value.forEachIndexed { index, childValue ->
            val childKey = "[$index]"
            onEvent.invoke(
                WalkerEvent.OnTraversingNode(
                    type = inferNodeType(childValue),
                    key = childKey,
                    value = value,
                    path = path,
                    isInRoot = isInRoot
                )
            )
            walkNode(
                key = childKey,
                value = childValue,
                path = path
            )
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

    private fun inferNodeType(value: Any?): String {
        return when (value) {
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