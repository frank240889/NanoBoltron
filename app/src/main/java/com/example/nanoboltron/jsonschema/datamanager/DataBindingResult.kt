package com.example.nanoboltron.jsonschema.datamanager

sealed class DataBindingResult {
    class DataBound(renderableNode: SimpleRenderableNode, val data: Any) :
        DataBindingResult()

    class Error(val message: String) : DataBindingResult()

    data object DataUpdated : DataBindingResult()
}