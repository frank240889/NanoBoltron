package com.example.nanoboltron.jsonschema.parser.parsers

import android.content.Context
import android.util.Log
import com.example.nanoboltron.jsonschema.parser.JsonNode
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import com.networknt.schema.ValidatorTypeCode
import com.networknt.schema.walk.JsonSchemaWalkListener
import com.networknt.schema.walk.WalkEvent
import com.networknt.schema.walk.WalkFlow
import java.io.BufferedReader
import java.io.InputStreamReader

class WalkParser(private val context: Context) : JsonParser {
    override fun parse(json: String): JsonNode? {
        val objectMapper = ObjectMapper()
        val jsonNode = objectMapper.readTree(json)

        val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
        val config = SchemaValidatorsConfig().apply {
            this.addPropertyWalkListener(
                object : JsonSchemaWalkListener {
                    override fun onWalkStart(walkEvent: WalkEvent?): WalkFlow? {
                        Log.d("onWalkStart", walkEvent?.schemaPath.toString())
                        return WalkFlow.CONTINUE
                    }

                    override fun onWalkEnd(
                        walkEvent: WalkEvent?,
                        validationMessages: Set<ValidationMessage?>?
                    ) {
                        Log.d("onWalkEnd", walkEvent.toString())
                    }

                }
            )
        }
        val schema = factory.getSchema(jsonNode, config)
        val inputNode =
            objectMapper.readTree(BufferedReader(InputStreamReader(context.assets.open("jsondata.json"))))
        val result = schema.walk(jsonNode, false)


        return null
    }
}