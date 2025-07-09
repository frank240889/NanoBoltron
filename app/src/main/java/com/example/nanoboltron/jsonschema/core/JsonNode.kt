package com.example.nanoboltron.jsonschema.core

/**
 * This class represents a JSON node.
 */
interface JsonNode {
    /**
     * The native type of node, it can be an array, a boolean, a number, an object, a string or null.
     */
    val type: Type?
    /**
     * The key of the node. Usually represents the name of the node.
     */
    val key: Key?
    /**
     * The path to get to the node in the format path.to.the.node where every word between the dots
     * represent one level down in the data structure, commonly a map, null indicates the node is in
     * the root node.
     */
    val path: Path?
}
