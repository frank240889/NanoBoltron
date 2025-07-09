package com.example.nanoboltron.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanoboltron.JsonLoader
import com.example.nanoboltron.jsonschema.core.Native
import com.example.nanoboltron.jsonschema.parser.JsonDataParser
import com.example.nanoboltron.jsonschema.parser.Parser
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.findNodeByPath
import com.example.nanoboltron.jsonschema.parser.parsers.JsonParser
import com.example.nanoboltron.jsonschema.processor.JsonProcessorImpl
import com.example.nanoboltron.jsonschema.validation.FieldDataHandlerImpl
import com.example.nanoboltron.jsonschema.walker.JsonWalker
import com.example.nanoboltron.jsonschema.walker.Walker
import kotlinx.coroutines.launch
import kotlin.random.Random

class ParserViewModel : ViewModel() {
    private val jsonProcessor = JsonProcessorImpl(JsonSchemaParser(), JsonDataParser())
    private val fieldDataHandler: com.example.nanoboltron.jsonschema.FieldDataHandler =
        FieldDataHandlerImpl()

    fun setValue(path: String? = null, value: Any) {
        viewModelScope.launch {
            fieldDataHandler.setValue(path, value)
        }
    }

    fun addContext(context: Context) {
        jsonProcessor.addContext(context)
    }

    fun parseJson() {
        jsonProcessor.loadSchema("default", "jsonschema.json")
    }

    fun loadJsonSchema(context: Context) {
        val jsonSchemaLoader = JsonLoader(context)
        val jsonSchemaString = jsonSchemaLoader.loadJson("jsonschema.json")
        val jsonDataString = jsonSchemaLoader.loadJson("jsondata.json")
        val jsonParser: Parser = JsonParser()
        val jsonWalker: Walker = JsonWalker()
        val nodes = jsonWalker
            .flatListNodes(jsonSchemaString)

        val randomNode = nodes[Random.nextInt(0, nodes.lastIndex)]
        val path = randomNode.path
        val key = randomNode.key
        Log.e("PATH", "PATH: $path")
        Log.e("KEY", "KEY: $key")
        val jsNode = jsonParser.parse(jsonSchemaString)
        val testNode = if (jsNode is Native) {
            jsNode.findNodeByPath(path, key)
        } else {
            null
        }
        Log.e("TEST", "testNode: $testNode")
    }

}