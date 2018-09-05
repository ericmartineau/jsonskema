package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SingleSchemaKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest

data class SingleSchemaKeywordDigester(val keyword: KeywordInfo<SingleSchemaKeyword>) : KeywordDigester<SingleSchemaKeyword> {

  override val includedKeywords = listOf(keyword)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<SingleSchemaKeyword>? {
    val subschema = jsonObject.path(keyword)
    val schema = schemaLoader.loadSubSchema(subschema, subschema.rootObject, report)
    return keyword.digest(SingleSchemaKeyword(schema))
  }
}
