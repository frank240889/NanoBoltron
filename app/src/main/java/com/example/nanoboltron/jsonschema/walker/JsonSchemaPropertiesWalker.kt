package com.example.nanoboltron.jsonschema.walker

import com.example.nanoboltron.jsonschema.ALL_OF
import com.example.nanoboltron.jsonschema.ANY_OF
import com.example.nanoboltron.jsonschema.CONST
import com.example.nanoboltron.jsonschema.CONTENT_ENCODING
import com.example.nanoboltron.jsonschema.CONTENT_MEDIA_TYPE
import com.example.nanoboltron.jsonschema.DEFAULT
import com.example.nanoboltron.jsonschema.DESCRIPTION
import com.example.nanoboltron.jsonschema.ENUM
import com.example.nanoboltron.jsonschema.FORMAT
import com.example.nanoboltron.jsonschema.ITEMS
import com.example.nanoboltron.jsonschema.ONE_OF
import com.example.nanoboltron.jsonschema.PROPERTIES
import com.example.nanoboltron.jsonschema.READ_ONLY
import com.example.nanoboltron.jsonschema.TITLE
import com.example.nanoboltron.jsonschema.TYPE
import com.example.nanoboltron.jsonschema.WRITE_ONLY
import javax.inject.Inject

class JsonSchemaPropertiesWalker @Inject constructor(
    private val walker: Walker
) : Walker {
    private val discriminators = arrayOf(
        TYPE,
        TITLE,
        DESCRIPTION,
        DEFAULT,
        READ_ONLY,
        WRITE_ONLY,
        ENUM,
        CONTENT_MEDIA_TYPE,
        CONTENT_ENCODING,
        ITEMS,
        CONST,
        FORMAT,
        PROPERTIES,
        ANY_OF,
        ONE_OF,
        ALL_OF
    )

    override fun walk(
        json: String,
        onEvent: (WalkerEvent) -> Unit
    ) {
        walker.walk(json) {
            if (it is WalkerEvent.OnTraversingNode) {
                if (it.key in discriminators) {
                    onEvent(it)
                }
            }
        }
    }

    override fun nodes(json: String): List<Node> {
        return walker.nodes(json)
    }
}