package com.example.nanoboltron.jsonschema.parser

import android.util.Log
import com.example.nanoboltron.jsonschema.parser.parsers.DescriptorNode

private const val TAG = "SchemaPrinter"
fun printUiTree(node: DescriptorNode, indent: String = "") {
    Log.i(TAG, "$indent$node")
    Log.i(TAG, "\n")

    when (node) {
        is DescriptorNode.GroupNode -> {
            node.nodes?.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        else -> Unit
    }
}

private const val TAG2 = "DataPrinter"
fun printUiTree(node: FormDataNode, indent: String = "") {
    Log.i(TAG2, "$indent$node")

    when (node) {
        is FormDataNode.ObjectData -> {
            node.children.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        is FormDataNode.ArrayData-> {
            node.items.forEach { child ->
                printUiTree(child, "$indent  ")
            }
        }

        else -> Unit
    }
}
