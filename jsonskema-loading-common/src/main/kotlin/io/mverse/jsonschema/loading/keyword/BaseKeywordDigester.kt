package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.ElementType

abstract class BaseKeywordDigester<T : Keyword<*>>(val keyword: KeywordInfo<T>,
                                                   vararg expectedTypes: ElementType) : KeywordDigester<T> {
  override val includedKeywords: List<KeywordInfo<T>> = when (expectedTypes.isNotEmpty()) {
    true -> keyword.getTypeVariants(*expectedTypes)
    false-> listOf(keyword)
  }

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder, schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<T>? {
    if(!jsonObject.containsKey(keyword.key)) return null

    val jsonElement = jsonObject[keyword.key]
    val keywordValue = extractKeyword(jsonElement)
    return KeywordDigest(keyword, keywordValue)
  }

  protected abstract fun extractKeyword(jsonElement: JsonElement): T
}
