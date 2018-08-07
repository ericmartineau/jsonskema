package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import lang.SetMultimap
import lang.convert

/**
 * This interface provides convenience methods for retrieving subschemas in the same version as the
 * current schema.  This is specified by the type parameter D
 *
 * @param <SELF> The version of the schema that should be returned.
</SELF> */
interface DraftSchema<SELF : DraftSchema<SELF>> : Schema {

  val types: Set<JsonSchemaType>

  val enumValues: JsonArray?

  val defaultValue: JsonElement?

  val format: String?

  val minLength: Int?

  val maxLength: Int?

  val pattern: String?

  val maximum: Number?

  val minimum: Number?

  val multipleOf: Number?

  val minItems: Int?

  val maxItems: Int?

  val allItemSchema: SELF?

  val itemSchemas: List<Schema>

  val additionalItemsSchema: SELF?

  val properties: Map<String, Schema>

  val patternProperties: Map<String, Schema>

  val additionalPropertiesSchema: SELF?

  val propertyDependencies: SetMultimap<String, String>

  val propertySchemaDependencies: Map<String, Schema>

  val requiresUniqueItems: Boolean

  fun convertVersion(source: Schema): SELF

  fun findPropertySchema(schemaName: String): SELF? {
    val found = properties[schemaName]
    return found?.convert { convertVersion(it) }
  }

  fun findPatternSchema(pattern: String): SELF? {
    val found = patternProperties[pattern]
    return found.convert { convertVersion(it) }
  }

  fun getPropertySchema(property: String): SELF {
    return findPropertySchema(property) ?: missingProperty(this, property)
  }

  fun getPatternSchema(pattern: String): SELF {
    return findPatternSchema(pattern) ?: missingProperty(this, pattern)
  }
}
