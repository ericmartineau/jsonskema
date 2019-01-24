package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import lang.collection.Multimaps
import lang.collection.MutableSetMultimap
import lang.collection.SetMultimap
import lang.json.JsonPath
import lang.json.MutableJsrObject
import lang.json.createJsrArray
import lang.json.jsrObject
import lang.json.jsrString

data class DependenciesKeyword(val dependencySchemas: SchemaMapKeyword = SchemaMapKeyword(),
                               val propertyDependencies: SetMultimap<String, String> = Multimaps.emptySetMultimap())
  : SubschemaKeyword, Keyword<Map<String, Schema>> {

  override val value: Map<String, Schema> = dependencySchemas.value
  override val subschemas: List<Schema> get() = dependencySchemas.subschemas

  fun toBuilder(): DependenciesKeywordBuilder {
    return DependenciesKeywordBuilder(this)
  }

  override fun toJson(keyword: KeywordInfo<*>, builder: MutableJsrObject, version: JsonSchemaVersion, includeExtraProperties: Boolean) {
    builder.apply {
      Keywords.DEPENDENCIES.key *= jsrObject {
        propertyDependencies.asMap().forEach { (prop: String, setOfDependentProps: Collection<String>) ->
          prop *= createJsrArray(setOfDependentProps.map { jsrString(it) })
        }

        dependencySchemas.value.forEach { (key, schema) ->
          key *= schema.asVersion(version).toJson()
        }
      }
    }
  }

  override fun withValue(value: Map<String, Schema>): Keyword<Map<String, Schema>> {
    return this.copy(dependencySchemas = SchemaMapKeyword(value))
  }

  override fun merge(path: JsonPath, keyword: KeywordInfo<*>, other: Keyword<Map<String, Schema>>, report: MergeReport): Keyword<Map<String, Schema>> {
    val deps = other as DependenciesKeyword
    val thisProps = this.propertyDependencies.asMap()
    val multimap = MutableSetMultimap<String, String>()

    thisProps.forEach {
      it.value.forEach { value->
        multimap += it.key to value
      }
    }

    deps.propertyDependencies.asMap().forEach {
      it.value.forEach { value->
        multimap += it.key to value
      }
    }
    return DependenciesKeyword(dependencySchemas = dependencySchemas.merge(path, keyword, deps.dependencySchemas, report) as SchemaMapKeyword,
        propertyDependencies = multimap.freeze())
  }

  companion object {
    fun builder(): DependenciesKeywordBuilder {
      return DependenciesKeywordBuilder()
    }
  }
}
