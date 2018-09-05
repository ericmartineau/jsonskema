package io.mverse.jsonschema.loading

import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo

/**
 * The result of a [KeywordDigester] processing a json document to extract keywords.
 *
 * Returns a keyword value that was retrieved from the document, along with the keyword metadata that sourced it.
 */
data class KeywordDigest<K : Keyword<*>>(
    val keyword: KeywordInfo<K>,
    val kvalue: K)

fun <K : Keyword<*>> KeywordInfo<K>.digest(value: K): KeywordDigest<K> {
  return KeywordDigest(this, value)
}
