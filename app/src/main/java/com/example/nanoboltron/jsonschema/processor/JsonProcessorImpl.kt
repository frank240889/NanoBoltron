package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import android.util.Log
import com.example.nanoboltron.jsonschema.core.JsonLoader
import com.example.nanoboltron.jsonschema.core.DescriptorNode
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.NodeTraverser

class JsonProcessorImpl constructor(
    private val jsonSchemaParser: JsonParser,
    private val jsonDataParser: JsonParser
) : JsonProcessor {
    private val jsonSchemaName: String = "default"
    private val jsonDataName: String = "default"
    private val traverser = NodeTraverser()

    override fun loadSchema(name: String, jsonSchemaString: String, context: Context): JsonNode? {
        val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsonschema.json")
        val nodes = jsonSchemaParser.parse(jsonSchemaString)
        if (nodes != null && nodes is DescriptorNode) {
            Log.d("JsonProcessor", "✅ Schema loaded successfully")
            traverser.forEachNode(nodes) { node, path, key, depth ->
                Log.d("JsonProcessor", "🔍 Primitive at path: $path, key: $key, depth: $depth")
            }
        } else {
            Log.e("JsonProcessor", "❌ Failed to parse schema or invalid node type")
        }
        return nodes
    }

    override fun loadData(name: String, jsonDataString: String?, context: Context) {
        val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsondata.json")
        val nodes = jsonDataParser.parse(jsonSchemaString)
        Log.e("DATA NODES", nodes.toString())
    }

    override fun updateValue(key: String, value: Any, path: String?): JsonProcessorResult {
        return JsonProcessorResult.OnDataBound
    }

    override fun validate(): JsonProcessorResult {
        return JsonProcessorResult.OnDataBound
    }
}
