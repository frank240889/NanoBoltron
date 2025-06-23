package com.example.nanoboltron.jsonschema.parser

/**
 * A node that represents subschemas, those ones like anyOf, allOf and oneOf.
 * This type of node contains a list of subschemas, that in turn, they are translated into a list
 * of [JsonSchemaNode]. Nodes like these don't introduce new nesting levels in the data path.
 */

sealed class Operator : JsonSchemaNode {
    data class Logical(
        override val type: String,
        val nodes: List<JsonSchemaNode>,
    ) : Operator()

    data class Decision(
        override val type: String,
        val ifSchema: JsonSchemaNode,
        val thenSchema: JsonSchemaNode?,
        val elseSchema: JsonSchemaNode?
    ) : Operator()
}
