package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.json
import lang.collection.Multimaps
import lang.collection.SetMultimap
import lang.json.toKtArray

data class DependenciesKeyword(val dependencySchemas: SchemaMapKeyword = SchemaMapKeyword(),
                               val propertyDependencies: SetMultimap<String, String> = Multimaps.emptySetMultimap())
  : SubschemaKeyword, Keyword<Map<String, Schema>> {

  override val value: Map<String, Schema> = dependencySchemas.value
  override val subschemas: List<Schema> get() = dependencySchemas.subschemas

  fun toBuilder(): DependenciesKeywordBuilder {
    return DependenciesKeywordBuilder(this)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.run {
      Keywords.DEPENDENCIES.key to json {
        propertyDependencies.asMap().forEach { (prop: String, setOfDependentProps: Collection<String>) ->
          prop to setOfDependentProps.toKtArray()
        }

        dependencySchemas.value.forEach { (key, schema) ->
          key to schema.asVersion(version).toJson()
        }
      }
    }
  }

  override fun withValue(value: Map<String, Schema>): Keyword<Map<String, Schema>> {
    return this.copy(dependencySchemas = SchemaMapKeyword(value))
  }

  companion object {
    fun builder(): DependenciesKeywordBuilder {
      return DependenciesKeywordBuilder()
    }
  }
}
