package com.example.nanoboltron.jsonschema.datamanager

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class FieldDataHandlerImpl : FieldDataHandler {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    val mapAdapter = moshi.adapter<Map<String, Any>>(type)

    private val data = mutableMapOf<String, Any>()

    override fun setValue(
        path: String?,
        value: Any
    ) {
        updateValue(path, value)
    }

    override suspend fun asJsonString(): String {
        return mapAdapter.toJsonValue(data).toString()
    }

    override fun asMap(): Map<String, Any> {
        return data
    }

    override fun clear() {
        data.clear()
    }

    private fun updateValue(string: String?, any: Any): Map<String, Any> {
        return data.toMap()
    }
}