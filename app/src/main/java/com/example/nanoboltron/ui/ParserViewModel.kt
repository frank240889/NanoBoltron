package com.example.nanoboltron.ui

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanoboltron.JsonLoader
import com.example.nanoboltron.jsonschema.JsonSchemaWalker
import com.example.nanoboltron.jsonschema.parser.JsonDataParser
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.parser.parsers.WalkParser
import com.example.nanoboltron.jsonschema.parser.printUiTree
import com.example.nanoboltron.jsonschema.processor.JsonProcessorImpl
import com.example.nanoboltron.jsonschema.validation.FieldDataHandlerImpl
import com.example.nanoboltron.jsonschema.walker.Walker
import kotlinx.coroutines.launch

class ParserViewModel : ViewModel() {
    private val jsonProcessor = JsonProcessorImpl(JsonSchemaParser(), JsonDataParser())
    private val fieldDataHandler: com.example.nanoboltron.jsonschema.FieldDataHandler =
        FieldDataHandlerImpl()
    private val walker: Walker = JsonSchemaWalker()

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
        val jsonparser: JsonParser = WalkParser(context)
        //jsonparser.parse(jsonSchemaString)
        walker.walk(jsonSchemaString) {
            Log.d("WalkerEvent", it.toString())
        }
        /*val jsonSchemaObject: JsonValue = JsonParser(jsonSchemaString).parse()
        val schema: Schema = SchemaLoader(jsonSchemaObject).load()
        val validator: Validator = Validator.create(
            schema,
            ValidatorConfig(FormatValidationPolicy.NEVER)
        )
        val dataString = jsonSchemaLoader.loadJson("jsondata.json")
        val data = com.github.erosb.jsonsKema.JsonParser(dataString).parse()
        val res = validator.validate(data)
        val mainNode = jsonParser.parse(jsonSchemaString)
        if (mainNode != null) {
            printUiTree(mainNode)
        }*/
    }

}