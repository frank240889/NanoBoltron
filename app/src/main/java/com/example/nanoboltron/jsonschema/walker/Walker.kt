package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.core.JsonNode

interface Walker {
    fun walk(json: String, onEvent: (WalkerEvent) -> Unit)
}

sealed class WalkerEvent {
    data object OnStartWalking : WalkerEvent()
    data class OnEnterNode(val node: JsonNode): WalkerEvent()
    data class OnExitNode(val node: JsonNode): WalkerEvent()
    data object OnEndWalking : WalkerEvent()
}