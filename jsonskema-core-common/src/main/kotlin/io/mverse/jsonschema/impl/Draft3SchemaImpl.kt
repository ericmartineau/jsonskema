package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.keyword.Draft3Keywords
import io.mverse.jsonschema.keyword.Draft3Keywords.EXTENDS
import io.mverse.jsonschema.keyword.Draft3Keywords.REQUIRED_DRAFT3
import io.mverse.jsonschema.keyword.IdKeyword
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.ID
import io.mverse.jsonschema.keyword.Keywords.MAXIMUM
import io.mverse.jsonschema.keyword.Keywords.MINIMUM
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.utils.Schemas.nullSchema
import lang.collection.freezeMap
import lang.json.JsrValue
import lang.net.URI

class Draft3SchemaImpl : JsonSchemaImpl<Draft3Schema>, Draft3Schema {

  override val version: JsonSchemaVersion = Draft3

  // #####################################################
  // #####  KEYWORDS ONLY USED BY Draft3 -> Draft 4   ####
  // #####################################################

  override val isAnyType: Boolean get() = types.isEmpty()

  override val disallow: Set<JsonSchemaType>
    get() = keyword(Draft3Keywords.DISALLOW)?.disallowedTypes ?: emptySet()

  override val extendsSchema: Schema? get() = values[EXTENDS]
  override val isRequired: Boolean get() = values[REQUIRED_DRAFT3] ?: false
  override val divisibleBy: Number? get() = multipleOf

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
  // #####################################################

  override val isExclusiveMinimum: Boolean
    get() = this[MINIMUM]?.isExclusive ?: false

  override val isExclusiveMaximum: Boolean
    get() = this[MAXIMUM]?.isExclusive ?: false

  override val isAllowAdditionalItems: Boolean
    get() = additionalItemsSchema != nullSchema

  override val isAllowAdditionalProperties: Boolean
    get() = additionalPropertiesSchema != nullSchema

  constructor(schemaLoader: SchemaLoader, from: Schema) : super(schemaLoader, from.location, from.keywords.freezeMap(), from.extraProperties.freezeMap(), Draft3)

  constructor(schemaLoader: SchemaLoader, location: SchemaLocation,
              keywords: Map<KeywordInfo<*>, Keyword<*>>,
              extraProperties: Map<String, JsrValue>) : super(schemaLoader, location, keywords, extraProperties, Draft3)

  override fun asDraft3(): Draft3Schema = this
  override fun convertVersion(source: Schema): Draft3Schema = source.asDraft3()

  override fun withId(id: URI): Schema {
    return Draft3SchemaImpl(schemaLoader=schemaLoader,
        location = location.withId(id),
        keywords = keywords.toMutableMap().also {
          it.remove(DOLLAR_ID)
          it[ID] = IdKeyword(id)
        },
        extraProperties = extraProperties)
  }
}
