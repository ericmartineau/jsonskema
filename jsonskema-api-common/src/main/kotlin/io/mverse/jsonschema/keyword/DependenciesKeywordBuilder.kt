package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import lang.collection.MutableSetMultimap
import lang.collection.toMutableSetMultimap

data class DependenciesKeywordBuilder(private val propertyDependencies: MutableSetMultimap<String, String> = MutableSetMultimap(),
                                      private var dependencySchemas: SchemaMapKeyword = SchemaMapKeyword()) {

  constructor(keyword: DependenciesKeyword) : this(keyword.propertyDependencies.toMutableSetMultimap(), keyword.dependencySchemas)

  fun propertyDependency(ifThisProperty: String, thenExpectThisProperty: String): DependenciesKeywordBuilder {
    return apply {
      propertyDependencies += ifThisProperty to thenExpectThisProperty
    }
  }

  fun addDependencySchema(key: String, anotherValue: Schema): DependenciesKeywordBuilder {
    return apply {
      dependencySchemas += key to anotherValue
    }
  }

  fun build(): DependenciesKeyword {
    return DependenciesKeyword(dependencySchemas, propertyDependencies)
  }
}
