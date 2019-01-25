package io.mverse.jsonschema.resolver

import lang.exception.nullPointer
import lang.json.JsrObject
import lang.string.indent
import lang.string.join
import lang.string.plusAssign
import lang.string.wrap
import kotlin.reflect.KClass

typealias FetcherKey = KClass<out JsonDocumentFetcher>

data class FetchedDocumentResults(val failures: Map<FetcherKey, Throwable> = emptyMap(),
                                  val cancelled: Set<FetcherKey> = emptySet(),
                                  val fetchedOrNull: FetchedDocument? = null) {
  val fetched: FetchedDocument
    get() = fetchedOrNull ?: nullPointer("No results for fetched document")
  val fetchedJsonOrNull = fetchedOrNull?.jsrObject
  val fetchedJson: JsrObject get() = fetched.jsrObject
}

class DocumentFetchException(result: FetchedDocumentResults) : Exception(result.toErrorString()) {
  val failures = result.failures

  companion object {
    fun FetchedDocumentResults.toErrorString(): String {
      failures.run {
        val builder = StringBuilder("Error fetching document with $size failures: \n")
        builder += toList().join { "${it.first.simpleName}: ${it.second}".indent() }.wrap()
        return builder.toString()
      }
    }
  }
}

