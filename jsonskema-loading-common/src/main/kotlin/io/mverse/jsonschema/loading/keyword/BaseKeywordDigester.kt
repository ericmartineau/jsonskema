package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.json.JsrType
import lang.json.JsrValue

abstract class BaseKeywordDigester<T : Keyword<*>>(val keyword: KeywordInfo<T>,
                                                   vararg expectedTypes: JsrType) : KeywordDigester<T> {
  override val includedKeywords: List<KeywordInfo<T>> = when (expectedTypes.isNotEmpty()) {
    true -> keyword.getTypeVariants(*expectedTypes)
    false -> listOf(keyword)
  }

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema, schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<T>? {
    if (!jsonObject.containsKey(keyword.key)) return null

    val JsrValue = jsonObject[keyword.key]
    val keywordValue = extractKeyword(JsrValue)
    return KeywordDigest(keyword, keywordValue)
  }

  protected abstract fun extractKeyword(jsonValue: JsrValue): T
}
