package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaMergeStrategy
import io.mverse.jsonschema.mergeCombine
import lang.json.JsonPath

open class SchemaListKeyword(final override val value: List<Schema> = listOf())
// Extends
  : SubschemaKeyword, KeywordImpl<List<Schema>>() {

  override fun withValue(value: List<Schema>): Keyword<List<Schema>> = SchemaListKeyword(value)

  override val subschemas: List<Schema> = value

  operator fun plus(schema: Schema): SchemaListKeyword {
    return SchemaListKeyword(value + schema)
  }

  operator fun plus(schema: List<Schema>): SchemaListKeyword {
    return SchemaListKeyword(value + schema)
  }

  override fun merge(merger: SchemaMergeStrategy, path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<List<Schema>>, report: MergeReport): Keyword<List<Schema>> {
    report += mergeCombine(path, keyword, this.value, other.value)
    return SchemaListKeyword(this.value + other.value)
  }
}
