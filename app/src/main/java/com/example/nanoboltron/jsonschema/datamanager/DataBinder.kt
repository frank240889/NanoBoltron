package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.Key

interface DataBinder {
    fun setData(data: String)
    fun getData(): String?
    fun bindData(key: Key, value: Any, path: String?): DataBindingResult
    fun update(key: Key, value: Any, path: String?): DataBindingResult
}