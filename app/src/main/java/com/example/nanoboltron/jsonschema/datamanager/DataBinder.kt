package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.UiDataNode
import com.example.nanoboltron.jsonschema.core.Key

interface DataBinder {
    fun setData(data: String)
    fun getData(): String?
    fun bindData(key: Key, value: Any, path: String?): UiDataNode?
    fun update(key: Key, value: Any, path: String?): Boolean
}