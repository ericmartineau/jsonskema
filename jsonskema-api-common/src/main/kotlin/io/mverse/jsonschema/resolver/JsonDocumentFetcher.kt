package io.mverse.jsonschema.resolver

import lang.exception.nullPointer
import lang.net.URI
import lang.net.path
import lang.net.readFully
import lang.resources.readStringAsResource

interface JsonDocumentFetcher {
  suspend fun fetchDocument(uri: URI): FetchedDocument
  val key: FetcherKey get() = this::class
  val isInterruptable: Boolean get() = false
}

class HttpDocumentFetcher : JsonDocumentFetcher {
  override suspend fun fetchDocument(uri: URI): FetchedDocument {
    val fetched = uri.readFully()
    return FetchedDocument(key, uri, uri, fetched)
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
