package com.example.nanoboltron.jsonschema

import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import com.example.nanoboltron.jsonschema.walker.Walker
import com.example.nanoboltron.jsonschema.walker.WalkerEvent
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaWalker: Walker {
    private val moshi = Moshi.Builder().build()
    private val mapAdapter: JsonAdapter<Map<String, Any?>> = moshi.adapter(
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    )

    override fun walk(
        json: String,
        onEvent: (WalkerEvent) -> Unit
    ) {
        onEvent(WalkerEvent.OnStartWalking)
        
        try {
            val jsonMap = mapAdapter.fromJson(json)
            if (jsonMap != null) {
                val rootNode = parseSchemaNode(jsonMap, null, null)
                if (rootNode != null) {
                    walkNode(rootNode, onEvent)
                }
            }
        } catch (e: Exception) {
            // Handle parsing errors gracefully
        }
        
        onEvent(WalkerEvent.OnEndWalking)
    }
    
    private fun walkNode(node: JsonNode, onEvent: (WalkerEvent) -> Unit) {
        onEvent(WalkerEvent.OnEnterNode(node))
        
        // Traverse child nodes based on node type
        when (node) {
            is DescriptorNode.GroupNode -> {
                node.nodes?.forEach { childNode ->
                    walkNode(childNode, onEvent)
                }
            }
            is DescriptorNode.CompositionNode -> {
                node.schemas.forEach { childSchema ->
                    walkNode(childSchema, onEvent)
                }
            }
            is DescriptorNode.ConditionalNode -> {
                node.ifSchema?.let { walkNode(it, onEvent) }
                node.thenSchema?.let { walkNode(it, onEvent) }
                node.elseSchema?.let { walkNode(it, onEvent) }
            }
            // Leaf nodes (StringNode, NumberNode, BooleanNode) have no children
        }
        
        onEvent(WalkerEvent.OnExitNode(node))
    }
    
    private fun parseSchemaNode(
        jsonMap: Map<String, Any?>,
        key: String? = null,
        path: String? = null
    ): DescriptorNode? {
        val type = jsonMap["type"] as? String
        val title = jsonMap["title"] as? String
        val description = jsonMap["description"] as? String
        
        // Handle composition schemas (allOf, anyOf, oneOf)
        when {
            jsonMap.containsKey("allOf") -> {
                val schemas = parseCompositionSchemas(jsonMap["allOf"], key, path)
                return DescriptorNode.CompositionNode(
                    key = key,
                    path = path,
                    type = "allOf",
                    title = title,
                    description = description,
                    compositionType = "allOf",
                    schemas = schemas
                )
            }
            jsonMap.containsKey("anyOf") -> {
                val schemas = parseCompositionSchemas(jsonMap["anyOf"], key, path)
                return DescriptorNode.CompositionNode(
                    key = key,
                    path = path,
                    type = "anyOf",
                    title = title,
                    description = description,
                    compositionType = "anyOf",
                    schemas = schemas
                )
            }
            jsonMap.containsKey("oneOf") -> {
                val schemas = parseCompositionSchemas(jsonMap["oneOf"], key, path)
                return DescriptorNode.CompositionNode(
                    key = key,
                    path = path,
                    type = "oneOf",
                    title = title,
                    description = description,
                    compositionType = "oneOf",
                    schemas = schemas
                )
            }
        }
        
        // Handle conditional schemas (if-then-else)
        if (jsonMap.containsKey("if")) {
            val ifSchema = parseSchemaNode(jsonMap["if"] as? Map<String, Any?> ?: emptyMap(), null, path)
            val thenSchema = jsonMap["then"]?.let { 
                parseSchemaNode(it as? Map<String, Any?> ?: emptyMap(), null, path)
            }
            val elseSchema = jsonMap["else"]?.let {
                parseSchemaNode(it as? Map<String, Any?> ?: emptyMap(), null, path)
            }
            
            return DescriptorNode.ConditionalNode(
                key = key,
                path = path,
                title = title,
                description = description,
                ifSchema = ifSchema,
                thenSchema = thenSchema,
                elseSchema = elseSchema
            )
        }
        
        return when (type) {
            "object" -> parseObjectNode(jsonMap, key, path)
            "string" -> parseStringNode(jsonMap, key, path)
            "number", "integer" -> parseNumberNode(jsonMap, key, path)
            "boolean" -> parseBooleanNode(jsonMap, key, path)
            "array" -> parseArrayNode(jsonMap, key, path)
            null -> {
                // If no type is specified, try to infer from properties
                if (jsonMap.containsKey("properties") || jsonMap.containsKey("additionalProperties")) {
                    parseObjectNode(jsonMap, key, path)
                } else {
                    // Default to group node for schemas without explicit types
                    DescriptorNode.GroupNode(
                        key = key,
                        path = path,
                        title = title,
                        description = description
                    )
                }
            }
            else -> null
        }
    }
    
    private fun parseObjectNode(
        jsonMap: Map<String, Any?>,
        key: String?,
        path: String?
    ): DescriptorNode.GroupNode {
        val properties = jsonMap["properties"] as? Map<String, Any?> ?: emptyMap()
        val title = jsonMap["title"] as? String
        val description = jsonMap["description"] as? String
        val readOnly = jsonMap["readOnly"] as? Boolean
        val writeOnly = jsonMap["writeOnly"] as? Boolean
        
        val childNodes = properties.mapNotNull { (propKey, propValue) ->
            val propPath = if (path != null) "$path.$propKey" else propKey
            parseSchemaNode(propValue as? Map<String, Any?> ?: emptyMap(), propKey, propPath)
        }
        
        return DescriptorNode.GroupNode(
            key = key,
            path = path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = childNodes
        )
    }
    
    private fun parseStringNode(
        jsonMap: Map<String, Any?>,
        key: String?,
        path: String?
    ): DescriptorNode.StringNode {
        return DescriptorNode.StringNode(
            key = key,
            path = path,
            title = jsonMap["title"] as? String,
            description = jsonMap["description"] as? String,
            default = jsonMap["default"] as? String,
            format = jsonMap["format"] as? String,
            readOnly = jsonMap["readOnly"] as? Boolean,
            writeOnly = jsonMap["writeOnly"] as? Boolean,
            enum = (jsonMap["enum"] as? List<*>)?.mapNotNull { it as? String },
            contentMediaType = jsonMap["contentMediaType"] as? String,
            contentEncoding = jsonMap["contentEncoding"] as? String
        )
    }
    
    private fun parseNumberNode(
        jsonMap: Map<String, Any?>,
        key: String?,
        path: String?
    ): DescriptorNode.NumberNode {
        return DescriptorNode.NumberNode(
            key = key,
            path = path,
            title = jsonMap["title"] as? String,
            description = jsonMap["description"] as? String,
            default = jsonMap["default"] as? Number,
            readOnly = jsonMap["readOnly"] as? Boolean,
            writeOnly = jsonMap["writeOnly"] as? Boolean,
            enum = (jsonMap["enum"] as? List<*>)?.mapNotNull { it as? Number },
            multipleOf = jsonMap["multipleOf"] as? Number
        )
    }
    
    private fun parseBooleanNode(
        jsonMap: Map<String, Any?>,
        key: String?,
        path: String?
    ): DescriptorNode.BooleanNode {
        return DescriptorNode.BooleanNode(
            key = key,
            path = path,
            title = jsonMap["title"] as? String,
            description = jsonMap["description"] as? String,
            default = jsonMap["default"] as? Boolean,
            readOnly = jsonMap["readOnly"] as? Boolean
        )
    }
    
    private fun parseArrayNode(
        jsonMap: Map<String, Any?>,
        key: String?,
        path: String?
    ): DescriptorNode.GroupNode {
        val items = jsonMap["items"] as? Map<String, Any?>
        val prefixItems = jsonMap["prefixItems"] as? List<*>
        val title = jsonMap["title"] as? String
        val description = jsonMap["description"] as? String
        
        val childNodes = mutableListOf<DescriptorNode>()
        
        // Handle prefixItems (tuple validation)
        prefixItems?.forEachIndexed { index, item ->
            val itemPath = if (path != null) "$path[$index]" else "[$index]"
            parseSchemaNode(item as? Map<String, Any?> ?: emptyMap(), index.toString(), itemPath)?.let {
                childNodes.add(it)
            }
        }
        
        // Handle items (array validation)
        if (items != null) {
            val itemPath = if (path != null) "$path[*]" else "[*]"
            parseSchemaNode(items, "*", itemPath)?.let {
                childNodes.add(it)
            }
        }
        
        return DescriptorNode.GroupNode(
            key = key,
            path = path,
            type = "array",
            title = title,
            description = description,
            nodes = childNodes
        )
    }
    
    private fun parseCompositionSchemas(
        compositionValue: Any?,
        key: String?,
        path: String?
    ): List<DescriptorNode> {
        val schemaList = compositionValue as? List<*> ?: return emptyList()
        return schemaList.mapNotNull { schema ->
            parseSchemaNode(schema as? Map<String, Any?> ?: emptyMap(), key, path)
        }
    }
}