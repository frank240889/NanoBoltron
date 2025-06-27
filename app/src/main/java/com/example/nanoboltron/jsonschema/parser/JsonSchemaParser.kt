package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.DESCRIPTION
import com.example.nanoboltron.jsonschema.ELSE
import com.example.nanoboltron.jsonschema.IF
import com.example.nanoboltron.jsonschema.INTEGER_NODE
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
import com.example.nanoboltron.jsonschema.parser.parsers.BooleanDescriptorParser
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import com.example.nanoboltron.jsonschema.parser.parsers.NumberDescriptorParser
import com.example.nanoboltron.jsonschema.parser.parsers.StringDescriptorParser
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaParser : JsonParser {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)
    private val stringDescriptorParser = StringDescriptorParser()
    private val numberDescriptorParser = NumberDescriptorParser()
    private val booleanDescriptorParser = BooleanDescriptorParser()

    override fun parse(schema: String): DescriptorNode? {
        val model: Map<String, Any?> = mapAdapter.fromJson(schema) ?: mapOf()
        return parseNode(value = model, isRootNode = true)
    }

    private fun parseNode(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = "",
        isRootNode: Boolean = false
    ): DescriptorNode {
        // Check for composition keywords first (they can coexist with other keywords)
        when {
            value.containsKey(ALL_OF) -> return handleAllOf(key, value, path)
            value.containsKey(ANY_OF) -> return handleAnyOf(key, value, path)
            value.containsKey(ONE_OF) -> return handleOneOf(key, value, path)
            value.containsKey(IF) -> return handleIfThenElse(key, value, path)
        }

        // Type inference according to JSON Schema spec
        val inferredType = inferNodeType(value, isRootNode)

        return when (inferredType) {
            STRING_NODE -> stringDescriptorParser.parse(STRING_NODE, key, value, path, isRootNode)

            NUMBER_NODE, INTEGER_NODE -> numberDescriptorParser.parse(
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

            OBJECT_NODE -> parseObjectNode(key, value, path, isRootNode)

            ARRAY_NODE -> parseArrayNode(key, value, path, isRootNode)

            else -> parseObjectNode(key, value, path, isRootNode) // default to object
        }
    }

    private fun parseObjectNode(
        key: Key?,
        value: Map<String, Any?>,
        path: String,
        isRootNode: Boolean
    ): DescriptorNode.GroupNode {
        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean

        val nodes = parseObjectProperties(value, path)

        return DescriptorNode.GroupNode(
            type = OBJECT_NODE,
            key = key,
            path = if (path.isBlank()) null else path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun parseArrayNode(
        key: Key?,
        value: Map<String, Any?>,
        path: String,
        isRootNode: Boolean
    ): DescriptorNode.GroupNode {
        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean

        val nodes = parseArrayItems(value, path, key)

        return DescriptorNode.GroupNode(
            type = REPEATABLE_GROUP,
            key = key,
            path = if (path.isBlank()) null else path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun handleAllOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ALL_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexedNotNull { index, schema ->
            parseNode(null, schema, "$path.allOf.$index", false)
        }

        // For allOf, we can either merge compatible schemas or create a CompositionNode
        return if (canMergeSchemas(schemas)) {
            val mergedSchema = mergeSchemas(schemas, value)
            parseNode(key, mergedSchema, path, false)
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
    }

    private fun handleAnyOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ANY_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexedNotNull { index, schema ->
            parseNode(null, schema, "$path.anyOf.$index", false)
        }

        return DescriptorNode.CompositionNode(
            key = key,
            path = if (path.isBlank()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ANY_OF,
            schemas = parsedSchemas
        )
    }

    private fun handleOneOf(key: Key?, value: Map<String, Any?>, path: String): DescriptorNode {
        val schemas = value[ONE_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexedNotNull { index, schema ->
            parseNode(null, schema, "$path.oneOf.$index", false)
        }

        return DescriptorNode.CompositionNode(
            key = key,
            path = if (path.isBlank()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ONE_OF,
            schemas = parsedSchemas
        )
    }

    private fun handleIfThenElse(
        key: Key?,
        value: Map<String, Any?>,
        path: String
    ): DescriptorNode {
        val ifSchema = value[IF] as? Map<String, Any?>
        val thenSchema = value[THEN] as? Map<String, Any?>
        val elseSchema = value[ELSE] as? Map<String, Any?>

        return DescriptorNode.ConditionalNode(
            key = key,
            path = if (path.isBlank()) null else path,
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            ifSchema = ifSchema?.let { parseNode(null, it, "$path.if", false) },
            thenSchema = thenSchema?.let { parseNode(null, it, "$path.then", false) },
            elseSchema = elseSchema?.let { parseNode(null, it, "$path.else", false) }
        )
    }

    private fun canMergeSchemas(schemas: List<Map<String, Any?>>): Boolean {
        // Simple heuristic: can merge if all schemas are objects with properties
        return schemas.all { schema ->
            schema.containsKey(PROPERTIES) &&
                    !schema.containsKey(ANY_OF) &&
                    !schema.containsKey(ONE_OF) &&
                    !schema.containsKey(ALL_OF) &&
                    !schema.containsKey(IF)
        }
    }

    private fun mergeSchemas(
        schemas: List<Map<String, Any?>>,
        baseSchema: Map<String, Any?>
    ): Map<String, Any?> {
        val merged = baseSchema.toMutableMap()
        val mergedProperties = mutableMapOf<String, Any?>()

        // Add properties from base schema
        (baseSchema[PROPERTIES] as? Map<*, *>)?.forEach { (key, value) ->
            mergedProperties[key as String] = value
        }

        // Merge properties from all schemas
        schemas.forEach { schema ->
            (schema[PROPERTIES] as? Map<*, *>)?.forEach { (key, value) ->
                mergedProperties[key as String] = value
            }
        }

        merged[PROPERTIES] = mergedProperties
        merged.remove(ALL_OF) // Remove the allOf since we've merged it

        return merged
    }

    private fun parseObjectProperties(
        value: Map<String, Any?>,
        path: String
    ): List<DescriptorNode>? {
        val newPath = if (path.isEmpty()) PROPERTIES else "$path.$PROPERTIES"

        return (value[PROPERTIES] as? Map<*, *>)?.mapNotNull { (rawKey, rawSchema) ->
            val fieldKey = rawKey as? String ?: return@mapNotNull null
            val fieldSchema = rawSchema as? Map<String, Any?> ?: return@mapNotNull null
            val type = fieldSchema[TYPE] as? String
            val isFinalNode = type == STRING_NODE ||
                    type == NUMBER_NODE ||
                    type == BOOLEAN_NODE ||
                    type == INTEGER_NODE

            val composePath = if (isFinalNode) {
                newPath
            } else {
                "$newPath.$fieldKey"
            }
            parseNode(fieldKey, fieldSchema, composePath, false)
        }
    }

    private fun parseArrayItems(
        value: Map<String, Any?>,
        path: String,
        key: Key?
    ): List<DescriptorNode>? {
        val newPath = "$path.$ITEMS"

        return (value[ITEMS])?.let { rawItems ->
            when (rawItems) {
                is Map<*, *> -> {
                    listOfNotNull(parseNode(key, rawItems as Map<String, Any?>, newPath, false))
                }

                is List<*> -> rawItems.mapIndexedNotNull { index, it ->
                    (it as? Map<String, Any?>)?.let { itemSchema ->
                        parseNode(null, itemSchema, "$newPath.$index", false)
                    }
                }

                else -> null
            }
        }
    }

    private fun inferNodeType(value: Map<String, Any?>, isRootNode: Boolean = false): String? {
        val type = value[TYPE]
        if (type != null && type is String) return type

        return when {
            value.containsKey(PROPERTIES) -> OBJECT_NODE
            value.containsKey(ITEMS) -> ARRAY_NODE
            else -> null
        }
    }
}
