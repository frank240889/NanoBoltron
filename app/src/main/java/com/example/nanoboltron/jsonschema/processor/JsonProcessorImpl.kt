package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import android.util.Log
import com.example.nanoboltron.JsonLoader
import com.example.nanoboltron.jsonschema.analyzer.SchemaAnalyzer
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import com.example.nanoboltron.jsonschema.core.JsonNode
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.NodeTraverser

class JsonProcessorImpl constructor(
    private val jsonSchemaParser: JsonParser,
    private val jsonDataParser: JsonParser
) : JsonProcessor {
    private val jsonSchemaName: String = "default"
    private val jsonDataName: String = "default"
    private lateinit var context: Context
    private val schemaAnalyzer = SchemaAnalyzer()
    private val traverser = NodeTraverser()

    override fun loadSchema(name: String, jsonSchemaString: String): JsonNode? {
        val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsonschema.json")
        val nodes = jsonSchemaParser.parse(jsonSchemaString)
        if (nodes != null && nodes is DescriptorNode) {
            Log.d("JsonProcessor", "✅ Schema loaded successfully")
            traverser.forEachNode(nodes) { node, path, key, depth ->
                Log.d("JsonProcessor", "🔍 Primitive at path: $path, key: $key, depth: $depth")
            }
            /*traverser
                .findAllAndTransform(
                    rootNode = nodes,
                    predicate = { node, path, key, depth ->
                        //path == "properties.users.items.allOf.[1].oneOf.[1].properties.preferences.properties" &&
                                key == "role"
                    },
                    transform = { node, path, key, depth ->
                        node
                    }
                )
                .forEach {
                    Log.d("NODE", it.toString())
                }*/

            // Perform complete schema analysis with one simple call
            //schemaAnalyzer.performCompleteAnalysis(nodes, name, "JsonProcessor")
        } else {
            Log.e("JsonProcessor", "❌ Failed to parse schema or invalid node type")
        }
        return nodes
    }

    override fun loadData(name: String, jsonDataString: String?) {
        /*val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsondata.json")
        val nodes = jsonDataParser.parse(jsonSchemaString)
        if (nodes != null && nodes is FormDataNode) {
            printUiTree(nodes)
        }*/
    }

    override fun updateValue(key: String, value: Any, path: String?): JsonProcessorResult {
        return JsonProcessorResult.OnDataBound
    }

    override fun validate(): JsonProcessorResult {
        return JsonProcessorResult.OnDataBound
    }

    override fun addContext(context: Context) {
        this.context = context
    }
}
