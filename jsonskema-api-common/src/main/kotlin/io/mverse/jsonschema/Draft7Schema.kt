package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.SetMultimap
import lang.URI

interface Draft7Schema : DraftSchema<Draft7Schema> {

  override val version: JsonSchemaVersion
    get() = JsonSchemaVersion.Draft7

  override val location: SchemaLocation

  // ###################################
  // #### Meta KEYWORDS ##############
  // ###################################

  override val schemaURI: URI?

  override val id: URI?

  override val title: String?

  override val description: String?

  val examples: kotlinx.serialization.json.JsonArray

  val definitions: Map<String, Schema>

  // ###################################
  // #### Draft 7 KEYWORDS #############
  // ###################################

  val ifSchema: Schema?

  val elseSchema: Schema?

  val thenSchema: Schema?

  val comment: String?

  val isReadOnly: Boolean

  val isWriteOnly: Boolean

  // ###################################
  // #### Shared KEYWORDS ##############
  // ###################################

  override val types: Set<JsonSchemaType>

  override val enumValues: kotlinx.serialization.json.JsonArray?

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

  override val allItemSchema: Draft7Schema?

  override val itemSchemas: List<Schema>

  override val additionalItemsSchema: Draft7Schema?

  val containsSchema: Draft7Schema?

  // ###################################
  // #### OBJECT KEYWORDS  ##############
  // ###################################

  override val properties: Map<String, Schema>

  override val patternProperties: Map<String, Schema>

  override val additionalPropertiesSchema: Draft7Schema?

  val propertyNameSchema: Draft7Schema?

  override val propertyDependencies: SetMultimap<String, String>
  override val propertySchemaDependencies: Map<String, Schema>
  override val requiresUniqueItems: Boolean

  val maxProperties: Int?

  val minProperties: Int?

  val requiredProperties: Set<String>



  override fun asDraft7(): Draft7Schema {
    return this
  }
}
