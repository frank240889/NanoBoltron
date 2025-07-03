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

            // Create schema factory and configuration
            val factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012)
            val config = SchemaValidatorsConfig().apply {
                // Listener specifically for array items
                this.addItemWalkListener(object : JsonSchemaWalkListener {
                    override fun onWalkStart(walkEvent: WalkEvent?): WalkFlow {
                        Log.d(TAG, "=== On Walk Start ===")
                        Log.d(TAG, "Schema Path: ${walkEvent?.schemaPath}")
                        Log.d(TAG, "Schema Node: ${walkEvent?.schemaNode?.get(walkEvent.keyWordName)}")
                        Log.d(TAG, "Schema field key: ${walkEvent?.keyWordName}")
                        return WalkFlow.CONTINUE
                    }

                    override fun onWalkEnd(
                        walkEvent: WalkEvent?,
                        validationMessages: Set<ValidationMessage?>?
                    ) {
                        walkEvent?.let { event ->
                            Log.d(TAG, "=== On Walk End ===")
                            Log.d(TAG, "\n")
                        }
                    }
                })
            }

            // Create schema from the loaded schema node
            val schema = factory.getSchema(schemaNode, config)

            // Parse the input JSON data
            val inputNode = objectMapper.readTree(json)

            // Walk through the schema with the input data
            Log.d(TAG, "Starting schema walk...")
            val walkResult = schema.walk(inputNode, false)

            Log.d(TAG, "Schema walk completed.")
            Log.d(TAG, "Walk result: ${walkResult.validationMessages}")

            // Also walk the schema itself to traverse all schema nodes
            //Log.d(TAG, "Walking schema structure2...")
            //val res = schema.walk(schemaNode, true)
            //Log.d(TAG, "Walk result: ${res.validationMessages}")
            //Log.d(TAG, "Schema walk completed2.")
        } catch (e: Exception) {
            Log.e(TAG, "Error during schema parsing and walking", e)
        }

        return null
    }
}