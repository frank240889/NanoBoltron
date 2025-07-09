package com.example.nanoboltron.jsonschema.core

sealed class Native: JsonNode {
    abstract val isInRoot: Boolean
    abstract val key: String?
    abstract val path: String?

    data class Primitive(
        override val type: Key,
        override val key: String?,
        val value: Any?,
        override val path: String?,
        override val isInRoot: Boolean
    ) : Native()

    data class Object(
        override val type: Key,
        override val key: String?,
        override val path: String?,
        override val isInRoot: Boolean,
        val children: Map<Key, JsonNode>? = null
    ) : Native()

    data class Array(
        override val type: Key,
        override val key: String?,
        override val path: String?,
        override val isInRoot: Boolean,
        val children: List<JsonNode>? = null
    ) : Native()
}
