package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.URI

interface Schema {

  val location: SchemaLocation

  val id: URI?

  val schemaURI: URI?

  val title: String?

  val description: String?

  val version: JsonSchemaVersion?

  val extraProperties: Map<String, JsonElement>

  val keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>>

  val absoluteURI: URI
    get() = location.uniqueURI

  val pointerFragmentURI: URI?
    get() = location.jsonPointerFragment

  /**
   * Creates a copy of this schema with the provided schema id
   */
  fun withId(id: URI): Schema

  fun toJson(version: JsonSchemaVersion = JsonSchemaVersion.latest()):JsonObject

  fun asVersion(version: JsonSchemaVersion): Schema {
    return when (version) {
      JsonSchemaVersion.Draft3 -> asDraft3()
      JsonSchemaVersion.Draft4 -> asDraft4()
      JsonSchemaVersion.Draft5 -> asDraft4()
      JsonSchemaVersion.Draft6 -> asDraft6()
      JsonSchemaVersion.Draft7 -> asDraft7()
      else -> throw IllegalArgumentException("Unable to determine version from: $version")
    }
  }

  fun <X : SchemaBuilder<X>> toBuilder(): X

  fun <X : SchemaBuilder<X>> toBuilder(id: URI): X

  fun asDraft3(): Draft3Schema

  fun asDraft4(): Draft4Schema

  fun asDraft6(): Draft6Schema

  fun asDraft7(): Draft7Schema
}
