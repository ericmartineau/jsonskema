package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.DollarSchemaKeyword
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.SCHEMA_KEYWORD
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest

class SchemaKeywordDigester : KeywordDigester<DollarSchemaKeyword> {
  override val includedKeywords: List<KeywordInfo<DollarSchemaKeyword>>
    get() = listOf(SCHEMA)

  override fun extractKeyword(jsonObject: JsonValueWithPath,
                              builder: SchemaBuilder,
                              schemaLoader: SchemaLoader,
                              report: LoadingReport): KeywordDigest<DollarSchemaKeyword>? {
    return when (jsonObject.containsKey(SCHEMA_KEYWORD)) {
      true -> SCHEMA.digest(DollarSchemaKeyword())
      false -> null
    }
  }
}
