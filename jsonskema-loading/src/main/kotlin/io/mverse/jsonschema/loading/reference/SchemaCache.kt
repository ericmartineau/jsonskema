package io.mverse.jsonschema.loading.reference

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.utils.JsonUtils.tryParseURI
import io.mverse.jsonschema.utils.recurse
import io.mverse.jsonschema.utils.trimEmptyFragment
import kotlinx.serialization.json.JsonObject
import lang.URI

/**
 * @author erosb
 */
data class SchemaCache(
    private val documentIdRefs:MutableMap<URI, Map<URI, JsonPath>> = hashMapOf(),
    private val absoluteDocumentCache:MutableMap<URI, kotlinx.serialization.json.JsonObject> = hashMapOf(),
    private val absoluteSchemaCache:MutableMap<URI, Schema> = hashMapOf()
) {

  operator fun plusAssign(pair: Pair<URI, Schema>) = cacheSchema(pair.first, pair.second)
  operator fun plus(pair: Pair<URI, Schema>):SchemaCache = apply { cacheSchema(pair.first, pair.second) }

  fun cacheSchema(schemaURI: URI, schema: Schema) {
    check(schemaURI.isAbsolute) {"Must be an absolute URI"}
    absoluteSchemaCache[schemaURI.normalize()] = schema
  }

  fun cacheDocument(documentURI: URI, document: kotlinx.serialization.json.JsonObject) {
    if (documentURI.isAbsolute) {
      absoluteDocumentCache[documentURI.normalize()] = document
    }
  }

  fun lookupDocument(documentURI: URI): kotlinx.serialization.json.JsonObject? {
    return absoluteDocumentCache[documentURI.normalize()]
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
      if (uri.isAbsolute) {
        val hit = absoluteSchemaCache[uri.normalize()]
        if (hit != null) {
          return hit
        }
      }
    }
    return null
  }

  fun resolveURIToDocumentUsingLocalIdentifiers(documentURI: URI, absoluteURI: URI, document: kotlinx.serialization.json.JsonObject): JsonPath? {

    val paths = documentIdRefs.getOrPut(documentURI.normalize()) {
      val values = hashMapOf<URI, JsonPath>()
      document.recurse { keyOrIndex, visited, location ->
        if (keyOrIndex == Keywords.DOLLAR_ID_KEY || keyOrIndex == Keywords.ID_KEY) {
          tryParseURI(visited)?.also { id ->
            val absoluteIdentifier = documentURI.normalize().resolve(id.normalize())
            values[absoluteIdentifier] = location
          }
        }
      }
      return@getOrPut values
    }

    return paths[absoluteURI.normalize()]
  }

  private fun URI.normalize():URI {
    return trimEmptyFragment()
  }

}
