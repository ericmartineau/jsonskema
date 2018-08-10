package io.mverse.jsonschema.loading.keyword.versions

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.CustomKeywordLoader
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaKeyword
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.convert

class KeywordDigesterImpl<T : JsonSchemaKeyword<*>>(val keyword: KeywordInfo<T>, private val loader: CustomKeywordLoader<T>) : KeywordDigester<T> {

  override val includedKeywords: List<KeywordInfo<T>> = listOf(keyword)

  override fun extractKeyword(jsonObject: JsonValueWithPath,
                              builder: SchemaBuilder<*>,
                              schemaLoader: SchemaLoader,
                              report: LoadingReport): KeywordDigest<T>? {
    return loader
        .loadKeywordFromJsonValue(jsonObject.path(keyword))
        ?.convert { k -> KeywordDigest.of(keyword, k) }
  }
}
