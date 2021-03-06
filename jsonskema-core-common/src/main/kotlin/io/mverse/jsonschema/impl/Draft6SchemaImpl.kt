package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import lang.collection.freezeMap
import lang.json.JsrArray
import lang.json.JsrValue
import lang.net.URI

class Draft6SchemaImpl : JsonSchemaImpl<Draft6Schema>, Draft6Schema {

  // #####################################################
  // #####  KEYWORDS for Draft6    #######################
  // #####################################################

  override val examples: JsrArray get() = super.examples
  override val definitions: Map<String, Schema> get() = super.definitions
  override val constValue: JsrValue? get() = super.constValue
  override val notSchema: Draft6Schema? get() = super.notSchema?.asDraft6()
  override val allOfSchemas: List<Schema> get() = super.allOfSchemas
  override val anyOfSchemas: List<Schema> get() = super.anyOfSchemas
  override val oneOfSchemas: List<Schema> get() = super.oneOfSchemas

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft4 -> Draft 6      ####
  // #####################################################

  override val maxProperties: Int? get() = super.maxProperties
  override val minProperties: Int? get() = super.minProperties
  override val requiredProperties: Set<String> get() = super.requiredProperties
  override val isAllowAdditionalItems: Boolean get() = super.isAllowAdditionalItems
  override val isAllowAdditionalProperties: Boolean get() = super.isAllowAdditionalProperties
  override val containsSchema: Draft6Schema? get() = super.containsSchema?.asDraft6()
  override val propertyNameSchema: Draft6Schema? get() = super.propertyNameSchema?.asDraft6()
  override val version: JsonSchemaVersion = JsonSchemaVersion.Draft6
  override val exclusiveMinimum: Number? get() = super.exclusiveMinimum
  override val exclusiveMaximum: Number? get() = super.exclusiveMaximum

  override val keywords: Map<KeywordInfo<*>, Keyword<*>>
    get() = super.keywords

  constructor(from: Schema) : super(from, JsonSchemaVersion.Draft6)

  constructor(location: SchemaLocation,
              keywords: Map<KeywordInfo<*>, Keyword<*>>,
              extraProperties: Map<String, JsrValue> = emptyMap()) : super(location, keywords.freezeMap(), extraProperties.freezeMap(), JsonSchemaVersion.Draft6)

  override fun asDraft6(): Draft6Schema = this
  override fun convertVersion(source: Schema): Draft6Schema = source.asDraft6()
  override fun withId(id: URI): Schema = Draft6SchemaImpl(location = location.withId(id),
      keywords = keywords.toMutableMap().also {
        it.remove(Keywords.ID)
        it[DOLLAR_ID] = IdKeyword(id)
      },
      extraProperties = extraProperties)
}
