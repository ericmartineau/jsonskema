package io.mverse.jsonschema.impl

import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft3
import io.mverse.jsonschema.keyword.Draft3Keywords
import io.mverse.jsonschema.keyword.Draft3Keywords.Companion.EXTENDS
import io.mverse.jsonschema.keyword.Draft3Keywords.Companion.REQUIRED_DRAFT3
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.utils.Schemas.nullSchema
import kotlinx.serialization.json.JsonElement
import lang.URI
import lang.freezeMap

class Draft3SchemaImpl : JsonSchemaImpl<Draft3Schema>, Draft3Schema {

  override val version: JsonSchemaVersion = Draft3

  // #####################################################
  // #####  KEYWORDS ONLY USED BY Draft3 -> Draft 4   ####
  // #####################################################

  override val isAnyType: Boolean get() = types.isEmpty()

  override val disallow: Set<JsonSchemaType>
    get() = keyword(Draft3Keywords.DISALLOW)?.disallowedTypes ?: emptySet()

  override val extendsSchema: Schema? by keywords(EXTENDS)
  override val isRequired: Boolean by keywords(REQUIRED_DRAFT3, false)
  override val divisibleBy: Number? get() = multipleOf

  // #####################################################
  // #####  KEYWORDS SHARED BY Draft3 -> Draft 4      ####
  // #####################################################

  override val isExclusiveMinimum: Boolean
    get() {
      return keyword(Keywords.MINIMUM)?.isExclusive ?: false
    }

  override val isExclusiveMaximum: Boolean
    get() {
      return keyword(Keywords.MAXIMUM)?.isExclusive ?: false
    }

  override val isAllowAdditionalItems: Boolean
    get() = additionalItemsSchema != nullSchema

  override val isAllowAdditionalProperties: Boolean
    get() = additionalPropertiesSchema != nullSchema

  constructor(from: Schema) : super(from.location, from.keywords.freezeMap(), from.extraProperties.freezeMap(), Draft3) {}

  constructor(location: SchemaLocation,
              keywords: Map<KeywordInfo<*>, JsonSchemaKeyword<*>>,
              extraProperties: Map<String, JsonElement>) : super(location, keywords, extraProperties, Draft3)

  override fun asDraft3(): Draft3Schema = this
  override fun convertVersion(source: Schema): Draft3Schema = source.asDraft3()

  override fun withId(id: URI): Schema {
    return Draft3SchemaImpl(location.withId(id), keywords, extraProperties)
  }
}
