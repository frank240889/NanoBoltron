package com.example.nanoboltron.jsonschema.parser

sealed class FormDataNode : JsonNode {
    abstract val key: Key?
    abstract val path: String?

    data class StringData(
        override val type: String?,
        override val key: String?,
        override val path: String,
        val value: String
    ) : FormDataNode()

    data class NumberData(
        override val type: String?,
        override val key: String?,
        override val path: String,
        val value: Number
    ) : FormDataNode()

    data class BooleanData(
        override val type: String?,
        override val key: String?,
        override val path: String,
        val value: Boolean
    ) : FormDataNode()

    data class ObjectData(
        override val type: String?,
        override val key: String?,
        override val path: String,
        val children: List<FormDataNode>
    ) : FormDataNode()

    data class ArrayData(
        override val type: String?,
        override val key: String?,
        override val path: String,
        val items: List<FormDataNode>
    ) : FormDataNode()
}