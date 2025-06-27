package com.example.nanoboltron.jsonschema

/**
 * Mandatory key words that are involved in how the UI is rendered.
 */
internal const val TYPE = "type"
internal const val TITLE = "title"
internal const val DESCRIPTION = "description"
internal const val DEFAULT = "default"
internal const val READ_ONLY = "readOnly"
internal const val WRITE_ONLY = "writeOnly"
internal const val ENUM = "enum"
internal const val CONTENT_MEDIA_TYPE = "contentMediaType"
internal const val CONTENT_ENCODING = "contentEncoding"
internal const val ITEMS = "items"
internal const val FORMAT = "format"
internal const val PROPERTIES = "properties"

/**
 * Data types
 */
internal const val STRING_NODE = "string"
internal const val NUMBER_NODE = "number"
internal const val INTEGER_NODE = "integer"
internal const val BOOLEAN_NODE = "boolean"
internal const val OBJECT_NODE = "object"
internal const val ARRAY_NODE = "array"

/**
 * UI types
 */
internal const val GROUP = "group"
internal const val REPEATABLE_GROUP = "repeatable"

/**
 * Subschemas
 */
internal const val ANY_OF = "anyOf"
internal const val ONE_OF = "oneOf"
internal const val ALL_OF = "allOf"
internal val selectionOperators = arrayOf(ANY_OF, ONE_OF, ALL_OF)

/**
 * Conditional
 */
internal const val IF = "if"
internal const val THEN = "then"
internal const val ELSE = "else"
