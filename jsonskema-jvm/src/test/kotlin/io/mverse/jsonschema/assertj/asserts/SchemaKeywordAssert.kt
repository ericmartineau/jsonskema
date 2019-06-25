package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.keyword.Keyword

typealias SchemaKeywordAssert<K> = Assert<Keyword<K>>

fun <K> SchemaKeywordAssert<K>.withAssertion(assertion: (K?) -> Unit): SchemaKeywordAssert<K> {
  given {actual->
    assertion(actual.value)
  }
  return this
}

fun <K> SchemaKeywordAssert<K>.hasValue(value: K): SchemaKeywordAssert<K> {
  transform("keyword.value"){it.value}.isEqualTo(value)
  return this
}

