package com.example.nanoboltron.jsonschema.parser

import android.util.Log

private const val TAG = "SchemaPrinter"
fun printUiTree(node: UiDescriptorNode, indent: String = "") {
    //val info = "[${node::class.simpleName}] key=${node.key}, path=${node.path}, type=${node.type}, title=${node.title}, description=${node.description}"
    Log.i(TAG, "$indent$node")

    when (node) {
        is UiDescriptorNode.GroupNode -> {
            node.properties?.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        is UiDescriptorNode.RepeatingGroupNode -> {
            node.items?.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        else -> Unit
    }
}
