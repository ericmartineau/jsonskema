package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.DependenciesKeywordBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.json
import lang.SetMultimap
import lang.json.asJsonArray

data class DependenciesKeyword(val dependencySchemas: SchemaMapKeyword = SchemaMapKeyword(),
                               val propertyDependencies: SetMultimap<String, String> = SetMultimap())
  : SubschemaKeyword, JsonSchemaKeyword<Map<String, Schema>> {

  override val value: Map<String, Schema>? = dependencySchemas.schemas
  override val subschemas: List<Schema> get() = dependencySchemas.subschemas

  fun toBuilder(): DependenciesKeywordBuilder {
    return DependenciesKeywordBuilder(this)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: JsonBuilder, version: JsonSchemaVersion) {
    builder.run {
      Keywords.DEPENDENCIES.key to json {
        propertyDependencies.asMap().forEach { (prop:String, setOfDependentProps:Set<String>) ->
          prop to setOfDependentProps.asJsonArray()
        }

        dependencySchemas.schemas.forEach{ (key, schema) ->
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
