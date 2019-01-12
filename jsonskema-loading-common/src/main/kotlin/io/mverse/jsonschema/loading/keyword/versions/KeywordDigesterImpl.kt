package io.mverse.jsonschema.loading.keyword.versions

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.KeywordLoader
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest

class KeywordDigesterImpl<T : Keyword<*>>(val keyword: KeywordInfo<T>, private val loader: KeywordLoader<T>) : KeywordDigester<T> {

  override val includedKeywords: List<KeywordInfo<T>> = listOf(keyword)

  override fun extractKeyword(jsonObject: JsonValueWithPath,
                              builder: SchemaBuilder,
                              schemaLoader: SchemaLoader,
                              report: LoadingReport): KeywordDigest<T>? {
    return loader
        .loadKeyword(jsonObject.path(keyword))
        ?.let { k -> keyword.digest(k) }
  }
}
