package io.mverse.jsonschema.keyword

data class BooleanKeyword(override val value: Boolean) : JsonSchemaKeywordImpl<Boolean>() {

  override fun withValue(value: Boolean): JsonSchemaKeyword<Boolean> {
    return this.copy(value = value)
  }
}
