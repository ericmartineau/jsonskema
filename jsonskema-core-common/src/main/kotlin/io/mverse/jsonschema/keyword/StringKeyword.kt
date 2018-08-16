package io.mverse.jsonschema.keyword

data class StringKeyword(override val value: String) : JsonSchemaKeywordImpl<String>() {
  override fun withValue(value: String): JsonSchemaKeyword<String> = this.copy(value=value)
}
