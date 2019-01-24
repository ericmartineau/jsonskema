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
import io.mverse.jsonschema.utils.createDispatcher
import io.mverse.logging.MLog
import io.mverse.logging.MLogged
import io.mverse.logging.duration
import io.mverse.logging.mlogger
import io.mverse.logging.timed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import lang.collection.asList
import lang.coroutines.TimeoutException
import lang.coroutines.awaitFirstOrNull
import lang.coroutines.blocking
import lang.exception.illegalArgument
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI
import lang.net.path
import kotlin.reflect.KClass

/**
 * A [JsonDocumentClient] implementation which uses a collection of [JsonDocumentFetcher] to find
 * schemas from various places (classpath, URL, memory, etc).  Uses coroutines to simultaneous
 * check all fetcher instances, and the first one to return a valid result wins, while the others
 * are cancelled.
 */
open class DefaultJsonDocumentClient(val fetchers: MutableList<JsonDocumentFetcher> = defaultFetchers.toMutableList(),
                                     val schemaCache: SchemaCache = SchemaCache(),
                                     val fetchTimeout: Long = 10000L) : JsonDocumentClient {

  constructor(fetcher: JsonDocumentFetcher, vararg moreFetchers: JsonDocumentFetcher, fetchTimeout: Long = 10000L)
      : this((fetcher.asList() + moreFetchers).toMutableList(), SchemaCache(), fetchTimeout)

  constructor(fetchTimeout: Long = 10000L)
      : this(schemaCache = SchemaCache(), fetchTimeout = fetchTimeout)

  init {
    if (fetchers.isEmpty()) {
      illegalArgument("Must provide at least one fetcher implementation")
    }
  }

  val dispatcher: CoroutineDispatcher = createDispatcher("json-document-fetch-%d")

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
    debug.timed("fetchall: ${uri.path}") {
      return fetchAll(uri, it).orThrow()
    }
  }

  fun fetchAll(uri: URI, mlog: MLog): FetchedDocumentResults {
    val errors: MutableMap<KClass<out JsonDocumentFetcher>, Throwable> = mutableMapOf()
    val deferreds = fetchers.mapIndexed { idx, fetcher ->
      GlobalScope.async(dispatcher) {
        mlog.duration("${fetcher.key.simpleName}-$idx") fetcher@{
          return@async try {
            val fetched = fetcher.fetchDocument(uri)
            fetched
          } catch (e: Exception) {
            mlog["${fetcher.key.simpleName}.error"] = "$e"
            errors[fetcher.key] = e
            null
          }
        }
      }
    }

    val fetched = blocking {
      supervisorScope {
        try {
          mlog.duration("await") {
            val awaited = deferreds.awaitFirstOrNull(fetchTimeout)
            awaited
          }
        } catch (e: TimeoutException) {
          mlog["timeout"] = true
          null
        } catch (e: Exception) {
          mlog["error"] = e.message
          null
        }
      }
    }

    return FetchedDocumentResults(errors, result = fetched)
  }

  override fun plusAssign(fetcher: JsonDocumentFetcher) {
    fetchers += fetcher
  }

  companion object : MLogged(mlogger {}) {
    val defaultFetchers = listOf(ClasspathDocumentFetcher(), HttpDocumentFetcher())
  }
}

