package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft7Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.*
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.Companion.COMMENT
import io.mverse.jsonschema.keyword.Keywords.Companion.ELSE
import io.mverse.jsonschema.keyword.Keywords.Companion.IF
import io.mverse.jsonschema.keyword.Keywords.Companion.READ_ONLY
import io.mverse.jsonschema.keyword.Keywords.Companion.THEN
import io.mverse.jsonschema.keyword.Keywords.Companion.WRITE_ONLY
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.URI

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

  // #####################################################
  // #####  KEYWORDS for Draft6    #######################
  // #####################################################

  override val examples: kotlinx.serialization.json.JsonArray get() = super.examples
  override val definitions: Map<String, Schema> get() = super.definitions

  override val notSchema: Schema? get() = super.notSchema
  override val constValue: JsonElement? get() = super.constValue
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
              keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>>,
              extraProperties: Map<String, JsonElement>) : super(location, keywords, extraProperties, Draft7)

  override val version: JsonSchemaVersion = Draft7
  override fun asDraft7(): Draft7Schema = this
  override fun convertVersion(source: Schema): Draft7Schema = source.asDraft7()
  override fun withId(id: URI): Schema = Draft7SchemaImpl(location.withId(id), keywords, extraProperties)
}
