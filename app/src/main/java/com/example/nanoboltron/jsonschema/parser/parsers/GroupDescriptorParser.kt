package com.example.nanoboltron.jsonschema.parser.parsers

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ARRAY_NODE
import com.example.nanoboltron.jsonschema.BOOLEAN_NODE
import com.example.nanoboltron.jsonschema.DESCRIPTION
import com.example.nanoboltron.jsonschema.GROUP
import com.example.nanoboltron.jsonschema.ITEMS
import com.example.nanoboltron.jsonschema.NUMBER_NODE
import com.example.nanoboltron.jsonschema.OBJECT_NODE
import com.example.nanoboltron.jsonschema.PROPERTIES
import com.example.nanoboltron.jsonschema.READ_ONLY
import com.example.nanoboltron.jsonschema.REPEATABLE_GROUP
import com.example.nanoboltron.jsonschema.STRING_NODE
import com.example.nanoboltron.jsonschema.TITLE
import com.example.nanoboltron.jsonschema.TYPE
import com.example.nanoboltron.jsonschema.WRITE_ONLY
import com.example.nanoboltron.jsonschema.parser.Key
import com.example.nanoboltron.jsonschema.selectionOperators
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
        path: String
    ): DescriptorNode {
        val title = value[TITLE] as? String
        val description = value[DESCRIPTION] as? String
        val readOnly = value[READ_ONLY] as? Boolean
        val writeOnly = value[WRITE_ONLY] as? Boolean
        var nodes: List<DescriptorNode>? = null
        var newType = type

        when (value[TYPE]) {
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
                        parseNode(OBJECT_NODE, fieldKey, fieldSchema, composePath)
                    }
            }

            ARRAY_NODE -> {
                newType = REPEATABLE_GROUP
                val newPath = "$path.$ITEMS"
                (value[ITEMS])
                    ?.let { rawItems ->
                        when (rawItems) {
                            is Map<*, *> -> listOfNotNull(
                                parseNode(
                                    GROUP,
                                    key,
                                    rawItems as Map<String, Any>,
                                    newPath
                                )
                            )

                            is List<*> -> rawItems.mapIndexedNotNull { index, it ->
                                (it as? Map<String, Any>)?.let {
                                    parseNode(REPEATABLE_GROUP, null, it, "$newPath.$index")
                                }
                            }

                            else -> null
                        }
                    }
            }

            else -> {
                when {

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
        return when (value[TYPE]) {
            STRING_NODE -> stringDescriptorParser.parse(STRING_NODE, key, value, path)
            NUMBER_NODE -> numberDescriptorParser.parse(NUMBER_NODE, key, value, path)
            BOOLEAN_NODE -> booleanDescriptorParser.parse(BOOLEAN_NODE, key, value, path)
            else ->  parse(type, key, value, path)
        }
    }
}