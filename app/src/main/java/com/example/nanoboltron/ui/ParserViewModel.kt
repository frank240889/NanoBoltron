package com.example.nanoboltron.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanoboltron.jsonschema.parser.JsonDataParser
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.processor.JsonProcessorImpl
import com.example.nanoboltron.jsonschema.validation.FieldDataHandlerImpl
import kotlinx.coroutines.launch

class ParserViewModel : ViewModel() {
    private val jsonProcessor = JsonProcessorImpl(JsonSchemaParser(), JsonDataParser())
    private val fieldDataHandler: com.example.nanoboltron.jsonschema.FieldDataHandler = FieldDataHandlerImpl()

    fun setValue(path: String? = null, value: Any) {
        viewModelScope.launch {
            fieldDataHandler.setValue(path, value)
        }
    }

    fun addContext(context: Context) {
        jsonProcessor.addContext(context)
    }

    fun parseJson() {
        val mainNode = jsonProcessor.loadSchema("default", "jsonschema.json")
        jsonProcessor.loadData("default", "jsondata.json")
    }

    fun loadJsonSchema(context: Context) {
        /*val jsonSchemaLoader = JsonLoader(context)
        val jsonSchemaString = jsonSchemaLoader.loadJson("jsonschema.json")
        val jsonSchemaObject: JsonValue = JsonParser(jsonSchemaString).parse()
        val schema: Schema = SchemaLoader(jsonSchemaObject).load()
        val validator: Validator = Validator.create(
            schema,
            ValidatorConfig(FormatValidationPolicy.NEVER)
        )
        val dataString = jsonSchemaLoader.loadJson("jsondata.json")
        val data = JsonParser(dataString).parse()
        val res = validator.validate(data)
        val mainNode = jsonParser.parse(jsonSchemaString)
        if (mainNode != null) {
            printUiTree(mainNode)
        }*/
    }

}