package com.example.nanoboltron.jsonschema.parser

import java.math.BigInteger

sealed class DataNode: JsonNode {
    abstract val key: Key?

    data class StringNode(
        override val type: String?,
        override val key: Key?,
        val value: String
    ): DataNode()

    data class NumberNode(
        override val type: String?,
        override val key: Key?,
        val value: Number
    ): DataNode()

    data class BigIntegerNode(
        override val type: String?,
        override val key: Key?,
        val value: BigInteger
    ): DataNode()

    data class BooleanNode(
        override val type: String?,
        override val key: Key?,
        val value: Boolean
    ): DataNode()
}