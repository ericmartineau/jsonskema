package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.DependenciesKeywordBuilder
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues.typeMismatch
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import kotlinx.serialization.json.ElementType

class DependenciesKeywordDigester : KeywordDigester<DependenciesKeyword> {

  override val includedKeywords = listOf(DEPENDENCIES)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: SchemaBuilder,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<DependenciesKeyword>? {

    val dependencies = jsonObject.path(DEPENDENCIES)
    val depsBuilder = DependenciesKeywordBuilder()

    dependencies.forEachKey { key, pathValue ->
      when (pathValue.type) {
        ElementType.OBJECT -> {
          val dependencySchema = schemaLoader.loadSubSchema(pathValue, pathValue.rootObject, report)
          depsBuilder.addDependencySchema(key, dependencySchema)
        }
        ElementType.ARRAY -> pathValue.jsonArray.forEach { arrayItem ->
          depsBuilder.propertyDependency(key, arrayItem.primitive.content)
        }
        else -> report.error(typeMismatch(DEPENDENCIES, pathValue.wrapped, pathValue.location))
      }
    }

    return DEPENDENCIES.digest(depsBuilder.build())
  }
}
