package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.Key
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class DataBinderImpl : DataBinder {
    private val moshi = Moshi.Builder().build()
    private val type =
        Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
    private val mapAdapter = moshi.adapter<Map<String, Any?>>(type)
    private var model: Map<String, Any?>? = null

    override fun setData(data: String) {
        model = mapAdapter.fromJson(data) ?: mapOf()
    }

    override fun getData(): String? {
       return mapAdapter.toJsonValue(model).toString()
    }

    override fun bindData(
        key: Key,
        value: Any,
        path: String?
    ): DataBindingResult {

        return DataBindingResult.DataBound("", "", "")
    }

    override fun update(
        key: Key,
        value: Any,
        path: String?
    ): DataBindingResult {
        TODO("Not yet implemented")
    }
}