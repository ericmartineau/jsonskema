package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaListKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues.typeMismatch
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import lang.json.JsrType

data class ListKeywordDigester(val keyword: KeywordInfo<SchemaListKeyword>) : KeywordDigester<SchemaListKeyword> {

  override val includedKeywords = listOf(keyword)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<SchemaListKeyword>? {
    val schemas = mutableListOf<Schema>()
    val jsonArray = jsonObject.path(keyword)
    jsonArray.forEachIndex { _, item ->
      if (item.type !== JsrType.OBJECT) {
        report.error(typeMismatch(keyword, item))
      } else {
        schemas.add(schemaLoader.loadSubSchema(item, item.rootObject, report))
      }
    }

    return keyword.digest(SchemaListKeyword(schemas))
  }
}
