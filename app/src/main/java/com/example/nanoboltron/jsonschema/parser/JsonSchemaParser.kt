package com.example.nanoboltron.jsonschema.parser

import com.example.nanoboltron.jsonschema.BOOLEAN
import com.example.nanoboltron.jsonschema.INTEGER
import com.example.nanoboltron.jsonschema.NUMBER
import com.example.nanoboltron.jsonschema.REPEATABLE
import com.example.nanoboltron.jsonschema.STRING
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaParser : JsonParser {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)

    override fun parse(schema: String): DescriptorNode? {
        val model: Map<String, Any?> = mapAdapter.fromJson(schema) ?: mapOf()
        return parseGroup(value = model)
    }

    private fun parseGroup(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.GroupNode? {
        val title = value["title"] as? String
        val description = value["description"] as? String
        val readOnly = value["readOnly"] as? Boolean
        val writeOnly = value["writeOnly"] as? Boolean

        val props = (value["properties"] as? Map<*, *>)?.mapNotNull { (rawKey, rawSchema) ->
            val fieldKey = rawKey as? String ?: return@mapNotNull null
            val fieldSchema = rawSchema as? Map<String, Any?> ?: return@mapNotNull null
            val newPath = if (path.isBlank()) fieldKey else "$path.$fieldKey"
            parseNode(fieldKey, fieldSchema, newPath)
        }

        return DescriptorNode.GroupNode(
            key = key,
            path = if (path.isBlank()) null else path,
            title = title,
            description = description,
            readOnly = readOnly,
            writeOnly = writeOnly,
            properties = props
        )
    }

    private fun parseNode(
        key: Key?,
        value: Map<String, Any?>,
        path: String
    ): DescriptorNode? {
        return when (value["type"]) {
            "string" -> parseString(key, value, path)
            "number" -> parseNumber(key, value, path)
            "integer" -> parseInteger(key, value, path)
            "boolean" -> parseBoolean(key, value, path)
            "object" -> parseGroup(key, value, path)
            "array" -> parseArray(key, value, path)
            else -> null
        }
    }

    private fun parseString(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.StringNode {
        return DescriptorNode.StringNode(
            key = key,
            path = path,
            type = STRING,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = value["default"] as? String,
            placeholder = value["placeholder"] as? String,
            format = value["format"] as? String,
            readOnly = value["readOnly"] as? Boolean,
            writeOnly = value["writeOnly"] as? Boolean,
            enum = value["enum"] as? List<String>
        )
    }

    private fun parseNumber(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.NumberNode {
        return DescriptorNode.NumberNode(
            key = key,
            path = path,
            type = NUMBER,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = (value["default"] as? Number)?.toDouble(),
            placeholder = value["placeholder"] as? String,
            readOnly = value["readOnly"] as? Boolean,
            writeOnly = value["writeOnly"] as? Boolean,
            multipleOf = (value["multipleOf"] as? Number)?.toDouble(),
            enum = value["enum"] as? List<Double>
        )
    }

    private fun parseInteger(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.IntegerNode {
        return DescriptorNode.IntegerNode(
            key = key,
            path = path,
            type = INTEGER,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = (value["default"] as? Number)?.toInt(),
            placeholder = value["placeholder"] as? String,
            readOnly = value["readOnly"] as? Boolean,
            writeOnly = value["writeOnly"] as? Boolean,
            multipleOf = (value["multipleOf"] as? Number)?.toInt(),
            enum = value["enum"] as? List<Int>
        )
    }

    private fun parseBoolean(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.BooleanNode {
        return DescriptorNode.BooleanNode(
            key = key,
            path = path,
            type = BOOLEAN,
            title = value["title"] as? String,
            description = value["description"] as? String,
            default = value["default"] as? Boolean,
            readOnly = value["readOnly"] as? Boolean
        )
    }

    private fun parseArray(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): DescriptorNode.RepeatingGroupNode {
        val rawItems = value["items"]
        val parsedItems = when (rawItems) {
            is Map<*, *> -> listOfNotNull(parseNode(key, rawItems as Map<String, Any>, path))
            is List<*> -> rawItems.mapIndexedNotNull { index, it ->
                (it as? Map<String, Any>)?.let {
                    parseNode(null, it, "$path[$index]")
                }
            }

            else -> null
        }
        return DescriptorNode.RepeatingGroupNode(
            key = key,
            path = path,
            type = REPEATABLE,
            title = value["title"] as? String,
            description = value["description"] as? String,
            items = parsedItems
        )
    }
}