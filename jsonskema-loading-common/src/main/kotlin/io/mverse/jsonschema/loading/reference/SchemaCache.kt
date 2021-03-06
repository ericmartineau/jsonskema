package io.mverse.jsonschema.loading.reference

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.utils.JsonUtils.tryParseURI
import io.mverse.jsonschema.utils.recurse
import io.mverse.jsonschema.utils.trimEmptyFragment
import lang.json.JsonPath
import lang.json.JsrObject
import lang.net.URI
import lang.net.resolveUri

/**
 * Responsible for caching resolved schemas.  Due to the possibility of self-referencing schemas, this
 * component is critical to avoid explosions.
 *
 * @author ericmartineau
 */
data class SchemaCache(
    private val documentIdRefs: MutableMap<URI, Map<URI, JsonPath>> = hashMapOf(),
    private val absoluteDocumentCache: MutableMap<URI, lang.json.JsrObject> = hashMapOf(),
    private val absoluteSchemaCache: MutableMap<URI, Schema> = hashMapOf()
) {

  operator fun plusAssign(pair: Pair<URI, Schema>) = cacheSchema(pair.first, pair.second)
  operator fun plus(pair: Pair<URI, Schema>): SchemaCache = apply { cacheSchema(pair.first, pair.second) }

  fun cacheSchema(schemaURI: URI, schema: Schema) {
    check(schemaURI.isAbsolute()) { "Must be an absolute URI" }
    absoluteSchemaCache[schemaURI.trimEmptyFragment()] = schema
  }

  fun cacheDocument(documentURI: URI, document: JsrObject) {
    val docURI = documentURI.trimEmptyFragment()
    if (docURI.isAbsolute()) {
      absoluteDocumentCache[docURI] = document
    }
  }

  fun lookupDocument(documentURI: URI): lang.json.JsrObject? {
    return absoluteDocumentCache[documentURI.trimEmptyFragment()]
  }

  fun cacheSchema(schema: Schema) {
    cacheSchema(schema.location, schema)
  }

  fun cacheSchema(location: SchemaLocation, schema: Schema) {
    val absoluteLocation = location.uniqueURI
    val jsonPointerLocation = location.absoluteJsonPointerURI
    cacheSchema(absoluteLocation, schema)
    cacheSchema(jsonPointerLocation, schema)
  }

  fun getSchema(schemaLocation: SchemaLocation): Schema? {
    //A schema can be cached in two places
    return getSchema(schemaLocation.uniqueURI, schemaLocation.canonicalURI)
  }

  operator fun get(schemaUri: URI): Schema? = this.getSchema(schemaUri)

  fun getSchema(vararg schemaURI: URI): Schema? {
    for (uri in schemaURI) {
      if (uri.isAbsolute()) {
        val hit = absoluteSchemaCache[uri.trimEmptyFragment()]
        if (hit != null) {
          return hit
        }
      }
    }
    return null
  }

  fun resolveURIToDocumentUsingLocalIdentifiers(documentURI: URI, absoluteURI: URI, document: lang.json.JsrObject): JsonPath? {

    val paths = documentIdRefs.getOrPut(documentURI.trimEmptyFragment()) {
      val values = hashMapOf<URI, JsonPath>()
      document.recurse { keyOrIndex, visited, location ->
        if (keyOrIndex == Keywords.DOLLAR_ID_KEY || keyOrIndex == Keywords.ID_KEY) {
          tryParseURI(visited)?.also { id ->
            val absoluteIdentifier = documentURI.trimEmptyFragment().resolveUri(id.trimEmptyFragment())
            values[absoluteIdentifier] = location
          }
        }
      }
      return@getOrPut values
    }

    return paths[absoluteURI.trimEmptyFragment()]
  }
}
