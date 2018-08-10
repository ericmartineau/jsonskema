package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword

typealias SchemaKeywordAssert<K> = Assert<JsonSchemaKeyword<K>>

fun <K> SchemaKeywordAssert<K>.withAssertion(assertion: (K?) -> Unit): SchemaKeywordAssert<K> {
  assertion(actual.value)
  return this
}

fun <K> SchemaKeywordAssert<K>.hasValue(value: K): SchemaKeywordAssert<K> {
  assert(actual.value, "keyword.value").isEqualTo(value)
  return this
}

