package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.JsrIterable
import lang.collection.SetMultimap
import lang.json.JsrValue
import lang.net.URI

interface DraftSharedKeywords : Schema {
  @Deprecated("Use metaSchemaURI", replaceWith = ReplaceWith("metaSchemaURI"))
  val schemaURI: URI?
    get() = metaSchemaURI

  val metaSchemaURI: URI?
  val title: String?
  val description: String?
  val types: Set<JsonSchemaType>
  val enumValues: JsrIterable?
  val defaultValue: JsrValue?
  val format: String?
  val minLength: Int?
  val maxLength: Int?
  val pattern: String?
  val minimum: Number?
  val maximum: Number?
  val multipleOf: Number?
  val minItems: Int?
  val maxItems: Int?
  val properties: Map<String, Schema>
  val patternProperties: Map<String, Schema>
  val additionalPropertiesSchema: Schema?
  val requiresUniqueItems: Boolean
  val isAllowAdditionalItems: Boolean
  val isAllowAdditionalProperties: Boolean

  val allItemSchema: Schema?
  val itemSchemas: List<Schema>
  val additionalItemsSchema: Schema?
  val propertyDependencies: SetMultimap<String, String>
  val propertySchemaDependencies: Map<String, Schema>
}
