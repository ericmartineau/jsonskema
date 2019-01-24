package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaMapKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import lang.json.JsrType

data class MapKeywordDigester(val keyword: KeywordInfo<SchemaMapKeyword>) : KeywordDigester<SchemaMapKeyword> {

  override val includedKeywords get() = listOf(keyword)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<SchemaMapKeyword>? {
    val keyedSchemas = mutableMapOf<String, Schema>()
    val propObject = jsonObject.path(keyword)
    propObject.forEachKey { key, value ->
      if (value.type !== JsrType.OBJECT) {
        report.error(LoadingIssues.typeMismatch(keyword, value))
      } else {
        keyedSchemas[key] = schemaLoader.loadSubSchema(value, value.rootObject, report)
      }
    }
    return keyword.digest(SchemaMapKeyword(keyedSchemas))
  }
}
