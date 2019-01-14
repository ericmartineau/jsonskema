package io.mverse.jsonschema.keyword

import lang.json.JsrValue

data class JsonValueKeyword(override val value: JsrValue) : KeywordImpl<JsrValue>() {
  override fun withValue(value: JsrValue): Keyword<JsrValue> = this.copy(value = value)
}
