package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.DependenciesKeywordBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.json
import lang.SetMultimap
import lang.json.toJsonArray

data class DependenciesKeyword(val dependencySchemas: SchemaMapKeyword = SchemaMapKeyword(),
                               val propertyDependencies: SetMultimap<String, String> = SetMultimap())
  : SubschemaKeyword, JsonSchemaKeyword<Map<String, Schema>> {

  override val value: Map<String, Schema> = dependencySchemas.value

  override fun copy(value: Map<String, Schema>): JsonSchemaKeyword<Map<String, Schema>> {
    return this.copy(dependencySchemas = SchemaMapKeyword(value))
  }

  override val subschemas: List<Schema> get() = dependencySchemas.subschemas

  fun toBuilder(): DependenciesKeywordBuilder {
    return DependenciesKeywordBuilder(this)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: kotlinx.serialization.json.JsonBuilder, version: JsonSchemaVersion) {
    builder.run {
      Keywords.DEPENDENCIES.key to json {
        propertyDependencies.asMap().forEach { (prop:String, setOfDependentProps:Set<String>) ->
          prop to setOfDependentProps.toJsonArray()
        }

        dependencySchemas.value.forEach{ (key, schema) ->
          key to schema.asVersion(version).toJson()
        }
      }
    }
  }

  companion object {
    fun builder(): DependenciesKeywordBuilder {
      return DependenciesKeywordBuilder()
    }
  }
}
