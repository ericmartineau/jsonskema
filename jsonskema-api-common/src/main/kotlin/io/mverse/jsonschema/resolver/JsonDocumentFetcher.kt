package io.mverse.jsonschema.resolver

import lang.exception.nullPointer
import lang.net.URI
import lang.net.path
import lang.net.readFully
import lang.resources.readStringAsResource
import lang.time.currentTime

interface JsonDocumentFetcher {
  suspend fun fetchDocument(uri: URI): FetchedDocument
  val key: FetcherKey get() = this::class
}

class HttpDocumentFetcher : JsonDocumentFetcher {
  override suspend fun fetchDocument(uri: URI): FetchedDocument {
    val fetched = uri.readFully()
    return FetchedDocument(key, uri, uri, fetched)
  }
}

class ClasspathDocumentFetcher : JsonDocumentFetcher {
  override suspend fun fetchDocument(uri: URI): FetchedDocument {
    // Do some uri munging:
    return fetch(uri)
  }

  fun fetch(uri: URI): FetchedDocument {
    // Do some uri munging:
    val resolved = timed("resolved") {
      (uri.path?.readStringAsResource() ?: nullPointer("No schema found at classpath:/${uri.path}"))
    }
    return timed("build") {
      FetchedDocument(key, uri, URI("classpath:/" + uri.path), resolved)
    }
  }
}

inline fun <R> timed(name: String, block: () -> R): R {
  val long = currentTime()
  return try {
    block()
  } finally {
    println("Duration ($name): ${currentTime() - long}ms")
  }
}

