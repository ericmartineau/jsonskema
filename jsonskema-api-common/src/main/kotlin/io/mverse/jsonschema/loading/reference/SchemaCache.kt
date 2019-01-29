package io.mverse.jsonschema.loading.reference

import io.mverse.jsonschema.Schema
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI

interface SchemaCache {
  operator fun plusAssign(pair: Pair<URI, Schema>)

  operator fun plus(pair: Pair<URI, Schema>): SchemaCache
  operator fun set(uri:URI, schema:Schema)

  fun lookupDocument(documentURI: URI): JsrObject?
  fun cacheSchema(schema: Schema)

  operator fun get(schemaUri: URI): Schema?
  fun cacheDocument(documentURI: URI, document: JsrObject)
  fun resolveURIToDocumentUsingLocalIdentifiers(documentURI: URI, absoluteURI: URI, document: lang.json.JsrObject): JsonPath?
}