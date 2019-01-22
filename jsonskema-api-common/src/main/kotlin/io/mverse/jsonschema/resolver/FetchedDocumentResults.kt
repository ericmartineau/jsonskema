package io.mverse.jsonschema.resolver

import lang.json.JsrObject
import lang.string.indent
import lang.string.join
import lang.string.plusAssign
import lang.string.wrap
import kotlin.reflect.KClass

typealias FetcherKey = KClass<out JsonDocumentFetcher>

data class FetchedDocumentResults(val errors: Map<FetcherKey, Throwable> = emptyMap(),
                                  val cancelled: Set<FetcherKey> = emptySet(),
                                  val result: FetchedDocument? = null) {
  fun orThrow(): FetchedDocumentResults = when (result) {
    null -> throw DocumentFetchException(this)
    else -> this
  }

  val fetchedDocument: FetchedDocument get() = result!!
  val fetchedJson: JsrObject get() = fetchedDocument.jsrObject
}

class DocumentFetchException(result: FetchedDocumentResults) : Exception(result.toErrorString()) {
  val failures = result.errors

  companion object {
    private fun FetchedDocumentResults.toErrorString(): String {
      errors.run {
        val builder = StringBuilder("Error fetching document with $size failures: \n")
        builder += toList().join { "${it.first.simpleName}: ${it.second}".indent() }.wrap()
        return builder.toString()
      }
    }
  }
}

