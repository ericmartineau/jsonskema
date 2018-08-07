package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonElement

data class JsonValueKeyword(val jsonElement: JsonElement) : JsonSchemaKeywordImpl<JsonElement>(jsonElement)
