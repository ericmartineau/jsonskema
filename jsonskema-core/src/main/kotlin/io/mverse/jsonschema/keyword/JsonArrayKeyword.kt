package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonArray

data class JsonArrayKeyword(val jsonArray: JsonArray) : JsonSchemaKeywordImpl<JsonArray>(jsonArray)
