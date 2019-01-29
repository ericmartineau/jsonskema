package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.Draft3Keywords
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_ITEMS
import io.mverse.jsonschema.keyword.Keywords.ADDITIONAL_PROPERTIES
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.keyword.Keywords.ITEMS
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.utils.Schemas.nullSchema
import lang.collection.Multimaps
import lang.json.JsrArray
import lang.json.jsrArrayOf
import lang.net.URI

/**
 * This interface provides convenience methods for retrieving subschemas in the same version as the
 * current schema.  This is specified by the type parameter D
 *
 */
interface AllKeywords : Draft3Schema, Draft7Schema {

  override val metaSchemaURI: URI? get() = values[SCHEMA]

  override val notSchema get() = values[Keywords.NOT]
  override val constValue get() = values[Keywords.CONST]
  override val allOfSchemas get() = values[Keywords.ALL_OF] ?: emptyList()
  override val anyOfSchemas get() = values[Keywords.ANY_OF] ?: emptyList()
  override val oneOfSchemas get() = values[Keywords.ONE_OF] ?: emptyList()
  override val maxProperties get() = values[Keywords.MAX_PROPERTIES]?.toInt()
  override val minProperties get() = values[Keywords.MIN_PROPERTIES]?.toInt()
  override val requiredProperties get() = values[Keywords.REQUIRED] ?: emptySet()
  override val exclusiveMinimum get() = this[Keywords.MINIMUM]?.exclusiveLimit
  override val exclusiveMaximum get() = this[Keywords.MAXIMUM]?.exclusiveLimit
  override val containsSchema get() = values[Keywords.CONTAINS]
  override val propertyNameSchema get() = values[Keywords.PROPERTY_NAMES]

  override val title get() = values[Keywords.TITLE]
  override val description get() = values[Keywords.DESCRIPTION]
  override val types get() = values[Keywords.TYPE] ?: emptySet()
  override val enumValues get() = values[Keywords.ENUM]
  override val defaultValue get() = values[Keywords.DEFAULT]
  override val format get() = values[Keywords.FORMAT]
  override val minLength get() = values[Keywords.MIN_LENGTH]?.toInt()
  override val maxLength get() = values[Keywords.MAX_LENGTH]?.toInt()
  override val pattern get() = values[Keywords.PATTERN]
  override val minimum get() = this[Keywords.MINIMUM]?.limit
  override val maximum get() = this[Keywords.MAXIMUM]?.limit
  override val multipleOf get() = values[Keywords.MULTIPLE_OF]
  override val minItems get() = values[Keywords.MIN_ITEMS]?.toInt()
  override val maxItems get() = values[Keywords.MAX_ITEMS]?.toInt()
  override val properties get() = values[Keywords.PROPERTIES] ?: emptyMap()
  override val patternProperties get() = values[Keywords.PATTERN_PROPERTIES] ?: emptyMap()
  override val additionalPropertiesSchema get() = values[Keywords.ADDITIONAL_PROPERTIES]
  override val requiresUniqueItems get() = values[Keywords.UNIQUE_ITEMS] ?: false

  // ###################################
  // #### Draft 7 KEYWORDS #############
  // ###################################

  override val ifSchema: Schema? get() = values[Keywords.IF]
  override val elseSchema: Schema? get() = values[Keywords.ELSE]
  override val thenSchema: Schema? get() = values[Keywords.THEN]
  override val comment: String? get() = values[Keywords.COMMENT]
  override val isReadOnly: Boolean get() = values[Keywords.READ_ONLY] ?: false
  override val isWriteOnly: Boolean get() = values[Keywords.WRITE_ONLY] ?: false
  override val contentEncoding: String? get() = values[Keywords.CONTENT_ENCODING]
  override val contentMediaType: String? get() = values[Keywords.CONTENT_MEDIA_TYPE]

  override val examples: JsrArray get() = values[Keywords.EXAMPLES] ?: jsrArrayOf()
  override val definitions: Map<String, Schema> get() = values[Keywords.DEFINITIONS] ?: emptyMap()

  // #####################################################
  // #####  KEYWORDS ONLY USED BY Draft3 -> Draft 4   ####
  // #####################################################

  override val isAnyType:Boolean get() = types.isEmpty()
  override val disallow get() = keyword(Draft3Keywords.DISALLOW)?.disallowedTypes ?: emptySet()
  override val extendsSchema get() = values[Draft3Keywords.EXTENDS]
  override val isRequired get() = values[Draft3Keywords.REQUIRED_DRAFT3] ?: false
  override val divisibleBy get() = multipleOf

  // #####################################################
  // #####  KEYWORDS ONLY USED BY ??                  ####
  // #####################################################

  override val additionalItemsSchema get() = this[ITEMS]?.additionalItemSchema
  override val propertyDependencies
    get() = this[DEPENDENCIES]?.propertyDependencies ?: Multimaps.emptySetMultimap()
  override val propertySchemaDependencies get() = this[DEPENDENCIES]?.dependencySchemas?.value ?: emptyMap()
  override val allItemSchema: Schema? get() = this[Keywords.ITEMS]?.allItemSchema
  override val itemSchemas: List<Schema> get() = this[ITEMS]?.indexedSchemas ?: emptyList()
  override val isExclusiveMinimum get() = this[Keywords.MINIMUM]?.isExclusive ?: false
  override val isExclusiveMaximum: Boolean get() = this[Keywords.MAXIMUM]?.isExclusive ?: false

  override val isAllowAdditionalItems get() = this[ADDITIONAL_ITEMS]?.additionalItemSchema != nullSchema
  override val isAllowAdditionalProperties get() = values[ADDITIONAL_PROPERTIES] != nullSchema

  operator fun contains(keyword: KeywordInfo<*>): Boolean = keywords.contains(keyword)
}
