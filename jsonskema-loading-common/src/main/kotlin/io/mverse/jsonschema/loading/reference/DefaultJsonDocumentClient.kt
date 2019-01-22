/*
 * Copyright (C) 2017 MVerse (http://mverse.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mverse.jsonschema.loading.reference

import io.mverse.jsonschema.loading.JsonDocumentClient
import io.mverse.jsonschema.resolver.ClasspathDocumentFetcher
import io.mverse.jsonschema.resolver.FetchedDocument
import io.mverse.jsonschema.resolver.FetchedDocumentResults
import io.mverse.jsonschema.resolver.HttpDocumentFetcher
import io.mverse.jsonschema.resolver.JsonDocumentFetcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import lang.coroutine.TimeoutException
import lang.coroutine.awaitFirstOrNull
import lang.coroutine.blocking
import lang.exception.illegalArgument
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI
import kotlin.reflect.KClass

/**
 * A [JsonDocumentClient] implementation which uses a collection of [JsonDocumentFetcher] to find
 * schemas from various places (classpath, URL, memory, etc).  Uses coroutines to simultaneous
 * check all fetcher instances, and the first one to return a valid result wins, while the others
 * are cancelled.
 */
open class DefaultJsonDocumentClient(val fetchers: MutableList<JsonDocumentFetcher> = defaultFetchers.toMutableList(),
                                     val schemaCache: SchemaCache,
                                     val fetchTimeout: Long = 10000L) : JsonDocumentClient {

  constructor(fetchers: List<JsonDocumentFetcher> = defaultFetchers, fetchTimeout: Long = 10000L) : this(fetchers.toMutableList(), SchemaCache(), fetchTimeout)

  init {
    if (fetchers.isEmpty()) {
      illegalArgument("Must provide at least one fetcher implementation")
    }
  }

  override fun findLoadedDocument(documentLocation: URI): JsrObject? {
    return schemaCache.lookupDocument(documentLocation)
  }

  override fun registerFetchedDocument(document: FetchedDocument) {
    schemaCache.cacheDocument(document.originalUri, document.jsrObject)
    if (document.isDifferentURI) {
      schemaCache.cacheDocument(document.uri, document.jsrObject)
    }
  }

  override fun registerFetchedDocument(documentLocation: URI, document: JsrObject) {
    schemaCache.cacheDocument(documentLocation, document)
  }

  override fun resolveSchemaWithinDocument(documentURI: URI, schemaURI: URI, document: JsrObject): JsonPath? {
    return schemaCache.resolveURIToDocumentUsingLocalIdentifiers(documentURI, schemaURI, document)
  }

  override fun fetchDocument(uri: URI): FetchedDocumentResults {
    val errors: MutableMap<KClass<out JsonDocumentFetcher>, Throwable> = mutableMapOf()
    return blocking fetch@{
      supervisorScope {
        val deferreds = fetchers.map { fetcher ->
          async(Dispatchers.Default) {
            try {
              val fetched = fetcher.fetchDocument(uri)
              return@async fetched
            } catch (e: Exception) {
              errors[fetcher.key] = e
              return@async null
            }
          }
        }

        try {
          deferreds.awaitFirstOrNull(fetchTimeout)
        } catch (e: TimeoutException) {
          null
        }.let { fetched ->
          val unaccounted = fetchers.map { it::class }.filter {
            it !in errors && fetched?.fetcherKey != it
          }.toSet()

          FetchedDocumentResults(errors, unaccounted, fetched).orThrow()
        }
      }
    }
  }

  override fun plusAssign(fetcher: JsonDocumentFetcher) {
    fetchers += fetcher
  }

  companion object {
    val defaultFetchers = listOf(ClasspathDocumentFetcher(), HttpDocumentFetcher())
  }
}

