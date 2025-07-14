package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.COMPOSITION_NODE
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.ONE_OF
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.core.Native.Array
import com.example.nanoboltron.jsonschema.core.Native.Object
import com.example.nanoboltron.jsonschema.core.Native.Primitive
import com.example.nanoboltron.jsonschema.parser.JsonParser

class JsonSchemaParserImpl(private val jsonParser: JsonParser): JsonParser {

    override fun parse(json: String): DescriptorNode? {
        val rootNode = jsonParser.parse(json) ?: return null
        return interpretAsSchemaNode(rootNode)
    }

    private fun interpretAsSchemaNode(jsonNode: JsonNode): DescriptorNode {
        return when (jsonNode) {
            is Object -> interpretSchemaObject(jsonNode)
            is Array -> interpretSchemaArray(jsonNode)
            is Primitive -> interpretSchemaPrimitive(jsonNode)
            else -> DescriptorNode.GroupNode(
                key = jsonNode.key,
                path = jsonNode.path,
                nodes = emptyList()
            )
        }
    }

    private fun interpretSchemaObject(obj: Object): DescriptorNode {
        // Extract common schema properties
        val title = extractStringProperty(obj, "title")
        val description = extractStringProperty(obj, "description")
        val type = extractStringProperty(obj, "type")
        val readOnly = extractBooleanProperty(obj, "readOnly")
        val writeOnly = extractBooleanProperty(obj, "writeOnly")

        // Handle composition keywords first
        when {
            obj.children?.containsKey("allOf") == true -> {
                return createCompositionNode(obj, ALL_OF, title, description)
            }
            obj.children?.containsKey("anyOf") == true -> {
                return createCompositionNode(obj, ANY_OF, title, description)
            }
            obj.children?.containsKey("oneOf") == true -> {
                return createCompositionNode(obj, ONE_OF, title, description)
            }
            obj.children?.containsKey("if") == true -> {
                return createConditionalNode(obj, title, description)
            }
        }

        // Handle type-specific schemas
        return when (type) {
            "string" -> createStringNode(obj, title, description, readOnly, writeOnly)
            "number", "integer" -> createNumberNode(obj, title, description, readOnly, writeOnly)
            "boolean" -> createBooleanNode(obj, title, description, readOnly)
            "object" -> createGroupNode(obj, title, description, readOnly, writeOnly)
            "array" -> createGroupNodeFromArray(obj, title, description, readOnly, writeOnly)
            else -> {
                // If no type specified, check if it has properties (object-like)
                if (obj.children?.containsKey("properties") == true) {
                    createGroupNode(obj, title, description, readOnly, writeOnly)
                } else {
                    // Default to group node for unspecified types
                    DescriptorNode.GroupNode(
                        key = obj.key,
                        path = obj.path,
                        title = title,
                        description = description,
                        readOnly = readOnly,
                        writeOnly = writeOnly,
                        nodes = emptyList()
                    )
                }
            }
        }
    }

    private fun createCompositionNode(
        obj: Object,
        compositionType: String,
        title: String?,
        description: String?
    ): DescriptorNode.CompositionNode {
        val compositionArray = obj.children?.get(compositionType) as? Array
        val schemas = compositionArray?.children?.map { interpretAsSchemaNode(it) } ?: emptyList()

        return DescriptorNode.CompositionNode(
            key = obj.key,
            path = obj.path,
            type = COMPOSITION_NODE,
            title = title,
            description = description,
            compositionType = compositionType,
            schemas = schemas
        )
    }

    private fun createConditionalNode(
        obj: Object,
        title: String?,
        description: String?
    ): DescriptorNode.ConditionalNode {
        val ifSchema = obj.children?.get("if")?.let { interpretAsSchemaNode(it) }
        val thenSchema = obj.children?.get("then")?.let { interpretAsSchemaNode(it) }
        val elseSchema = obj.children?.get("else")?.let { interpretAsSchemaNode(it) }

        return DescriptorNode.ConditionalNode(
            jsonSchemaType = GROUP, // or appropriate type
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            ifSchema = ifSchema,
            thenSchema = thenSchema,
            elseSchema = elseSchema
        )
    }

