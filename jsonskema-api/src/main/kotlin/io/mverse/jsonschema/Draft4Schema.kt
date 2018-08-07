package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.SetMultimap
import lang.URI

interface Draft4Schema : DraftSchema<Draft4Schema> {

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

  override val enumValues: JsonArray?

  override val defaultValue: JsonElement?

  val notSchema: Draft4Schema?

  val allOfSchemas: List<Schema>

  val anyOfSchemas: List<Schema>

  val oneOfSchemas: List<Schema>

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
  override val multipleOf: Number?

  override val maximum: Number?

  override val minimum: Number?

  val isExclusiveMinimum: Boolean?

  val isExclusiveMaximum: Boolean?

  // ###################################
  // #### ARRAY KEYWORDS  ##############
  // ###################################

  override val minItems: Int?

  override val maxItems: Int?

  override val allItemSchema: Draft4Schema?

  override val itemSchemas: List<Schema>

  val isAllowAdditionalItems: Boolean

  override val additionalItemsSchema: Draft4Schema?

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  val isAllowAdditionalProperties: Boolean

  override val additionalPropertiesSchema: Draft4Schema?

  override val propertyDependencies: SetMultimap<String, String>

  override val propertySchemaDependencies: Map<String, Schema>

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>

  override val version: JsonSchemaVersion
    get() = JsonSchemaVersion.Draft4

  override val requiresUniqueItems: Boolean

  override fun asDraft4(): Draft4Schema {
    return this
  }
}
