package com.example.nanoboltron.jsonschema.datamanager

/**
 * This component is in charge to update the value of certain field.
 */
interface FieldDataHandler {
    /**
     * Finds the node of the value and updates the field following the path node.
     * @param path The path in the map structure, following the next rules:
     *          1. if the field is in the root node, the path is null
     *          2. otherwise, the structure will be as next:
     *              path.to.the.field, where every word among the dots represent a sub node
     *
     * @param value The new value of the field.
     * @return The new map containing the new values or and empty map if is no values has been set.
     */
    fun setValue(path: String?, value: Any)

    /**
     * @return The json as a string, or an empty json ({}) in case of empty map.
     */
    suspend fun asJsonString(): String

    /**
     * @return The json as a string, or an empty json ({}) in case of empty map.
     */
    fun asMap(): Map<String, Any>

    fun clear()
}