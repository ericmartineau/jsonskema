package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ELSE
import io.mverse.jsonschema.keyword.Keywords.IF
import io.mverse.jsonschema.keyword.Keywords.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.THEN
import io.mverse.jsonschema.keyword.Keywords.WRITE_ONLY
import lang.collection.freezeMap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI

class Draft7SchemaImpl : JsonSchemaImpl<Draft7Schema>, Draft7Schema {

  // #####################################################
  // #####  KEYWORDS for Draft7    #######################
  // #####################################################

  override val ifSchema: Schema? by keywords(IF)

  override val elseSchema: Schema? by keywords(ELSE)
  override val thenSchema: Schema? by keywords(THEN)
  override val comment: String? by keywords(COMMENT)

  override val isReadOnly: Boolean by keywords(READ_ONLY, false)
  override val isWriteOnly: Boolean by keywords(WRITE_ONLY, false)
  override val contentEncoding: String? by keywords(CONTENT_ENCODING)
  override val contentMediaType: String? by keywords(CONTENT_MEDIA_TYPE)

  // #####################################################
  // #####  KEYWORDS for Draft6    #######################
  // #####################################################

  override val examples: JsrArray get() = super.examples
  override val definitions: Map<String, Schema> get() = super.definitions

  override val notSchema: Schema? get() = super.notSchema
  override val constValue: JsrValue? get() = super.constValue
  override val allOfSchemas: List<Schema> get() = super.allOfSchemas
  override val anyOfSchemas: List<Schema> get() = super.anyOfSchemas
  override val oneOfSchemas: List<Schema> get() = super.oneOfSchemas

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft4 -> Draft 6      ####
  // #####################################################

  override val multipleOf: Number? get() = super.multipleOf
  override val exclusiveMinimum: Number? get() = super.exclusiveMinimum
  override val exclusiveMaximum: Number? get() = super.exclusiveMaximum
  override val containsSchema: Draft7Schema? get() = super.containsSchema?.asDraft7()
  override val propertyNameSchema: Draft7Schema? get() = super.propertyNameSchema?.asDraft7()
  override val maxProperties: Int? get() = super.maxProperties
  override val minProperties: Int? get() = super.minProperties
  override val requiredProperties: Set<String> get() = super.requiredProperties

  constructor(from: Schema) : super(from, Draft7) {}

  constructor(location: SchemaLocation,
              keywords: Map<KeywordInfo<*>, Keyword<*>>,
              extraProperties: Map<String, JsrValue>) : super(location, keywords.freezeMap(), extraProperties.freezeMap(), Draft7)

  override val version: JsonSchemaVersion = Draft7
  override fun asDraft7(): Draft7Schema = this
  override fun convertVersion(source: Schema): Draft7Schema = source.asDraft7()
  override fun withId(id: URI): Schema {
    return Draft7SchemaImpl(
        location = location.withId(id),
        keywords = keywords.toMutableMap().also {
          it.remove(Keywords.ID)
          it[DOLLAR_ID] = IdKeyword(id)
        },
        extraProperties = extraProperties)
  }
}
