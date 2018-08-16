package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonArray

data class JsonArrayKeyword(override val value: JsonArray) : JsonSchemaKeywordImpl<JsonArray>() {
  override fun withValue(value: JsonArray): JsonSchemaKeyword<JsonArray> = this.copy(value = value)
}
