package io.mverse.jsonschema.keyword

import lang.json.JsrArray

data class JsonArrayKeyword(override val value: JsrArray) : KeywordImpl<JsrArray>() {
  override fun withValue(value: JsrArray): Keyword<JsrArray> = this.copy(value = value)
}
