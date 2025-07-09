package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.core.JsonNode

data class FlatNode(
    override val type: String?,
    override val key: String?,
    override val path: String?,
    val isInRoot: Boolean,
    val data: Any?
): JsonNode