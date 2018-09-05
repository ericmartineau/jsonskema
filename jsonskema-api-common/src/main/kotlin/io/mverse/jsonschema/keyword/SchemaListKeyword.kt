package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema

open class SchemaListKeyword(final override val value: List<Schema> = listOf())
  // Extends
  : SubschemaKeyword, KeywordImpl<List<Schema>>() {

  override fun withValue(value: List<Schema>): Keyword<List<Schema>> = SchemaListKeyword(value)

  override val subschemas: List<Schema> = value

  operator fun plus(schema:Schema): SchemaListKeyword {
    return SchemaListKeyword(value + schema)
  }

  operator fun plus(schema:List<Schema>): SchemaListKeyword {
    return SchemaListKeyword(value + schema)
  }
}
