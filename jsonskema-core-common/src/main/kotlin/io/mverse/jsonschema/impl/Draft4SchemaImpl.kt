package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft4
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.keyword.Keywords.MAXIMUM
import io.mverse.jsonschema.keyword.Keywords.MINIMUM
import lang.collection.freezeMap
import lang.json.JsrValue
import lang.net.URI

class Draft4SchemaImpl : JsonSchemaImpl<Draft4Schema>, Draft4Schema {

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
  // #####################################################

  override val notSchema: Draft4Schema? get() = super.notSchema?.asDraft4()
  override val allOfSchemas: List<Schema> get() = super.allOfSchemas
  override val anyOfSchemas: List<Schema> get() = super.anyOfSchemas
  override val oneOfSchemas: List<Schema> get() = super.oneOfSchemas

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft4 -> Draft 6      ####
  // #####################################################

  override val isExclusiveMinimum: Boolean by lazy { keyword(MINIMUM)?.isExclusive ?: false }
  override val isExclusiveMaximum: Boolean by lazy { keyword(MAXIMUM)?.isExclusive ?: false }
  override val maxProperties: Int? get() = super.maxProperties
  override val minProperties: Int? get() = super.minProperties
  override val requiredProperties: Set<String> get() = super.requiredProperties
  override val isAllowAdditionalItems: Boolean get() = super.isAllowAdditionalItems
  override val isAllowAdditionalProperties: Boolean get() = super.isAllowAdditionalProperties

  constructor(from: Schema) : super(from, Draft4)
  constructor(location: SchemaLocation,
              keywords: Map<KeywordInfo<*>, Keyword<*>>,
              extraProperties: Map<String, JsrValue>) : super(location, keywords.freezeMap(), extraProperties.freezeMap(), Draft4)

  override val version: JsonSchemaVersion = Draft4
  override fun asDraft4(): Draft4Schema = this
  override fun convertVersion(source: Schema): Draft4Schema = source.asDraft4()
  override fun withId(id: URI): Schema = Draft4SchemaImpl(location = location.withId(id),
      keywords = keywords.toMutableMap().also {
        it.remove(ID)
        it[DOLLAR_ID] = IdKeyword(id)
      },
      extraProperties = extraProperties)
}
