package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SubschemaKeyword
import io.mverse.jsonschema.utils.calculateMergeURI
import lang.Name
import lang.exception.illegalState
import lang.json.JsonPath
import lang.json.JsrObject
import lang.json.JsrValue
import lang.net.URI

interface Schema {
  val id: URI?
  val location: SchemaLocation

  var parent: Schema?

  val version: JsonSchemaVersion

  val extraProperties: Map<String, JsrValue>

  val keywords: Map<KeywordInfo<*>, Keyword<*>>

  val absoluteURI: URI
    get() = location.uniqueURI

  val pointerFragmentURI: URI?
    get() = location.jsonPointerFragment


  operator fun plus(override: Schema): Schema = merge(JsonPath.rootPath, override, MergeReport())

  fun merge(path: JsonPath, override: Schema?, report: MergeReport,
            mergedId: URI? = absoluteURI.calculateMergeURI(override?.absoluteURI)): Schema

  @Deprecated("Use toMutableSchema()", replaceWith = ReplaceWith("toMutableSchema()"))
  fun toBuilder(): MutableSchema = toMutableSchema()

  fun toMutableSchema(): MutableSchema

  @Deprecated("Use toMutableSchema()", replaceWith = ReplaceWith("toMutableSchema(id)"))
  fun toBuilder(id: URI): MutableSchema = toMutableSchema(id)

  fun toMutableSchema(id: URI): MutableSchema

  fun withVersion(version:JsonSchemaVersion):Schema

  @Name("asDraft3")
  fun asDraft3(): Draft3Schema = JsonSchema.draftSchema(this)

  @Name("asDraft4")
  fun asDraft4(): Draft4Schema = JsonSchema.draftSchema(this)

  @Name("asDraft6")
  fun asDraft6(): Draft6Schema = JsonSchema.draftSchema(this)

  @Name("asDraft7")
  fun asDraft7(): Draft7Schema = JsonSchema.draftSchema(this)

  fun asVersion(version: JsonSchemaVersion): DraftSchema {
    return when (version) {
      JsonSchemaVersion.Draft7 -> asDraft7()
      JsonSchemaVersion.Draft6 -> asDraft6()
      JsonSchemaVersion.Draft4 -> asDraft4()
      JsonSchemaVersion.Draft3 -> asDraft3()
      else -> illegalState("Cant convert to custom")
    }
  }

  fun relocate() {
    keywords.values
        .mapNotNull { it as? SubschemaKeyword }
        .map { it.subschemas }
        .flatten()
        .forEach { it.parent = this }
  }

  fun toJson(version: JsonSchemaVersion): JsrObject

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

inline fun <reified D : DraftSchema> Schema.asVersion(): D {
  return when (D::class) {
    Draft3Schema::class -> asDraft3() as D
    Draft4Schema::class -> asDraft4() as D
    Draft6Schema::class -> asDraft6() as D
    Draft7Schema::class -> asDraft7() as D
    else -> throw IllegalArgumentException("Unable to determine version from: $version")
  }
}

