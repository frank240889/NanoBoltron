package com.example.nanoboltron.jsonschema.processor

import android.content.Context
import android.util.Log
import com.example.nanoboltron.JsonLoader
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode
import com.example.nanoboltron.jsonschema.parser.FormDataNode
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.UiDataNode
import com.example.nanoboltron.jsonschema.parser.printUiTree

class JsonProcessorImpl constructor(
    private val jsonSchemaParser: JsonParser,
    private val jsonDataParser: JsonParser
) : JsonProcessor {
    private val jsonSchemaName: String = "default"
    private val jsonDataName: String = "default"
    private lateinit var context: Context

    override fun loadSchema(name: String, jsonSchemaString: String) {
        val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsonschema.json")
        val nodes = jsonSchemaParser.parse(jsonSchemaString)
        if (nodes != null && nodes is DescriptorNode.GroupNode) {
            Log.e("NODES", nodes.toString())
        }
    }

    override fun loadData(name: String, jsonDataString: String?) {
        val jsonLoader = JsonLoader(context)
        val jsonSchemaString = jsonLoader.loadJson("jsondata.json")
        val nodes = jsonDataParser.parse(jsonSchemaString)
        /*if (nodes != null && nodes is FormDataNode) {
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