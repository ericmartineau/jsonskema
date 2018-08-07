package io.mverse.jsonschema.keyword

data class StringSetKeyword(val stringSet:Set<String> = setOf()) : JsonSchemaKeywordImpl<Set<String>>(stringSet) {

  constructor(value: String) : this(setOf(value))

  fun withAnotherValue(anotherValue: String): StringSetKeyword {
    return StringSetKeyword(stringSet + anotherValue)
  }

}
