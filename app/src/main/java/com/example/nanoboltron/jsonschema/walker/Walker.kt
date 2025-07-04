package com.example.nanoboltron.jsonschema.walker

interface Walker {
    fun walk(json: String, onEvent: (WalkerEvent) -> Unit)
}

sealed class WalkerEvent {
    data object OnParsed : WalkerEvent()
    data object OnStartWalking : WalkerEvent()
    data class OnTraversingNode(val key: String?, val path: String?, val data: Map<String, Any?>, val isRoot: Boolean): WalkerEvent()
    data object OnEndWalking : WalkerEvent()
}