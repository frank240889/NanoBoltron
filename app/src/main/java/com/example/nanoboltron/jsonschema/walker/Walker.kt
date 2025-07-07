package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.Key

interface Walker {
    fun walk(json: String, onEvent: (WalkerEvent) -> Unit)
    fun nodes(json: String): List<Node> {
        return emptyList()
    }

    fun treeNodes(json: String): List<Node> {
        return emptyList()
    }
}

sealed class WalkerEvent {
    data object OnStartParsing : WalkerEvent()
    data object OnParsed : WalkerEvent()
    data class OnParsingError(val message: String) : WalkerEvent()
    data object OnStartWalking : WalkerEvent()
    data class OnTraversingNode(
        val key: String?,
        val value: Any?,
        val path: String?,
        val isInRootNode: Boolean
    ) : WalkerEvent()

    data object OnEndWalking : WalkerEvent()
}


data class Node(
    override val type: Key,
    val key: String?,
    val value: Any?,
    val path: String?,
    val children: List<Node>? = null
): JsonNode
