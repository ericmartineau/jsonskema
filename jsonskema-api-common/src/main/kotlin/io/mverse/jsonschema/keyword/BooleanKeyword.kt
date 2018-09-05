package io.mverse.jsonschema.keyword

data class BooleanKeyword(override val value: Boolean) : KeywordImpl<Boolean>() {

  override fun withValue(value: Boolean): Keyword<Boolean> {
    return this.copy(value = value)
  }
}
