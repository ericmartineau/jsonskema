package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.Schema
import lang.collection.MutableSetMultimap
import lang.collection.toMutableSetMultimap

data class MutableDependenciesKeyword(private val propertyDependencies: MutableSetMultimap<String, String> = MutableSetMultimap(),
                                      private var dependencySchemas: SchemaMapKeyword = SchemaMapKeyword()) {

  constructor(keyword: DependenciesKeyword) : this(keyword.propertyDependencies.toMutableSetMultimap(), keyword.dependencySchemas)

  fun propertyDependency(ifThisProperty: String, thenExpectThisProperty: String): MutableDependenciesKeyword {
    return apply {
      propertyDependencies.add(ifThisProperty, thenExpectThisProperty)
    }
  }

  fun addDependencySchema(key: String, anotherValue: Schema): MutableDependenciesKeyword {
    return apply {
      dependencySchemas += key to anotherValue
    }
  }

  fun build(): DependenciesKeyword {
    return DependenciesKeyword(dependencySchemas, propertyDependencies)
  }
}
