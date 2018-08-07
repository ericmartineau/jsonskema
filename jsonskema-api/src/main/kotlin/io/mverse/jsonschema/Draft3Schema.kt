package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.SetMultimap
import lang.URI

interface Draft3Schema : DraftSchema<Draft3Schema> {

  override val location: SchemaLocation

  // ###################################
  // #### Meta KEYWORDS ##############
  // ###################################

  override val schemaURI: URI?

  override val id: URI?

  override val title: String?

  override val description: String?

  // ###################################
  // #### Shared KEYWORDS ##############
  // ###################################

  override val types: Set<JsonSchemaType>

  val isAnyType: Boolean

  val disallow: Set<JsonSchemaType>

  val extendsSchema: Schema?

  val isRequired: Boolean

  override val enumValues: JsonArray?

  override val defaultValue: JsonElement?

  // ###################################
  // #### String KEYWORDS ##############
  // ###################################

  override val format: String?

  override val minLength: Int?

  override val maxLength: Int?

  override val pattern: String?

  // ###################################
  // #### NUMBER KEYWORDS ##############
  // ###################################
  val divisibleBy: Number?

  override val maximum: Number?

  override val minimum: Number?

  val isExclusiveMinimum: Boolean?

  val isExclusiveMaximum: Boolean?

  // ###################################
  // #### ARRAY KEYWORDS  ##############
  // ###################################

  override val minItems: Int?

  override val maxItems: Int?

  override val allItemSchema: Draft3Schema?

  override val itemSchemas: List<Schema>

  val isAllowAdditionalItems: Boolean

  override val additionalItemsSchema: Draft3Schema?

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  override val properties: Map<String, Schema>

  override val patternProperties: Map<String, Schema>

  val isAllowAdditionalProperties: Boolean

  override val additionalPropertiesSchema: Draft3Schema?

  override val propertyDependencies: SetMultimap<String, String>

  override val propertySchemaDependencies: Map<String, Schema>

  override val version: JsonSchemaVersion
    get() = JsonSchemaVersion.Draft3

  override val requiresUniqueItems: Boolean

  override fun asDraft3(): Draft3Schema = this
}
