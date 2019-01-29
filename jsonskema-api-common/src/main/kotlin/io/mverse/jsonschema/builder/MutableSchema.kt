package io.mverse.jsonschema.builder

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.MutableKeywordContainer
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import lang.Name
import lang.collection.SetMultimap
import lang.json.JsonPath
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.JsrValue
import lang.net.URI

@Deprecated("Use MutableSchema", replaceWith = ReplaceWith("MutableSchema"))
typealias SchemaBuilder = MutableSchema

interface MutableSchema : MutableKeywordContainer {

  // ##################################################################
  // ########           COUPLE OF GETTERS                ##############
  // ##################################################################

  @Name("id")
  val id: URI?

  var metaSchema: URI?
  val location: SchemaLocation

  @Name("ref")
  var ref: Any?
  var refURI: URI?
  var refSchema: Schema?
  var title: String?
  var defaultValue: JsrValue?
  var description: String?
  var type: JsonSchemaType?
  var types: Set<JsonSchemaType>

  // ##################################################################
  // ########           STRING KEYWORDS                  ##############
  // ##################################################################

  var format: String?
  var pattern: String?
  var regex: Regex?
  var minLength: Int?
  var maxLength: Int?

  var additionalProperties: Boolean
  var comment: String?
  var readOnly: Boolean
  var writeOnly: Boolean
  var requiredProperties: Set<String>

  var contentEncoding: String?
  var contentMediaType: String?

  // ##################################################################
  // ########           METADATA KEYWORDS                ##############
  // ##################################################################

  var isUseSchemaKeyword: Boolean

  // ##################################################################
  // ########           OBJECT KEYWORDS                  ##############
  // ##################################################################

  var schemaOfAdditionalProperties: MutableSchema?
  var schemaDependencies: MutableSchemaDependencies
  var propertyDependencies: SetMultimap<String, String>
  var properties: MutableProperties

  var propertyNameSchema: MutableSchema?
  var patternProperties: MutableSchemaMap
  var definitions: MutableSchemaMap

  var minProperties: Int?
  var maxProperties: Int?

  // ##################################################################
  // ########           NUMBER KEYWORDS                  ##############
  // ##################################################################

  var multipleOf: Number?
  var exclusiveMinimum: Number?
  var minimum: Number?
  var exclusiveMaximum: Number?
  var maximum: Number?

  // ##################################################################
  // ########           ARRAY KEYWORDS                  ##############
  // ##################################################################

  var needsUniqueItems: Boolean
  var maxItems: Int?
  var minItems: Int?

  var schemaOfAdditionalItems: MutableSchema?
  var containsSchema: MutableSchema?

  var itemSchemas: List<MutableSchema>
  var allItemSchema: MutableSchema?

  // ##################################################################
  // ########           COMMON KEYWORDS                  ##############
  // ##################################################################

  var notSchema: MutableSchema?
  var enumValues: JsrArray?
  var const: Any?
  var constValue: JsrValue?

  var oneOfSchemas: List<MutableSchema>
  var anyOfSchemas: List<MutableSchema>
  var allOfSchemas: List<MutableSchema>
  var ifSchema: MutableSchema?
  var thenSchema: MutableSchema?
  var elseSchema: MutableSchema?

  operator fun invoke(block: MutableSchema.() -> Unit): MutableSchema

  fun itemSchema(block: MutableSchema.() -> Unit)
  fun oneOf(block: MutableSchema.() -> Unit)
  fun allOf(block: MutableSchema.() -> Unit)
  fun anyOf(block: MutableSchema.() -> Unit)
  fun allItemsSchema(block: MutableSchema.() -> Unit)
  fun containsSchema(block: MutableSchema.() -> Unit)
  fun ifSchema(block: MutableSchema.() -> Unit)
  fun notSchema(block: MutableSchema.() -> Unit)
  fun thenSchema(block: MutableSchema.() -> Unit)
  fun elseSchema(block: MutableSchema.() -> Unit)
  fun schemaOfAdditionalProperties(block: MutableSchema.() -> Unit)
  fun schemaOfAdditionalItems(block: MutableSchema.() -> Unit)

  // ##################################################################
  // ########           INNER KEYWORDS                   ##############
  // ##################################################################

  var extraProperties: MutableMap<String, JsrValue>

  @Name("buildWithLocation")
  fun build(itemsLocation: SchemaLocation? = null, report: LoadingReport): Schema

  fun build(block: MutableSchema.() -> Unit): Schema

  var loadingReport: LoadingReport
  val schemaLoader: SchemaLoader
  var currentDocument: JsrObject?

  fun buildSubSchema(toBuild: MutableSchema, keyword: KeywordInfo<*>, path: String, vararg paths: String): Schema
  fun subSchemaBuilder(keyword: KeywordInfo<*>, vararg child: String): MutableSchema

  @Name("build")
  fun build(): Schema

  operator fun contains(keyword: KeywordInfo<*>): Boolean

  fun merge(path: JsonPath, other: Schema, report: MergeReport)
  operator fun plusAssign(other: Schema) = merge(JsonPath.rootPath, other, MergeReport())

  /**
   * Returns a copy of this schema with the specified locatiion
   */
  fun withLocation(location: SchemaLocation): MutableSchema
}
