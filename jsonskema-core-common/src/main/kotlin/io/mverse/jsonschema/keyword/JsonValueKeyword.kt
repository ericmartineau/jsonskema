package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonElement

data class JsonValueKeyword(override val value: JsonElement) : JsonSchemaKeywordImpl<JsonElement>()