    private fun createStringNode(
        obj: Object,
        title: String?,
        description: String?,
        readOnly: Boolean?,
        writeOnly: Boolean?
    ): DescriptorNode.StringNode {
        val default = extractStringProperty(obj, "default")
        val format = extractStringProperty(obj, "format")
        val enumValues = extractStringArrayProperty(obj, "enum")
        val contentMediaType = extractStringProperty(obj, "contentMediaType")
        val contentEncoding = extractStringProperty(obj, "contentEncoding")

        return DescriptorNode.StringNode(
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            default = default,
            format = format,
            readOnly = readOnly,
            writeOnly = writeOnly,
            enum = enumValues,
            contentMediaType = contentMediaType,
            contentEncoding = contentEncoding
        )
    }

    private fun createNumberNode(
        obj: Object,
        title: String?,
        description: String?,
        readOnly: Boolean?,
        writeOnly: Boolean?
    ): DescriptorNode.NumberNode {
        val default = extractNumberProperty(obj, "default")
        val enumValues = extractNumberArrayProperty(obj, "enum")
        val multipleOf = extractNumberProperty(obj, "multipleOf")

        return DescriptorNode.NumberNode(
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            default = default,
            readOnly = readOnly,
            writeOnly = writeOnly,
            enum = enumValues,
            multipleOf = multipleOf
        )
    }

    private fun createBooleanNode(
        obj: Object,
        title: String?,
        description: String?,
        readOnly: Boolean?
    ): DescriptorNode.BooleanNode {
        val default = extractBooleanProperty(obj, "default")

        return DescriptorNode.BooleanNode(
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            default = default,
            readOnly = readOnly
        )
    }

    private fun createGroupNode(
        obj: Object,
        title: String?,
        description: String?,
        readOnly: Boolean?,
        writeOnly: Boolean?
    ): DescriptorNode.GroupNode {
        val properties = obj.children?.get("properties") as? Object
        val nodes = properties?.children?.map { (_, propertyNode) ->
            interpretAsSchemaNode(propertyNode)
        } ?: emptyList()

        return DescriptorNode.GroupNode(
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun createGroupNodeFromArray(
        obj: Object,
        title: String?,
        description: String?,
        readOnly: Boolean?,
        writeOnly: Boolean?
    ): DescriptorNode.GroupNode {
        val items = obj.children?.get("items")?.let { interpretAsSchemaNode(it) }
        val nodes = items?.let { listOf(it) } ?: emptyList()

        return DescriptorNode.GroupNode(
            key = obj.key,
            path = obj.path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            nodes = nodes
        )
    }

    private fun interpretSchemaArray(arr: Array): DescriptorNode {
        // Arrays in JSON Schema context are typically composition arrays
        val schemas = arr.children?.map { interpretAsSchemaNode(it) } ?: emptyList()
        return DescriptorNode.GroupNode(
            key = arr.key,
            path = arr.path,
            nodes = schemas
        )
    }

    private fun interpretSchemaPrimitive(prim: Primitive): DescriptorNode {
        // Handle primitive values in schema context
        return when (prim.value) {
            is String -> DescriptorNode.StringNode(
                key = prim.key,
                path = prim.path,
                default = prim.value
            )

            is Number -> DescriptorNode.NumberNode(
                key = prim.key,
                path = prim.path,
                default = prim.value
            )

            is Boolean -> DescriptorNode.BooleanNode(
                key = prim.key,
                path = prim.path,
                default = prim.value
            )

            else -> DescriptorNode.GroupNode(
                key = prim.key,
                path = prim.path,
                nodes = emptyList()
            )
        }
    }

    // Helper methods to extract properties safely
    private fun extractStringProperty(obj: Object, key: String): String? {
        return (obj.children?.get(key) as? Primitive)?.value as? String
    }

    private fun extractBooleanProperty(obj: Object, key: String): Boolean? {
        return (obj.children?.get(key) as? Primitive)?.value as? Boolean
    }

    private fun extractNumberProperty(obj: Object, key: String): Number? {
        return (obj.children?.get(key) as? Primitive)?.value as? Number
    }

    private fun extractStringArrayProperty(obj: Object, key: String): List<String>? {
        val arrayNode = obj.children?.get(key) as? Array
        return arrayNode?.children?.mapNotNull {
            (it as? Primitive)?.value as? String
        }
    }

    private fun extractNumberArrayProperty(obj: Object, key: String): List<Number>? {
        val arrayNode = obj.children?.get(key) as? Array
        return arrayNode?.children?.mapNotNull {
            (it as? Primitive)?.value as? Number
        }
    }
}