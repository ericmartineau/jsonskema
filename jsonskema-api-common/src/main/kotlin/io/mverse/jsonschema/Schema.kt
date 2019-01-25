package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import lang.Name
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.net.URI

interface Schema {

  val location: SchemaLocation

  val id: URI?

  val schemaURI: URI?

  val title: String?

  val description: String?

  val version: JsonSchemaVersion?

  val extraProperties: Map<String, JsrValue>

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

  fun toJson(version: JsonSchemaVersion = JsonSchemaVersion.latest,
             includeExtraProperties: Boolean = false): JsrObject

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

  operator fun plus(override: Schema):Schema = merge(JsonPath.rootPath, override, MergeReport())

  fun merge(path: JsonPath, override:Schema?, report:MergeReport): Schema

  fun toMutableSchema(): MutableSchema

  fun toMutableSchema(id: URI): MutableSchema

  @Name("asDraft3")
  fun asDraft3(): Draft3Schema

  @Name("asDraft4")
  fun asDraft4(): Draft4Schema

  @Name("asDraft6")
  fun asDraft6(): Draft6Schema

  @Name("asDraft7")
  fun asDraft7(): Draft7Schema

  fun toString(version: JsonSchemaVersion, includeExtraProperties: Boolean = false, indent:Boolean = false): String
  operator fun contains(keyword:KeywordInfo<*>):Boolean = keywords.contains(keyword)
}
