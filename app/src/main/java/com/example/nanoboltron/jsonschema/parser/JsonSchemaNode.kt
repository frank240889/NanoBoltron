package com.example.nanoboltron.jsonschema.parser

typealias Key = String
/**
 * This class represents a JSON Schema node [https://json-schema.org/understanding-json-schema/reference/type]
 * They contain the type of the node and its properties that can be use to describe how the UI
 * will be rendered. The current properties could be overridden by the TypedUISchema, which is the
 * equivalent of JsonUISchema but for mobile. The allOf, anyOf and oneOf are a subtype of this
 * interface as they represent a list of JsonSchemaNode.
 * Since Kotlin is a strong typed language, undefined properties from JS will be treated as typed-null
 * since we don't have an UNDEFINED type.
 */
interface JsonSchemaNode {
    /**
     * The type of node, check [UiDescriptorNode].
     */
    val type: String
}
