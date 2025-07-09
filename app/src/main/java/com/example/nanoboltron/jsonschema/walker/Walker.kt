package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.core.JsonNode

interface Walker {
    fun walk(json: String, onEvent: (WalkerEvent) -> Unit)
    fun flatListNodes(json: String): List<JsonNode>
}

sealed class WalkerEvent {
    data object OnStartParsing : WalkerEvent()
    data object OnParsed : WalkerEvent()
    data class OnParsingError(val message: String) : WalkerEvent()
    data object OnStartWalking : WalkerEvent()
    data class OnTraversingNode(
        val type: String,
        val key: String?,
        val value: Any?,
        val path: String?,
        val isInRoot: Boolean
    ) : WalkerEvent()

    data object OnEndWalking : WalkerEvent()
}
