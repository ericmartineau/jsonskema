package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonArray

data class JsonArrayKeyword(override val value: JsonArray) : KeywordImpl<JsonArray>() {
  override fun withValue(value: JsonArray): Keyword<JsonArray> = this.copy(value = value)
}
