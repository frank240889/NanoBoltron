package com.example.nanoboltron.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nanoboltron.JsonSchemaLoader
import com.example.nanoboltron.jsonschema.parser.JsonSchemaParser
import com.example.nanoboltron.jsonschema.FieldDataHandler
import com.example.nanoboltron.jsonschema.parser.JsonParser
import com.example.nanoboltron.jsonschema.parser.printUiTree
import com.example.nanoboltron.jsonschema.validation.FieldDataHandlerImpl
import com.github.erosb.jsonsKema.FormatValidationPolicy
import com.github.erosb.jsonsKema.JsonParser
import com.github.erosb.jsonsKema.JsonValue
import com.github.erosb.jsonsKema.Schema
import com.github.erosb.jsonsKema.SchemaLoader
import com.github.erosb.jsonsKema.Validator
import com.github.erosb.jsonsKema.ValidatorConfig
import kotlinx.coroutines.launch

class ParserViewModel : ViewModel() {

    private val jsonParser: JsonParser = JsonSchemaParser()
    private val fieldDataHandler: FieldDataHandler = FieldDataHandlerImpl()

    fun setValue(path: String? = null, value: Any) {
        viewModelScope.launch {
            fieldDataHandler.setValue(path, value)
        }
    }

    fun parseJson(context: Context) {
        val jsonSchemaLoader = JsonSchemaLoader(context)
        val jsonSchemaString = jsonSchemaLoader.loadJson("jsonschema.json")
        val nodes = jsonParser.parse(jsonSchemaString)
        if (nodes != null) {
            printUiTree(nodes)
        }
    }

    fun loadJsonSchema(context: Context) {
        val jsonSchemaLoader = JsonSchemaLoader(context)
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
        }
    }

}