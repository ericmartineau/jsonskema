package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonArray

data class JsonArrayKeyword(override val value: JsonArray) : JsonSchemaKeywordImpl<JsonArray>()
