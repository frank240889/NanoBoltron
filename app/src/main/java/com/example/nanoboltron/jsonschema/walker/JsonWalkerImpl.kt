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
import com.example.nanoboltron.jsonschema.parser.Key
import com.example.nanoboltron.jsonschema.parser.Path
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonWalkerImpl : Walker {
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

    override fun nodes(json: String): List<Node> {
        val data = mutableListOf<Node>()
        walk(json) { event ->
            if (event is WalkerEvent.OnTraversingNode) {
                data.add(
                    Node(
                        type = inferNodeType(event.value),
                        key = event.key,
                        value = event.value,
                        path = event.path
                    )
                )
            }
        }
        return data
    }

    override fun treeNodes(json: String): List<Node> {
        val nodes = nodes(json)
        /*val pathToNode = nodes.associateBy { it.path }
            .toMutableMap()

        nodes.sortedByDescending { it.path?.split(".")?.size ?: 0 }.forEach { node ->
            val parentPath = node.path?.substringBeforeLast('.', missingDelimiterValue = "")
            if (parentPath.isNullOrBlank()) return@forEach

            val parent = pathToNode[parentPath]
            if (parent != null) {
                val updatedParent = parent.copy(
                    children = (parent.children.orEmpty() + node)
                        .sortedBy { it.key } // opcional, por orden
                )
                pathToNode[parentPath] = updatedParent
            }
        }

        return pathToNode.values*/
        return emptyList()
    }

    private fun parse(json: String): Map<String, Any?> {
        return mapAdapter.fromJson(json) ?: mapOf()
    }

    private fun walkNode(
        key: Key?,
        value: Any?,
        path: Path?,
        isInRootNode: Boolean? = null
    ) {
        val childPath = buildChildPath(key, path, isInRootNode)

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
        val isInRootNode = path == null
        value.entries.forEach { entry ->
            val childKey = entry.key
            val childValue = entry.value
            onEvent.invoke(
                WalkerEvent.OnTraversingNode(
                    key = childKey,
                    value = value,
                    path = path,
                    isInRootNode = isInRootNode,
                )
            )
            walkNode(
                key = childKey,
                value = childValue,
                path = path,
                isInRootNode = isInRootNode
            )
        }
    }

    private fun walkArrayNode(
        value: List<Any?>,
        path: String?,
    ) {
        val isInRootNode = path == null
        value.forEachIndexed { index, childValue ->
            val childKey = "[$index]"
            onEvent.invoke(
                WalkerEvent.OnTraversingNode(
                    key = childKey,
                    value = value,
                    path = path,
                    isInRootNode = isInRootNode,
                )
            )
            walkNode(
                key = childKey,
                value = childValue,
                path = path,
                isInRootNode = path == null
            )
        }
    }

    private fun isParentNode(value: Any?): Boolean {
        return value is Map<*, *> || value is List<*>
    }

    private fun buildChildPath(
        key: Key?,
        parentPath: String?,
        isInRootNode: Boolean?
    ): String? {
        return when {
            isInRootNode == true -> null
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