package io.mverse.jsonschema.keyword

data class StringKeyword(private val string: String) : JsonSchemaKeywordImpl<String>(string)
