package com.example.nanoboltron.jsonschema.datamanager

import com.example.nanoboltron.jsonschema.parser.UiDescriptorNode

sealed class DataBindingResult {
    class DataBound(renderableNode: RenderableNode, val data: Any) :
        DataBindingResult()

    class Error(val message: String) : DataBindingResult()

    data object DataUpdated : DataBindingResult()
}