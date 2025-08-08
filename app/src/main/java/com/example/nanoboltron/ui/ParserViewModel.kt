package com.example.nanoboltron.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanoboltron.jsonschema.core.JsonLoader
import com.example.nanoboltron.jsonschema.core.Native
import com.example.nanoboltron.jsonschema.core.findNodeByPath
import com.example.nanoboltron.jsonschema.datamanager.FieldDataHandler
import com.example.nanoboltron.jsonschema.processor.JsonProcessorImpl
import com.example.nanoboltron.jsonschema.datamanager.FieldDataHandlerImpl
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.parsers.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.parsers.JsonFormDataParser
import com.example.nanoboltron.jsonschema.walker.JsonWalker
import com.example.nanoboltron.jsonschema.walker.Walker
import kotlinx.coroutines.launch
import kotlin.random.Random

class ParserViewModel : ViewModel() {
    private val jsonProcessor = JsonProcessorImpl(JsonSchemaParser(), JsonFormDataParser())
    private val fieldDataHandler: FieldDataHandler =
        FieldDataHandlerImpl()

    fun setValue(path: String? = null, value: Any) {
        viewModelScope.launch {
            fieldDataHandler.setValue(path, value)
        }
    }

    fun loadData(context: Context) {
        jsonProcessor.loadData("", "jsondata.json", context)
    }

    fun loadJsonSchema(context: Context) {
        jsonProcessor.loadSchema("", "jsonschema.json", context)
    }

    fun nodeLookUp(context: Context) {
        val jsonSchemaLoader = JsonLoader(context)
        val jsonSchemaString = jsonSchemaLoader.loadJson("jsonschema.json")
        val jsonWalker: Walker = JsonWalker()
        val nodes = jsonWalker.flatListNodes(jsonSchemaString)

        val randomNode = nodes[Random.nextInt(0, nodes.lastIndex)]
        val path = randomNode.path
        val key = randomNode.key
        Log.e("PATH", "PATH: $path")
        Log.e("KEY", "KEY: $key")

        val jsonFormDataParser: JsonParser = JsonFormDataParser()
        val jsNode = jsonFormDataParser.parse(jsonSchemaString)
        val testNode = if (jsNode is Native) {
            jsNode.findNodeByPath(path, key)
        } else {
            null
        }
        Log.e("TEST", "testNode: $testNode")

        val jsonSchemaParser: JsonParser = JsonSchemaParser()
        val anotherNode = jsonSchemaParser.parse(jsonSchemaString)
        val anotherRandomNode = nodes[Random.nextInt(0, nodes.lastIndex)]
        val anotherPath = anotherRandomNode.path
        val anotherKey = randomNode.key
        Log.e("ANOTHER NODE", "ANOTHER NODE: $anotherNode")
        Log.e("ANOTHER PATH", "ANOTHER PATH: $anotherPath")
        Log.e("ANOTHER KEY", "ANOTHER KEY: $anotherKey")
        val anotherTestNode = anotherNode?.findNodeByPath(path, key)
        Log.e("ANOTHER TEST", "anotherTestNode: $anotherTestNode")
    }

}