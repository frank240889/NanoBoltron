package com.example.nanoboltron.jsonschema.parser

sealed class ArrayItems {
    data class ObjDescriptor(val item: UiNodeDescriptor) : ArrayItems()
    data class ArrDescriptor(val items: List<UiNodeDescriptor>) : ArrayItems()
}