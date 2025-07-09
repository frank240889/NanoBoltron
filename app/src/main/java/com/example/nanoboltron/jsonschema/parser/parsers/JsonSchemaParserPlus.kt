package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.CONST
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
import com.example.nanoboltron.jsonschema.parser.Parser
import com.example.nanoboltron.jsonschema.core.Key
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaParserPlus : Parser {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)
    private val stringDescriptorParser = StringDescriptorParser()
    private val numberDescriptorParser = NumberDescriptorParser()
    private val booleanDescriptorParser = BooleanDescriptorParser()

    /**
     * Centralized path building for JSON Schema traversal
     * Handles the inconsistent rules of JSON Schema path construction
     */
    private fun buildPath(
        parentPath: String?,
        key: Key?,
        schemaContext: String,
        isRootNode: Boolean = false
    ): String? {
        return when {
            isRootNode -> null
            parentPath.isNullOrBlank() && key != null -> key
            parentPath.isNullOrBlank() -> schemaContext
            key != null -> "$parentPath.$key"
            else -> parentPath
        }
    }

    /**
     * Builds child path for container nodes (objects, arrays, compositions)
     */
    private fun buildChildPath(
        parentPath: String?,
        key: Key?,
        containerType: String,
        isRootNode: Boolean = false
    ): String {
        return when {
            isRootNode -> containerType
            parentPath.isNullOrBlank() && key != null -> "$key.$containerType"
            parentPath.isNullOrBlank() -> containerType
            key != null -> "$parentPath.$key.$containerType"
            else -> "$parentPath.$containerType"
        }
    }

    override fun parse(schema: String): DescriptorNode? {
        return try {
            val model: Map<String, Any?> = mapAdapter.fromJson(schema) ?: mapOf()
            if (model.isEmpty()) {
                return null
            }
            parseNode(value = model, isRootNode = true)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Validates and normalizes JSON Schema structure
     * Handles common inconsistencies in JSON Schema definitions
     */
    private fun validateAndNormalizeSchema(value: Map<String, Any?>): Map<String, Any?> {
        val normalized = value.toMutableMap()

        // Handle boolean schemas (true/false shorthand)
        if (normalized.size == 1 && normalized.containsKey("type") && normalized["type"] == true) {
            return mapOf("type" to "object") // true schema accepts anything, default to object
        }

        // Ensure consistent array items structure
        normalized[ITEMS]?.let { items ->
            if (items is Boolean && items) {
                normalized[ITEMS] = mapOf<String, Any?>() // empty schema allows anything
            }
        }

        // Handle legacy array syntax
        if (normalized.containsKey("items") && normalized.containsKey("additionalItems")) {
        }

        return normalized
    }

    private fun parseNode(
        key: Key? = null,
        path: String? = null,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode {
        val normalizedValue = validateAndNormalizeSchema(value)

        // Check for composition keywords first (they can coexist with other keywords)
        when {
            normalizedValue.containsKey(ALL_OF) -> return handleAllOf(
                key,
                path,
                normalizedValue,
                isRootNode
            )

            normalizedValue.containsKey(ANY_OF) -> return handleAnyOf(
                key,
                path,
                normalizedValue,
                isRootNode
            )

            normalizedValue.containsKey(ONE_OF) -> return handleOneOf(
                key,
                path,
                normalizedValue,
                isRootNode
            )

            normalizedValue.containsKey(IF) -> return handleIfThenElse(
                key,
                path,
                normalizedValue,
                isRootNode
            )
        }

        // Type inference according to JSON Schema spec
        val inferredType = inferNodeType(normalizedValue)
        // Debug logging can be enabled if needed
        // println("inferredType: $inferredType")

        return when (inferredType) {
            STRING_NODE -> stringDescriptorParser.parse(
                STRING_NODE,
                key,
                path,
                normalizedValue,
                isRootNode
            )

            NUMBER_NODE, INTEGER_NODE -> numberDescriptorParser.parse(
                NUMBER_NODE,
                key,
                path,
                normalizedValue,
                isRootNode
            )

            BOOLEAN_NODE -> booleanDescriptorParser.parse(
                BOOLEAN_NODE,
                key,
                path,
                normalizedValue,
                isRootNode
            )

            OBJECT_NODE -> parseObjectNode(key, path, normalizedValue, isRootNode)

            ARRAY_NODE -> parseArrayNode(key, path, normalizedValue, isRootNode)

            else -> parseObjectNode(key, path, normalizedValue, isRootNode) // default to object
        }
    }

    private fun handleIfThenElse(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode {
        val ifSchema = value[IF] as? Map<String, Any?>
        val thenSchema = value[THEN] as? Map<String, Any?>
        val elseSchema = value[ELSE] as? Map<String, Any?>

        return DescriptorNode.ConditionalNode(
            key = key,
            path = if (path.isNullOrBlank()) null else path,
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            ifSchema = ifSchema?.let {
                parseNode(
                    null,
                    buildPath(path, null, "if", isRootNode),
                    it,
                    false
                )
            },
            thenSchema = thenSchema?.let {
                parseNode(
                    null,
                    buildPath(path, null, "then", isRootNode),
                    it,
                    false
                )
            },
            elseSchema = elseSchema?.let {
                parseNode(
                    null,
                    buildPath(path, null, "else", isRootNode),
                    it,
                    false
                )
            }
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
        val childrenPath = buildChildPath(path, key, PROPERTIES, isRootNode)
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
        val childrenPath = buildChildPath(path, key, ITEMS, isRootNode)
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

    private fun handleAllOf(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean
    ): DescriptorNode {
        val schemas = value[ALL_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val allOfPath = buildPath(path, key, ALL_OF, isRootNode)
            val childKey = if (
                schema.containsKey(ALL_OF) || schema.containsKey(ANY_OF) ||
                schema.containsKey(ONE_OF) || schema.containsKey(IF)
            ) {
                null // Let composition/conditional nodes determine their own key
            } else {
                "$ALL_OF[$index]" // Use composition type + indexed key for regular schemas
            }
            val childPath = "$allOfPath.$ALL_OF[$index]" // All children get indexed paths
            parseNode(childKey, childPath, schema, false)
        }

        return DescriptorNode.CompositionNode(
            key = key ?: ALL_OF,
            path = path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ALL_OF,
            schemas = parsedSchemas
        )

    }

    private fun handleAnyOf(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode {
        val schemas = value[ANY_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val anyOfPath = buildPath(path, key, ANY_OF, isRootNode)
            val childKey = if (
                schema.containsKey(ALL_OF) || schema.containsKey(ANY_OF) ||
                schema.containsKey(ONE_OF) || schema.containsKey(IF)
            ) {
                null // Let composition/conditional nodes determine their own key
            } else {
                "$ANY_OF[$index]" // Use composition type + indexed key for regular schemas
            }
            val childPath = "$anyOfPath.$ANY_OF[$index]" // All children get indexed paths
            parseNode(childKey, childPath, schema, false)
        }

        return DescriptorNode.CompositionNode(
            key = key ?: ANY_OF,
            path = path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ANY_OF,
            schemas = parsedSchemas
        )
    }

    private fun handleOneOf(
        key: Key?,
        path: String?,
        value: Map<String, Any?>,
        isRootNode: Boolean = false
    ): DescriptorNode {
        val schemas = value[ONE_OF] as? List<Map<String, Any?>> ?: emptyList()
        val parsedSchemas = schemas.mapIndexed { index, schema ->
            val oneOfPath = buildPath(path, key, ONE_OF, isRootNode)
            val childKey = if (
                schema.containsKey(ALL_OF) || schema.containsKey(ANY_OF) ||
                schema.containsKey(ONE_OF) || schema.containsKey(IF)
            ) {
                null // Let composition/conditional nodes determine their own key
            } else {
                "$ONE_OF[$index]" // Use composition type + indexed key for regular schemas
            }
            val childPath = "$oneOfPath.$ONE_OF[$index]" // All children get indexed paths
            parseNode(childKey, childPath, schema, false)
        }

        return DescriptorNode.CompositionNode(
            key = key ?: ONE_OF,
            path = path,
            type = "composition",
            title = value[TITLE] as? String,
            description = value[DESCRIPTION] as? String,
            compositionType = ONE_OF,
            schemas = parsedSchemas
        )
    }

    private fun parseProperties(
        value: Map<String, Any?>,
        childrenPath: String?
    ): List<DescriptorNode>? {
        return value.entries.map { (rawKey, rawSchema) ->
            val fieldSchema = (rawSchema as? Map<String, Any?>).orEmpty()
            // Child properties should inherit parent path, handling both .properties and indexed paths
            val parentPath = if (childrenPath?.endsWith(".properties") == true) {
                childrenPath.removeSuffix(".properties")
            } else {
                childrenPath // For indexed paths like "oneOf[0]", keep as is
            }
            parseNode(rawKey, parentPath, fieldSchema, false)
        }
    }

    private fun parseArrayItems(
        items: Any?,
        childrenPath: String?,
    ): List<DescriptorNode>? {
        return when (items) {
            is Map<*, *> -> {
                // Single schema for all array items - container keeps .items path
                val itemSchema = items as Map<String, Any?>
                listOfNotNull(parseNode(null, childrenPath, itemSchema, false))
            }

            is List<*> -> {
                // Tuple validation - different schema for each position
                val parentPath = childrenPath?.removeSuffix(".items")
                items.mapIndexedNotNull { index, itemNode ->
                    (itemNode as? Map<String, Any?>)?.let { itemSchema ->
                        val indexedPath = "$parentPath[$index]"
                        parseNode("[$index]", indexedPath, itemSchema, false)
                    }
                }
            }

            else -> null
        }
    }

    private fun inferNodeType(value: Map<String, Any?>): String? {
        val type = value[TYPE]
        if (type != null && type is String) return type

        return when {
            value.containsKey(PROPERTIES) -> OBJECT_NODE
            value.containsKey(ITEMS) -> ARRAY_NODE
            value.containsKey(CONST) -> checkValue(value[CONST])
            else -> null
        }
    }

    private fun checkValue(any: Any?): String? {
        return when (any) {
            is Int -> INTEGER_NODE
            is String -> STRING_NODE
            is Boolean -> BOOLEAN_NODE
            is Double -> NUMBER_NODE
            is List<*> -> ARRAY_NODE
            is Map<*, *> -> OBJECT_NODE
            else -> null
        }
    }
}
