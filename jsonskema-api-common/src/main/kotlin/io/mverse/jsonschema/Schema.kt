package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keywords.REF
import io.mverse.jsonschema.utils.calculateMergeURI
import lang.Name
import lang.exception.illegalState
import lang.exception.nullPointer
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.net.URI
import lang.suppress.Suppressions
import lang.suppress.Suppressions.Companion.UNCHECKED_CAST

/**
 * A json-schema instance.
 */
interface Schema: KeywordContainer {

  /**
   * The $id keyword, if specified
   */
  val id: URI?

  /**
   * The location for this schema/subschema
   */
  val location: SchemaLocation

  /**
   * The version represented by this schema
   */
  val version: JsonSchemaVersion

  /**
   * Any properties that weren't part of the json-schema spec
   */
  val extraProperties: Map<String, JsrValue>

  /**
   * An absolute URI location.  If none was provided/parsed from the schema, a unique UUID-based
   * URI will be generated
   */
  val absoluteURI: URI get() = location.uniqueURI

  val pointerFragmentURI: URI? get() = location.jsonPointerFragment

  val isRefSchema: Boolean get() = this is RefSchema || values[REF] != null

  /**
   * Combines this schema with another schema by merging the keywords, a new copy is returned
   */
  operator fun plus(override: Schema): Schema = merge(JsonPath.rootPath, override, MergeReport())

  /**
   * Merged this schema with another this schema, a copy is returned.
   */
  fun merge(path: JsonPath, override: Schema?, report: MergeReport,
            mergedId: URI? = absoluteURI.calculateMergeURI(override?.absoluteURI)): Schema

  @Deprecated("Use toMutableSchema()", replaceWith = ReplaceWith("toMutableSchema()"))
  fun toBuilder(): MutableSchema = toMutableSchema()

  fun toMutableSchema(): MutableSchema

  @Deprecated("Use toMutableSchema()", replaceWith = ReplaceWith("toMutableSchema(id)"))
  fun toBuilder(id: URI): MutableSchema = toMutableSchema(id)

  fun toMutableSchema(id: URI): MutableSchema

  @Name("asDraft3")
  fun asDraft3(): Draft3Schema

  @Name("asDraft4")
  fun asDraft4(): Draft4Schema

  @Name("asDraft6")
  fun asDraft6(): Draft6Schema

  @Name("asDraft7")
  fun asDraft7(): Draft7Schema

  fun <D:Schema> asVersion(version: JsonSchemaVersion): D {
    @Suppress(UNCHECKED_CAST)
    return when (version) {
      JsonSchemaVersion.Draft7 -> asDraft7()
      JsonSchemaVersion.Draft6 -> asDraft6()
      JsonSchemaVersion.Draft4 -> asDraft4()
      JsonSchemaVersion.Draft3 -> asDraft3()
      else -> illegalState("Cant convert to custom")
    } as D
  }

  operator fun get(path:JsonPath): Schema = getOrNull(path) ?: nullPointer("Unable to resolve schema for path $path")

  fun getOrNull(path:JsonPath): Schema?

  fun toJson(includeExtraProperties:Boolean): JsrObject
  fun toString(includeExtraProperties:Boolean, indent:Boolean = true): String

  /**
   * Creates a copy of this schema with the provided schema id
   */
  @Name("withId")
  fun withId(id: URI): Schema

  /**
   * Creates a copy of this schema with the provided document URI
   */
  @Name("withDocumentURI")
  fun withDocumentURI(documentURI: URI): Schema

}

inline fun <reified D : Schema> Schema.asVersion(): D {
  return when (D::class) {
    Draft3Schema::class -> asDraft3() as D
    Draft4Schema::class -> asDraft4() as D
    Draft6Schema::class -> asDraft6() as D
    Draft7Schema::class -> asDraft7() as D
    else -> throw IllegalArgumentException("Unable to determine version from: $version")
  }
}

