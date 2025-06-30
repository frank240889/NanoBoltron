package com.example.nanoboltron.jsonschema.parser

import android.util.Log
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
        path: String? = null,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode {
        // Check for composition keywords first (they can coexist with other keywords)
        when {
            value.containsKey(ALL_OF) -> return handleAllOf(path, value, isRootNode)
            value.containsKey(ANY_OF) -> return handleAnyOf(key, path, value)
            value.containsKey(ONE_OF) -> return handleOneOf(key, path, value)
            value.containsKey(IF) -> return handleIfThenElse(key, path, value)
        }

        // Type inference according to JSON Schema spec
        val inferredType = inferNodeType(value)
        Log.e("inferredType", inferredType.toString())

        return when (inferredType) {
            STRING_NODE -> stringDescriptorParser.parse(STRING_NODE, key, path, value, isRootNode)

            NUMBER_NODE, INTEGER_NODE -> numberDescriptorParser.parse(
                NUMBER_NODE,
                key,
                path,
                value,
                isRootNode
            )

            BOOLEAN_NODE -> booleanDescriptorParser.parse(
                BOOLEAN_NODE,
                key,
                path,
                value,
                isRootNode
            )

            OBJECT_NODE -> parseObjectNode(key, path, value, isRootNode)

            ARRAY_NODE -> parseArrayNode(key, path, value, isRootNode)

            else -> parseObjectNode(key, path, value, isRootNode) // default to object
        }
    }

    private fun handleIfThenElse(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
    ): DescriptorNode {
        val ifSchema = value[IF] as? Map<String, Any?>
        val thenSchema = value[THEN] as? Map<String, Any?>
        val elseSchema = value[ELSE] as? Map<String, Any?>

        return DescriptorNode.ConditionalNode(
            key = key,
            path = if (path.isNullOrBlank()) null else path,
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            ifSchema = ifSchema?.let { parseNode(null, "${path.orEmpty()}.if", it, false) },
            thenSchema = thenSchema?.let { parseNode(null, "${path.orEmpty()}.then", it, false) },
            elseSchema = elseSchema?.let { parseNode(null, "${path.orEmpty()}.else", it, false) }
        )
    }

    private fun parseObjectNode(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean
    ): DescriptorNode.GroupNode {
        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean

        // For object nodes, pass the current path + key to children
        val childrenPath = if (isRootNode) {
            PROPERTIES
        } else {
            "$path.$key.$PROPERTIES"
        }
        Log.e("parseObjectNode", "key: $key, path: $path, value: $value, isRootNode: $isRootNode")
        val childrenNodes = (value[PROPERTIES] as? Map<String, Any?>).orEmpty()
        val nodes = parseProperties(childrenNodes, childrenPath)

        return DescriptorNode.GroupNode(
            type = OBJECT_NODE,
            key = key,
            path = path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun parseArrayNode(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean
    ): DescriptorNode.GroupNode {
        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean

        // For array nodes, pass the current path + key to children
        val childrenPath = if (isRootNode) {
            ITEMS
        } else {
            "$path.$key"
        }
        Log.e("parseArrayNode", "key: $key, path: $path, value: $value, isRootNode: $isRootNode")
        val items = value[ITEMS]
        val nodes = parseArrayItems(items, childrenPath)

        return DescriptorNode.GroupNode(
            type = REPEATABLE_GROUP,
            key = key,
            path = path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun handleAllOf(path: String?, value: Map<String, Any?>, isRootNode: Boolean): DescriptorNode {
        val schemas = value[ALL_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val allOfKey = ALL_OF
            val allOfPath = if (isRootNode) ALL_OF else "$path.$ALL_OF[$index]"
            parseNode(allOfKey, allOfPath, schema, false)
        }

        // For allOf, we can either merge compatible schemas or create a CompositionNode
        return if (canMergeSchemas(schemas)) {
            val mergedSchema = mergeSchemas(schemas, value)
            parseNode(ALL_OF, path, mergedSchema, false)
        } else {
            DescriptorNode.CompositionNode(
                key = "allOf",
                path = if (path.isNullOrBlank()) null else path,
                type = "composition",
                title = value[TITLE] as? String,
                description = value[DESCRIPTION] as? String,
                compositionType = ALL_OF,
                schemas = parsedSchemas
            )
        }
    }

    private fun handleAnyOf(key: Key?, path: String?, value: Map<String, Any?>): DescriptorNode {
        val schemas = value[ANY_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val anyOfKey = "anyOf"
            val anyOfPath = if (path.isNullOrEmpty()) "anyOf[$index]" else "$path.anyOf[$index]"
            parseNode(anyOfKey, anyOfPath, schema, false)
        }

        return DescriptorNode.CompositionNode(
            key = "anyOf",
            path = if (path.isNullOrEmpty()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ANY_OF,
            schemas = parsedSchemas
        )
    }

    private fun handleOneOf(key: Key?, path: String?, value: Map<String, Any?>): DescriptorNode {
        val schemas = value[ONE_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val oneOfKey = "oneOf"
            val oneOfPath = if (path.isNullOrEmpty()) "oneOf[$index]" else "$path.oneOf[$index]"
            parseNode(oneOfKey, oneOfPath, schema, false)
        }

        return DescriptorNode.CompositionNode(
            key = "oneOf",
            path = if (path.isNullOrEmpty()) null else path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ONE_OF,
            schemas = parsedSchemas
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
        (baseSchema[PROPERTIES] as? Map<String, Any?>)?.forEach { (key, value) ->
            mergedProperties[key] = value
        }

        // Merge properties from all schemas
        schemas.forEach { schema ->
            (schema[PROPERTIES] as? Map<String, Any?>)?.forEach { (key, value) ->
                mergedProperties[key] = value
            }
        }

        merged[PROPERTIES] = mergedProperties
        merged.remove(ALL_OF) // Remove the allOf since we've merged it

        return merged
    }

    private fun parseProperties(
        value: Map<String, Any?>,
        path: String?
    ): List<DescriptorNode>? {
        return value.entries.map { (rawKey, rawSchema) ->
            val fieldSchema = (rawSchema as? Map<String, Any?>).orEmpty()
            Log.e("parseProperties", "key: $rawKey, path: $path, value: $value")
            parseNode(rawKey, path, fieldSchema, false)
        }
    }

    private fun parseArrayItems(
        items: Any?,
        path: String?,
    ): List<DescriptorNode>? {
        return items.let { rawItems ->
            when (rawItems) {
                is Map<*, *> -> {
                    val itemSchema = rawItems as Map<String, Any?>
                    // Don't extract key for single items - let properties be parsed directly
                    listOfNotNull(parseNode(ITEMS, path, itemSchema, false))
                }

                is List<*> -> {
                    rawItems.mapIndexedNotNull { index, node ->
                        (node as? Map<String, Any?>)?.let { itemSchema ->
                            // For tuple arrays, don't use .items, use direct indexing
                            val itemsPath = "$path[$index]"

                            // For array items, the key should include the index
                            val itemKey = "[$index]"/*if (itemSchema.containsKey(PROPERTIES)) {
                                val propertyKey =
                                    (itemSchema[PROPERTIES] as? Map<String, Any?>)?.keys?.firstOrNull() as? String
                                "${propertyKey}[$index]"
                            } else "item[$index]"*/
                            parseNode(itemKey, itemsPath, itemSchema, false)
                        }
                    }
                }

                else -> null
            }
        }
    }

    private fun inferNodeType(value: Map<String, Any?>): String? {
        val type = value[TYPE]
        if (type != null && type is String) return type

        return when {
            value.containsKey(PROPERTIES) -> OBJECT_NODE
            value.containsKey(ITEMS) -> ARRAY_NODE
            else -> null
        }
    }
}
