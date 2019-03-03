package io.mverse.jsonschema.resolver

import io.mverse.jsonschema.utils.httpGet
import kotlinx.io.core.readText
import lang.exception.nullPointer
import lang.net.URI
import lang.net.path
import lang.net.scheme
import lang.resources.readStringAsResource
import lang.string.Charsets
import lang.string.toString

interface JsonDocumentFetcher {
  suspend fun fetchDocument(uri: URI): FetchedDocument
  val key: FetcherKey get() = this::class
  val isInterruptable: Boolean get() = false
  fun handles(refURI: URI): Boolean = true
}

class HttpDocumentFetcher : JsonDocumentFetcher {
  override suspend fun fetchDocument(uri: URI): FetchedDocument {
    val fetched = uri.httpGet().toString(Charsets.UTF_8)
    return FetchedDocument(key, uri, uri, fetched)
  }

  override fun handles(refURI: URI): Boolean {
    return refURI.scheme?.startsWith("http") == true
  }

  override val isInterruptable = true
}

class ClasspathDocumentFetcher : JsonDocumentFetcher {
  override suspend fun fetchDocument(uri: URI): FetchedDocument {
    // Do some uri munging:
    val resolved = (uri.path?.readStringAsResource()
        ?: nullPointer("No schema found at classpath:/${uri.path}"))

    return FetchedDocument(key, uri, URI("classpath:/" + uri.path), resolved)
  }
}
