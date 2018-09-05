package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keyword
import kotlinx.serialization.json.JsonElement
import lang.Name
import lang.URI

interface Schema {

  val location: SchemaLocation

  val id: URI?

  val schemaURI: URI?

  val title: String?

  val description: String?

  val version: JsonSchemaVersion?

  val extraProperties: Map<String, JsonElement>

  val keywords: Map<KeywordInfo<*>, Keyword<*>>

  val absoluteURI: URI
    get() = location.uniqueURI

  val pointerFragmentURI: URI?
    get() = location.jsonPointerFragment

  /**
   * Creates a copy of this schema with the provided schema id
   */
  @Name("withId")
  fun withId(id: URI): Schema

  fun toJson(version: JsonSchemaVersion = JsonSchemaVersion.latest): kotlinx.serialization.json.JsonObject

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

  fun toBuilder(): SchemaBuilder

  fun toBuilder(id: URI): SchemaBuilder

  @Name("asDraft3")
  fun asDraft3(): Draft3Schema

  @Name("asDraft4")
  fun asDraft4(): Draft4Schema

  @Name("asDraft6")
  fun asDraft6(): Draft6Schema

  @Name("asDraft7")
  fun asDraft7(): Draft7Schema

  fun toString(version:JsonSchemaVersion):String
}
