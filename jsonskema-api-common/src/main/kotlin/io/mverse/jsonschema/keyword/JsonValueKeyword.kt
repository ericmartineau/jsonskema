package io.mverse.jsonschema.keyword

import kotlinx.serialization.json.JsonElement

data class JsonValueKeyword(override val value: JsonElement) : KeywordImpl<JsonElement>() {
  override fun withValue(value: JsonElement): Keyword<JsonElement> = this.copy(value = value)
}
