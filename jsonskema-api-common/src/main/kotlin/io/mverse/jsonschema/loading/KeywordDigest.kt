package io.mverse.jsonschema.loading


import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword

/**
 * The result of a [KeywordDigester] processing a json document to extract keywords.
 *
 * Returns a keyword value that was retrieved from the document, along with the keyword metadata that sourced it.
 */
data class KeywordDigest<K : JsonSchemaKeyword<*>>(
  val keyword: KeywordInfo<K>,
  val keywordValue: K) {

  companion object {
    fun <K : JsonSchemaKeyword<*>> of(keyword: KeywordInfo<K>, value: K): KeywordDigest<K> {
      return KeywordDigest(keyword, value)
    }

    fun <K : JsonSchemaKeyword<*>> ofNullable(keyword: KeywordInfo<K>, value: K): KeywordDigest<K>? {
      return KeywordDigest(keyword, value)
    }
  }
}
