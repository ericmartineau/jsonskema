package io.mverse.jsonschema

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import lang.Name
import lang.Pattern
import lang.SetMultimap
import lang.URI

interface SchemaBuilder {

  // ##################################################################
  // ########           COUPLE OF GETTERS                ##############
  // ##################################################################

  @Name("id")
  val id: URI?

  @Name("ref")
  var ref: Any?
  var refURI: URI?
  var title: String?
  var defaultValue: JsonElement?
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

  var isUseSchemaKeyword:Boolean

  // ##################################################################
  // ########           OBJECT KEYWORDS                  ##############
  // ##################################################################

  var schemaOfAdditionalProperties:SchemaBuilder?
  var schemaDependencies: Map<String, SchemaBuilder>
  var propertyDependencies: SetMultimap<String, String>
  var propertySchemas: Map<String, SchemaBuilder>

  var propertyNameSchema:SchemaBuilder?
  var patternProperties:Map<String, SchemaBuilder>

  var minProperties:Int?
  var maxProperties:Int?

  // ##################################################################
  // ########           NUMBER KEYWORDS                  ##############
  // ##################################################################


  var multipleOf:Number?
  var exclusiveMinimum:Number?
  var minimum:Number?
  var exclusiveMaximum:Number?
  var maximum:Number?

  // ##################################################################
  // ########           ARRAY KEYWORDS                  ##############
  // ##################################################################

  var needsUniqueItems:Boolean
  var maxItems:Int?
  var minItems:Int?


  var schemaOfAdditionalItems:SchemaBuilder?
  var containsSchema:SchemaBuilder?

  var itemSchemas: List<SchemaBuilder>
  var allItemSchema:SchemaBuilder?

  // ##################################################################
  // ########           COMMON KEYWORDS                  ##############
  // ##################################################################

  var notSchema:SchemaBuilder?
  var enumValues:JsonArray?
  var const:Any?
  var constValue:JsonElement?

  var oneOfSchemas: List<SchemaBuilder>
  var anyOfSchemas: List<SchemaBuilder>
  var allOfSchemas: List<SchemaBuilder>
  var ifSchema:SchemaBuilder?
  var thenSchema:SchemaBuilder?
  var elseSchema:SchemaBuilder?

  operator fun invoke(block: SchemaBuilder.()->Unit):SchemaBuilder

  // ##################################################################
  // ########           INNER KEYWORDS                   ##############
  // ##################################################################

  /**
   * Adds a keyword directly to the builder - this bypasses any convenience methods, and can be used for loading custom
   * keywords.
   * @return reference to self
   */
//  fun <K : JsonSchemaKeyword<*>> keyword(keyword: KeywordInfo<K>, keywordValue: K)
//
//  fun <X : JsonSchemaKeyword<*>> getKeyword(keyword: KeywordInfo<X>): X?

  var extraProperties:Map<String, JsonElement>

  @Name("buildWithLocation")
  fun build(itemsLocation: SchemaLocation? = null, report: LoadingReport): Schema

  var loadingReport:LoadingReport
  var schemaLoader:SchemaLoader?
  var currentDocument:JsonObject?

  operator fun <K:Keyword<*>> set(keyword: KeywordInfo<K>, value: K)

  @Name("build")
  fun build(): Schema
}
