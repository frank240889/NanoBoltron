package com.example.nanoboltron

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

class JsonSchemaLoader(private val context: Context) {
    fun loadJson(schemaName: String): String {
        val inputStream = context.assets.open(schemaName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val contents = reader.readText()
        reader.close()
        return contents
    }
}