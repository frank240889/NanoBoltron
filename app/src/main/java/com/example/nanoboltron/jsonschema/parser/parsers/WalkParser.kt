package com.example.nanoboltron.jsonschema.parser.parsers

import android.content.Context
import android.util.Log
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SchemaValidatorsConfig.ALL_KEYWORD_WALK_LISTENER_KEY
import com.networknt.schema.SpecVersion
import com.networknt.schema.ValidationMessage
import com.networknt.schema.ValidatorTypeCode
import com.networknt.schema.walk.JsonSchemaWalkListener
import com.networknt.schema.walk.WalkEvent
import com.networknt.schema.walk.WalkFlow
import java.io.BufferedReader
import java.io.InputStreamReader

class WalkParser(private val context: Context) : JsonParser {
    private val TAG = "WalkParser"

    override fun parse(json: String): JsonNode? {
        val objectMapper = ObjectMapper()

        try {
            // Load the JSON schema from assets
            val schemaInputStream = context.assets.open("jsonschema.json")
            val schemaNode =
                objectMapper.readTree(BufferedReader(InputStreamReader(schemaInputStream)))
            val listener = UiNodeBuilderListener()
            // Create schema factory and configuration
            val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
            val config = SchemaValidatorsConfig().apply {
                addKeywordWalkListener(ValidatorTypeCode.ALL_OF.value, listener)
                addKeywordWalkListener(ValidatorTypeCode.ANY_OF.value, listener)
                addKeywordWalkListener(ValidatorTypeCode.ONE_OF.value, listener)
                addKeywordWalkListener(ValidatorTypeCode.IF_THEN_ELSE.value, listener)
                addKeywordWalkListener(ValidatorTypeCode.PROPERTIES.value, listener)
                addKeywordWalkListener(ValidatorTypeCode.ITEMS.value, listener)
                addKeywordWalkListener(ALL_KEYWORD_WALK_LISTENER_KEY, listener)
                addItemWalkListener(listener)
                addPropertyWalkListener(listener)
            }

            // Create schema from the loaded schema node
            val schema = factory.getSchema(schemaNode, config)

            // Parse the input JSON data
            val inputNode = objectMapper.readTree(json)

            // Walk through the schema with the input data
            Log.d(TAG, "Starting schema walk...")
            val walkResult = schema.walk(inputNode, true)

            Log.d(TAG, "Schema walk completed.")
            Log.d(TAG, "Walk result: ${walkResult.validationMessages.joinToString()}")

            // Try walking the schema structure itself
            Log.d(TAG, "Walking schema structure...")
            val schemaWalkResult = schema.walk(schemaNode, true)
            Log.d(TAG, "Schema structure walk completed.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during schema parsing and walking", e)
        }

        return null
    }
}

class UiNodeBuilderListener : JsonSchemaWalkListener {
    private val tag = "Walk"
    override fun onWalkStart(walkEvent: WalkEvent): WalkFlow {

        Log.d(tag, "=== On Walk Start ===")
        Log.d(tag, "Schema Path: ${walkEvent.schemaPath}")
        Log.d(tag, "Schema Node: ${walkEvent.schemaNode?.get(walkEvent.keyWordName)}")
        Log.d(tag, "Node: ${walkEvent.node.get(walkEvent.keyWordName)}")
        Log.d(tag, "Schema field key: ${walkEvent.keyWordName}")

        // Special debug for items keyword
        if (walkEvent.keyWordName == "items") {
            Log.d(tag, "*** ITEMS KEYWORD FOUND ***")
            Log.d(tag, "Items schema: ${walkEvent.schemaNode}")
        }

        Log.d(tag, "\n")
        return WalkFlow.CONTINUE
    }

    override fun onWalkEnd(we: WalkEvent, messages: Set<ValidationMessage>) {

    }
}