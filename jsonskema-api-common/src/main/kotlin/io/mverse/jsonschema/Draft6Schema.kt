package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.SetMultimap
import lang.URI

interface Draft6Schema : DraftSchema<Draft6Schema> {

  override val version: JsonSchemaVersion
    get() = JsonSchemaVersion.Draft6

  override val location: SchemaLocation

  // ###################################
  // #### Meta KEYWORDS ##############
  // ###################################

  override val schemaURI: URI?

  override val id: URI?

  override val title: String?

  override val description: String?

  val examples: JsonArray

  val definitions: Map<String, Schema>

  // ###################################
  // #### Shared KEYWORDS ##############
  // ###################################

  override val types: Set<JsonSchemaType>

  override val enumValues: JsonArray?

  override val defaultValue: JsonElement?

  val notSchema: Schema?

  val constValue: JsonElement?

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

  val exclusiveMinimum: Number?

  val exclusiveMaximum: Number?

  // ###################################
  // #### ARRAY KEYWORDS  ##############
  // ###################################

  override val minItems: Int?

  override val maxItems: Int?

  override val allItemSchema: Draft6Schema?

  override val itemSchemas: List<Schema>

  override val additionalItemsSchema: Draft6Schema?

  val containsSchema: Draft6Schema?

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  override val properties: Map<String, Schema>

  override val patternProperties: Map<String, Schema>

  override val additionalPropertiesSchema: Draft6Schema?

  val propertyNameSchema: Draft6Schema?

  override val propertyDependencies: SetMultimap<String, String>

  override val propertySchemaDependencies: Map<String, Schema>

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>

  override val requiresUniqueItems: Boolean

  override fun asDraft6(): Draft6Schema {
    return this
  }
}
