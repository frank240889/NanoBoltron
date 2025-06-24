package com.example.nanoboltron.jsonschema.parser

sealed class Items {
    data class ObjDescriptor(val item: UiDescriptorNode) : Items()
    data class ArrDescriptor(val items: List<UiDescriptorNode>) : Items()
}