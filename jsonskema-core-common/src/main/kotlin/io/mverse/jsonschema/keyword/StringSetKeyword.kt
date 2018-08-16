package io.mverse.jsonschema.keyword

data class StringSetKeyword(override val value: Set<String> = setOf()) : JsonSchemaKeywordImpl<Set<String>>() {
  constructor(value: String) : this(setOf(value))

  operator fun plus(other: String): StringSetKeyword {
    return StringSetKeyword(value + other)
  }

  fun withAnotherValue(anotherValue: String): StringSetKeyword {
    return this + anotherValue
  }

  override fun withValue(value: Set<String>): JsonSchemaKeyword<Set<String>> = this.copy(value=value)
}
