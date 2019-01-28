package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.utils.calculateJsonSchemaType
import lang.collection.SetMultimap
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.JsrValue
import lang.net.URI

/**
 * This interface provides convenience methods for retrieving subschemas in the same version as the
 * current schema.  This is specified by the type parameter D
 *
 * @param <SELF> The version of the schema that should be returned.
</SELF> */
interface DraftSchema {
  val schema: Schema
  val keywords: Map<KeywordInfo<*>, Keyword<*>> get() = schema.keywords
  val location: SchemaLocation get() = schema.location
  val id: URI?

  val schemaURI: URI?

  val title: String?

  val description: String?

  val version: JsonSchemaVersion?

  val extraProperties: Map<String, JsrValue> get() = schema.extraProperties

  val types: Set<JsonSchemaType>

  val enumValues: JsrArray?

  val defaultValue: JsrValue?

  val format: String?

  val minLength: Int?

  val maxLength: Int?

  val pattern: String?

  val maximum: Number?

  val minimum: Number?

  val multipleOf: Number?

  val minItems: Int?

  val maxItems: Int?

  val allItemSchema: DraftSchema?

  val itemSchemas: List<DraftSchema>

  val additionalItemsSchema: DraftSchema?

  val properties: Map<String, DraftSchema>

  val patternProperties: Map<String, DraftSchema>

  val additionalPropertiesSchema: DraftSchema?

  val propertyDependencies: SetMultimap<String, String>

  val propertySchemaDependencies: Map<String, DraftSchema>

  val requiresUniqueItems: Boolean

  fun asDraft7(): Draft7Schema
  fun asDraft6(): Draft6Schema
  fun toDraft4(): Draft4Schema
  fun toDraft3(): Draft3Schema
//  fun findPropertySchema(schemaName: String): SELF? {
//    val found = properties[schemaName]
//    return found?.let { convertVersion(it) }
//  }
//
//  fun findPatternSchema(pattern: String): SELF? {
//    val found = patternProperties[pattern]
//    return found?.let { convertVersion(it) }
//  }
//
//  fun getPropertySchema(property: String): SELF {
//    return findPropertySchema(property) ?: missingProperty(this, property)
//  }
//
//  fun getPatternSchema(pattern: String): SELF {
//    return findPatternSchema(pattern) ?: missingProperty(this, pattern)
//  }

  fun toJson(includeExtraProperties: Boolean = false): JsrObject

  fun toString(includeExtraProperties: Boolean = false, indent:Boolean = false): String
  operator fun contains(keyword: KeywordInfo<*>):Boolean = keywords.contains(keyword)
  fun calculateJsonSchemaType(): JsonSchemaType? = schema.calculateJsonSchemaType()
  val isRefSchema: Boolean
}
