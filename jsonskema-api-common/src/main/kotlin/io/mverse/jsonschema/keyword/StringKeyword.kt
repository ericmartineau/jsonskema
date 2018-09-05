package io.mverse.jsonschema.keyword

data class StringKeyword(override val value: String) : KeywordImpl<String>() {
  override fun withValue(value: String): Keyword<String> = this.copy(value=value)
}
