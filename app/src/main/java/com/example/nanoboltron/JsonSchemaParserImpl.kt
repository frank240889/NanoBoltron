package com.example.nanoboltron

import com.example.nanoboltron.jsonschema.ARRAY
import com.example.nanoboltron.jsonschema.BOOLEAN
import com.example.nanoboltron.jsonschema.INTEGER
import com.example.nanoboltron.jsonschema.NUMBER
import com.example.nanoboltron.jsonschema.OBJECT
import com.example.nanoboltron.jsonschema.STRING
import com.example.nanoboltron.jsonschema.parser.ArrayItems
import com.example.nanoboltron.jsonschema.parser.JsonSchemaNode
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.Key
import com.example.nanoboltron.jsonschema.parser.Operator
import com.example.nanoboltron.jsonschema.parser.UiNodeDescriptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class JsonSchemaParserImpl : JsonSchemaParser {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    val mapAdapter = moshi.adapter<Map<String, Any?>>(type)

    override fun parse(schema: String): JsonSchemaNode? {
        val model: Map<String, Any?> = mapAdapter.fromJson(schema) ?: mapOf()
        val mainNode = parseNode(value = model)
        return mainNode
    }

    private fun parseNode(
        key: Key? = null,
        value: Map<String, Any?>,
        path: String = ""
    ): JsonSchemaNode? {
        val type = value["type"] as? String

        // 1. Si tiene operadores lógicos
        when {
            value.containsKey("anyOf") || value.containsKey("allOf") || value.containsKey("oneOf") -> {
                val operatorType = listOf("anyOf", "allOf", "oneOf").first { value.containsKey(it) }
                val rawList = value[operatorType] as? List<Map<String, Any>> ?: return null

                val subnodes = rawList.mapIndexed { index, entry ->
                    parseNode(
                        key = null,
                        value = entry,
                        path = path // no cambia el path
                    )
                }.filterNotNull()

                return Operator.Logical(
                    type = operatorType,
                    nodes = subnodes
                )
            }

            value.containsKey("if") -> {
                val ifSchema = parseNode(null, value["if"] as Map<String, Any>, path) ?: return null
                val thenSchema = (value["then"] as? Map<String, Any>)?.let { parseNode(null, it, path) }
                val elseSchema = (value["else"] as? Map<String, Any>)?.let { parseNode(null, it, path) }

                return Operator.Decision(
                    type = "if",
                    ifSchema = ifSchema,
                    thenSchema = thenSchema,
                    elseSchema = elseSchema
                )
            }
        }

        // 2. Parsear según el tipo
        return when (type) {
            OBJECT -> {
                val props = (value["properties"] as? Map<String, Map<String, Any>>)
                    ?.map { (propKey, propValue) ->
                        parseNode(
                            key = propKey,
                            value = propValue,
                            path = if (path.isEmpty()) propKey else "$path.$propKey"
                        )
                    }?.filterNotNull()

                UiNodeDescriptor.GroupNode(
                    key = key,
                    path = path.ifEmpty { null },
                    type = OBJECT,
                    title = value["title"] as? String,
                    description = value["description"] as? String,
                    readOnly = value["readOnly"] as? Boolean,
                    writeOnly = value["writeOnly"] as? Boolean,
                    properties = props
                )
            }

            STRING -> UiNodeDescriptor.StringUiNodeDescriptor(
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

            BOOLEAN -> UiNodeDescriptor.BooleanUiNodeDescriptor(
                key = key,
                path = path,
                type = BOOLEAN,
                title = value["title"] as? String,
                description = value["description"] as? String,
                default = value["default"] as? Boolean,
                readOnly = value["readOnly"] as? Boolean
            )

            NUMBER -> UiNodeDescriptor.NumberUiNodeDescriptor(
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

            INTEGER -> UiNodeDescriptor.IntegerUiNodeDescriptor(
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

            ARRAY -> {
                val rawItems = value["items"]
                val parsedItems = when (rawItems) {
                    is Map<*, *> -> listOfNotNull(parseNode(null, rawItems as Map<String, Any>, path))
                    is List<*> -> rawItems.mapIndexedNotNull { index, it ->
                        (it as? Map<String, Any>)?.let {
                            parseNode(null, it, "$path[$index]")
                        }
                    }
                    else -> null
                }

                UiNodeDescriptor.RepeatingGroupNode(
                    key = key,
                    path = path,
                    type = ARRAY,
                    title = value["title"] as? String,
                    description = value["description"] as? String,
                    default = null,
                    readOnly = value["readOnly"] as? Boolean,
                    writeOnly = value["writeOnly"] as? Boolean,
                    items = parsedItems?.let { ArrayItems.ArrDescriptor(it) }
                )
            }

            else -> {
                // Puedes registrar un warning si quieres
                null
            }
        }
    }

}
