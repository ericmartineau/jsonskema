package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.Pattern
import lang.URI

interface SchemaBuilder<SELF : SchemaBuilder<SELF>> {

  // ##################################################################
  // ########           COUPLE OF GETTERS                ##############
  // ##################################################################

  val id: URI?
  var ref: URI?

  // ##################################################################
  // ########           METADATA KEYWORDS                ##############
  // ##################################################################

  fun withSchema(): SELF

  fun withoutSchema(): SELF

  fun ref(ref: URI): SELF

  fun title(title: String): SELF

  fun defaultValue(defaultValue: JsonElement): SELF

  fun description(description: String): SELF

  fun type(requiredType: JsonSchemaType): SELF

  fun orType(requiredType: JsonSchemaType): SELF

  fun types(requiredTypes: Set<JsonSchemaType>?): SELF

  fun comment(comment: String): SELF

  fun clearTypes(): SELF

  fun readOnly(value: Boolean = true): SELF

  fun writeOnly(value: Boolean = true): SELF

  // ##################################################################
  // ########           STRING KEYWORDS                  ##############
  // ##################################################################

  fun pattern(pattern: String): SELF

  fun pattern(pattern: Pattern): SELF

  fun minLength(minLength: Int): SELF

  fun maxLength(maxLength: Int): SELF

  fun format(format: String): SELF

  // ##################################################################
  // ########           OBJECT KEYWORDS                  ##############
  // ##################################################################

  fun schemaOfAdditionalProperties(schemaOfAdditionalProperties: SchemaBuilder<*>): SELF

  fun schemaDependency(property: String, dependency: SchemaBuilder<*>): SELF

  fun propertyDependency(ifPresent: String, thenRequireThisProperty: String): SELF

  fun requiredProperty(requiredProperty: String): SELF

  fun propertySchema(propertySchemaKey: String, propertySchemaValue: SchemaBuilder<*>): SELF

  fun updatePropertySchema(propertyName: String, updater: (SchemaBuilder<*>)->SchemaBuilder<*>): SELF

  fun propertyNameSchema(propertyNameSchema: SELF): SELF

  fun patternProperty(pattern: String, schema: SchemaBuilder<*>): SELF

  fun patternProperty(pattern: Pattern, schema: SchemaBuilder<*>): SELF

  fun minProperties(minProperties: Int): SELF

  fun maxProperties(maxProperties: Int): SELF

  fun clearRequiredProperties(): SELF

  fun clearPropertySchemas(): SELF

  // ##################################################################
  // ########           NUMBER KEYWORDS                  ##############
  // ##################################################################

  fun multipleOf(multipleOf: Number): SELF

  fun exclusiveMinimum(exclusiveMinimum: Number): SELF

  fun minimum(minimum: Number): SELF

  fun exclusiveMaximum(exclusiveMaximum: Number): SELF

  fun maximum(maximum: Number): SELF

  // ##################################################################
  // ########           ARRAY KEYWORDS                  ##############
  // ##################################################################

  fun needsUniqueItems(needsUniqueItems: Boolean): SELF

  fun maxItems(maxItems: Int): SELF

  fun minItems(minItems: Int): SELF

  fun containsSchema(containsSchema: SchemaBuilder<*>): SELF

  fun noAdditionalItems(): SELF

  fun schemaOfAdditionalItems(schemaOfAdditionalItems: SchemaBuilder<*>): SELF

  fun itemSchemas(itemSchemas: List<SchemaBuilder<*>>): SELF

  fun itemSchema(itemSchema: SchemaBuilder<*>): SELF

  fun allItemSchema(allItemSchema: SchemaBuilder<*>): SELF

  // ##################################################################
  // ########           COMMON KEYWORDS                  ##############
  // ##################################################################

  fun notSchema(notSchema: SchemaBuilder<*>): SELF

  fun enumValues(enumValues: JsonArray): SELF

  fun constValueString(constValue: String): SELF

  fun constValueInt(constValue: Int): SELF

  fun constValueDouble(constValue: Double): SELF

  fun constValue(constValue: JsonElement): SELF

  fun oneOfSchemas(oneOfSchemas: Collection<SchemaBuilder<*>>): SELF

  fun oneOfSchema(oneOfSchema: SchemaBuilder<*>): SELF

  fun anyOfSchemas(anyOfSchemas: Collection<SchemaBuilder<*>>): SELF

  fun anyOfSchema(anyOfSchema: SchemaBuilder<*>): SELF

  fun allOfSchemas(allOfSchemas: Collection<SchemaBuilder<*>>): SELF

  fun allOfSchema(allOfSchema: SchemaBuilder<*>): SELF

  fun ifSchema(ifSchema: SchemaBuilder<*>): SELF

  fun thenSchema(thenSchema: SchemaBuilder<*>): SELF

  fun elseSchema(elseSchema: SchemaBuilder<*>): SELF

  // ##################################################################
  // ########           INNER KEYWORDS                   ##############
  // ##################################################################

  /**
   * Adds a keyword directly to the builder - this bypasses any convenience methods, and can be used for loading custom
   * keywords.
   * @return reference to self
   */
//  fun <K : JsonSchemaKeyword<*>> keyword(keyword: KeywordInfo<K>, keywordValue: K): SELF
//
//  fun <X : JsonSchemaKeyword<*>> getKeyword(keyword: KeywordInfo<X>): X?

  fun extraProperty(propertyName: String, JsonElement: JsonElement): SELF

  fun build(itemsLocation: SchemaLocation? = null, report: LoadingReport): Schema

  fun build(): Schema

  fun withLoadingReport(report: LoadingReport): SELF

  fun withSchemaLoader(factory: SchemaLoader): SELF

  fun withCurrentDocument(currentDocument: JsonObject): SELF
}
