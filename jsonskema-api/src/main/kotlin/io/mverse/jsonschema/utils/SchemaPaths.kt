package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.utils.JsonUtils.extractIdFromObject
import kotlinx.serialization.json.JsonObject
import lang.URI
import lang.UUID

/**
 * Helper class for creating schema paths for different situations.
 */
interface SchemaPaths {
  companion object {

    /**
     * Constructs a SchemaLocation instance given an $id that may not be absolute.  In this case,
     * we prepend a unique URI so we can still take advantage of caching.
     */
    fun fromIdNonAbsolute(id: URI): SchemaLocation {
      if (id.isAbsolute) {
        return SchemaLocation.builderFromId(id).build()
      } else {
        val uniqueURI = generateUniqueURI(UUID.randomUUID())
        val resolvedFromUnique = uniqueURI.resolve(id)
        val locationBuilder = SchemaLocation.builderFromId(resolvedFromUnique)
        if (id.isJsonPointer()) {
          locationBuilder.jsonPath(JsonPath.parseFromURIFragment(id))
        }
        return locationBuilder.build()
      }
    }

    /**
     * This builds a uniquely hashed URI from an object source.  Providing the same object source
     * later, woudl return in the same SchemaLocation being created.
     */
    fun fromNonSchemaSource(value: Any): SchemaLocation {
      return SchemaLocation.builderFromId(generateUniqueURI(value)).build()
    }

    /**
     * This builds an instance from an absolute $id URI.
     */
    fun fromId(id: URI): SchemaLocation {
      check(id.isAbsolute) { "\$id must be absolute" }
      return SchemaLocation.builderFromId(id).build()
    }

    /**
     * This builds an instance from the schema values loaded into a builder.  Another builder with
     * the exact same keyword configuration woudl have the same location.
     */
    fun fromBuilder(builder: SchemaBuilder<*>): SchemaLocation {
      val uniqueURIFromBuilder = generateUniqueURI(builder)
      return SchemaLocation.builderFromId(uniqueURIFromBuilder).build()
    }

    /**
     * This builds a unique instance from a json document.  We look for an $id, and build the location
     * based on that.  If no $id is found, then a location that is unique to the json document will
     * be created.
     */
    fun fromDocument(documentJson: kotlinx.serialization.json.JsonObject, idKey: String, vararg otherIdKeys: String): SchemaLocation {
      // There are three cases here.

      val id = extractIdFromObject(documentJson, idKey, *otherIdKeys)
      return fromDocumentWithProvidedId(documentJson, id)
    }

    fun fromDocumentWithProvidedId(documentRoot: kotlinx.serialization.json.JsonObject, id: URI?): SchemaLocation {

      return when {
        id == null-> SchemaLocation.builderFromId(generateUniqueURI(documentRoot)).build()
        id.isAbsolute->SchemaLocation.builderFromId(id).build()
        else-> {
          val uniqueURI = generateUniqueURI(documentRoot)
          val resolvedFromUnique = uniqueURI.resolve(id)
          val locationBuilder = SchemaLocation.builderFromId(resolvedFromUnique)
          if (id.isJsonPointer()) {
            locationBuilder.jsonPath(JsonPath.parseFromURIFragment(id))
          }
          locationBuilder.build()
        }
      }
    }
  }
}
