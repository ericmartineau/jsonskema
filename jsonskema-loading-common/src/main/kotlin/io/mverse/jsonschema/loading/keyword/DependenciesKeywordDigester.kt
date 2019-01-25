package io.mverse.jsonschema.loading.keyword

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.MutableDependenciesKeyword
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES
import io.mverse.jsonschema.loading.KeywordDigest
import io.mverse.jsonschema.loading.KeywordDigester
import io.mverse.jsonschema.loading.LoadingIssues.typeMismatch
import io.mverse.jsonschema.loading.LoadingReport
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.digest
import lang.json.JsrArray
import lang.json.JsrObject
import lang.json.unbox
import lang.json.values

class DependenciesKeywordDigester : KeywordDigester<DependenciesKeyword> {

  override val includedKeywords = listOf(DEPENDENCIES)

  override fun extractKeyword(jsonObject: JsonValueWithPath, builder: MutableSchema,
                              schemaLoader: SchemaLoader, report: LoadingReport): KeywordDigest<DependenciesKeyword>? {

    val dependencies = jsonObject.path(DEPENDENCIES)
    val depsBuilder = MutableDependenciesKeyword()

    dependencies.forEachKey { key, pathValue ->
      val wrapped = pathValue.wrapped
      when (wrapped) {
        is JsrObject -> {
          val dependencySchema = schemaLoader.loadSubSchema(pathValue, pathValue.rootObject, report)
          depsBuilder.addDependencySchema(key, dependencySchema)
        }
        is JsrArray -> wrapped.values.forEach { arrayItem ->
          depsBuilder.propertyDependency(key, arrayItem.unbox())
        }
        else -> report.error(typeMismatch(DEPENDENCIES, pathValue.wrapped, pathValue.location))
      }
    }

    return DEPENDENCIES.digest(depsBuilder.build())
  }
}
