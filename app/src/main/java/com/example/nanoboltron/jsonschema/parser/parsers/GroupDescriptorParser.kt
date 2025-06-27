package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.DESCRIPTION
import com.example.nanoboltron.jsonschema.ELSE
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.IF
import com.example.nanoboltron.jsonschema.ITEMS
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.OBJECT_NODE
import com.example.nanoboltron.jsonschema.ONE_OF
import com.example.nanoboltron.jsonschema.PROPERTIES
import com.example.nanoboltron.jsonschema.READ_ONLY
import com.example.nanoboltron.jsonschema.REPEATABLE_GROUP
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.THEN
import com.example.nanoboltron.jsonschema.TITLE
import com.example.nanoboltron.jsonschema.TYPE
import com.example.nanoboltron.jsonschema.WRITE_ONLY
import com.example.nanoboltron.jsonschema.parser.Key
import javax.inject.Inject

class GroupDescriptorParser @Inject constructor(
    private val stringDescriptorParser: NodeParser,
    private val numberDescriptorParser: NodeParser,
    private val booleanDescriptorParser: NodeParser,
) : NodeParser {
    override fun parse(
        type: String,
        key: Key?,
        value: Map<String, Any?>,
        path: String,
        isRootNode: Boolean
    ): DescriptorNode {
        // Handle special composition keywords first (they take precedence)
        when {
            value.containsKey(ALL_OF) -> return handleAllOf(key, value, path)
            value.containsKey(ANY_OF) -> return handleAnyOf(key, value, path)
            value.containsKey(ONE_OF) -> return handleOneOf(key, value, path)
            value.containsKey(IF) -> return handleIfThenElse(key, value, path)
        }

        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean
        var nodes: List<DescriptorNode>? = null
        var newType = type

        // Determine the actual type based on JSON Schema RFC logic
        val actualType = when {
            // Explicit type takes precedence
            value.containsKey(TYPE) -> value[TYPE] as? String
            // For root node, infer type from content
            isRootNode -> {
                when {
                    value.containsKey(PROPERTIES) -> OBJECT_NODE
                    value.containsKey(ITEMS) -> ARRAY_NODE
                    else -> null
                }
            }
            // For non-root nodes, default to object if no type specified
            else -> OBJECT_NODE
        }

        when (actualType) {
            OBJECT_NODE, null -> {
                newType = OBJECT_NODE
                val newPath = if (path.isEmpty()) {
                    PROPERTIES
                } else {
                    "$path.$PROPERTIES"
                }
                nodes = (value[PROPERTIES] as? Map<*, *>)
                    ?.mapNotNull { (rawKey, rawSchema) ->
                        val fieldKey = rawKey as? String ?: return@mapNotNull null
                        val fieldSchema = rawSchema as? Map<String, Any?> ?: return@mapNotNull null
                        val composePath = "$newPath.$fieldKey"
                        parseNode(GROUP, fieldKey, fieldSchema, composePath)
                    }
            }

            ARRAY_NODE -> {
                newType = REPEATABLE_GROUP
                val newPath = "$path.$ITEMS"
                nodes = (value[ITEMS])
                    ?.let { rawItems ->
                        when (rawItems) {
                            is Map<*, *> -> {
                                listOfNotNull(
                                    parseNode(
                                        GROUP,
                                        key,
                                        rawItems as Map<String, Any?>,
                                        newPath
                                    )
                                )
                            }

                            is List<*> -> rawItems.mapIndexedNotNull { index, it ->
                                (it as? Map<String, Any?>)?.let { itemSchema ->
                                    parseNode(GROUP, null, itemSchema, "$newPath.$index")
                                }
                            }

                            else -> null
                        }
                    }
            }

            else -> {
                // Handle other types that might be at root level
                return when (actualType) {
                    STRING_NODE -> stringDescriptorParser.parse(
                        STRING_NODE,
                        key,
                        value,
                        path,
                        isRootNode
                    )

                    NUMBER_NODE -> numberDescriptorParser.parse(
                        NUMBER_NODE,
                        key,
                        value,
                        path,
                        isRootNode
                    )

                    BOOLEAN_NODE -> booleanDescriptorParser.parse(
                        BOOLEAN_NODE,
                        key,
                        value,
                        path,
                        isRootNode
                    )

                    else -> createEmptyGroup(key, path)
                }
            }
        }

        return DescriptorNode.GroupNode(
            type = newType,
            key = key,
            path = if (path.isBlank()) null else path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun parseNode(
        type: String,
        key: Key?,
        value: Map<String, Any?>,
        path: String
    ): DescriptorNode? {
        // Handle special composition keywords first
        when {
            value.containsKey(ALL_OF) -> return handleAllOf(key, value, path)
            value.containsKey(ANY_OF) -> return handleAnyOf(key, value, path)
            value.containsKey(ONE_OF) -> return handleOneOf(key, value, path)
            value.containsKey(IF) -> return handleIfThenElse(key, value, path)
        }

        // Handle regular types
        return when (value[TYPE]) {
            STRING_NODE -> stringDescriptorParser.parse(STRING_NODE, key, value, path, false)
            NUMBER_NODE -> numberDescriptorParser.parse(NUMBER_NODE, key, value, path, false)
            BOOLEAN_NODE -> booleanDescriptorParser.parse(BOOLEAN_NODE, key, value, path, false)
            OBJECT_NODE, null -> parse(type, key, value, path, false)
            ARRAY_NODE -> parse(type, key, value, path, false)
            else -> null
        }
    }

    private fun handleAllOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ALL_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapNotNull { schema ->
            parseNode(GROUP, null, schema, "$path.allOf")
        }

        // If we can merge all schemas into one, return a merged GroupNode
        // Otherwise return a CompositionNode for UI to handle
        val allOfSchemas = if (parsedSchemas.all { it is DescriptorNode.GroupNode }) {
            val mergedSchema = mergeSchemas(schemas)
            parseNode(GROUP, key, mergedSchema, path) ?: createEmptyGroup(key, path)
        } else {
            DescriptorNode.CompositionNode(
                key = key,
                path = if (path.isBlank()) null else path,
                type = "composition",
                title = value[TITLE] as? String,
                description = value[DESCRIPTION] as? String,
                compositionType = ALL_OF,
                schemas = parsedSchemas
            )
        }
        //Log.e("allOfSchemas", allOfSchemas.toString())
        return allOfSchemas
    }

    private fun handleAnyOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ANY_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexedNotNull { index, schema ->
            parseNode(GROUP, null, schema, "$path.anyOf.$index")
        }
        val anyOfSchemas = DescriptorNode.CompositionNode(
            key = key,
            path = if (path.isBlank()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ANY_OF,
            schemas = parsedSchemas
        )
        //Log.e("anyOfSchemas", anyOfSchemas.toString())
        return anyOfSchemas
    }

    private fun handleOneOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ONE_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexedNotNull { index, schema ->
            parseNode(GROUP, null, schema, "$path.oneOf.$index")
        }
        val oneOfSchemas = DescriptorNode.CompositionNode(
            key = key,
            path = if (path.isBlank()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ONE_OF,
            schemas = parsedSchemas
        )
        //Log.e("oneOfSchemas", oneOfSchemas.toString())
        return oneOfSchemas
    }

    private fun handleIfThenElse(
        key: Key?,
        value: Map<String, Any?>,
        path: String
    ): DescriptorNode {
        val ifSchema = value[IF] as? Map<String, Any?>
        val thenSchema = value[THEN] as? Map<String, Any?>
        val elseSchema = value[ELSE] as? Map<String, Any?>

        val ifThenElseSchemas = DescriptorNode.ConditionalNode(
            key = key,
            path = if (path.isBlank()) null else path,
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            ifSchema = ifSchema?.let { parseNode(GROUP, null, it, "$path.if") },
            thenSchema = thenSchema?.let { parseNode(GROUP, null, it, "$path.then") },
            elseSchema = elseSchema?.let { parseNode(GROUP, null, it, "$path.else") }
        )

        //Log.e("ifThenElseSchemas", ifThenElseSchemas.toString())
        return ifThenElseSchemas
    }

    private fun mergeSchemas(schemas: List<Map<String, Any?>>): Map<String, Any?> {
        val merged = mutableMapOf<String, Any?>()
        schemas.forEach { schema ->
            schema.forEach { (key, value) ->
                when (key) {
                    PROPERTIES -> {
                        val existingProps = merged[PROPERTIES] as? Map<String, Any?> ?: emptyMap()
                        val newProps = value as? Map<String, Any?> ?: emptyMap()
                        merged[PROPERTIES] = existingProps + newProps
                    }

                    else -> merged[key] = value
                }
            }
        }
        return merged
    }

    private fun createEmptyGroup(key: Key?, path: String): DescriptorNode.GroupNode {
        return DescriptorNode.GroupNode(
            key = key,
            path = if (path.isBlank()) null else path
        )
    }
}
