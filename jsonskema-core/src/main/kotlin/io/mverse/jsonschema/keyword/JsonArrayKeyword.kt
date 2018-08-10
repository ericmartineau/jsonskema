package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonArray

data class JsonArrayKeyword(val jsonArray: kotlinx.serialization.json.JsonArray) : JsonSchemaKeywordImpl<kotlinx.serialization.json.JsonArray>(jsonArray)
