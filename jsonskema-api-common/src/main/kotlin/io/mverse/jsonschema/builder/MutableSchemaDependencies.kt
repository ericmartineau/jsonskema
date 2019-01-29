package io.mverse.jsonschema.builder

import io.mverse.jsonschema.JsonSchemas.schemaBuilder
import io.mverse.jsonschema.keyword.DependenciesKeyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.DEPENDENCIES

class MutableSchemaDependencies(val builder: MutableSchema) {

  operator fun set(key: String, block: MutableSchema.() -> Unit) {
    val existing = (builder[DEPENDENCIES] ?: DependenciesKeyword()).toBuilder()
    val childLocation = builder.location.child(Keywords.PROPERTIES).child(key)
    val newProperty = schemaBuilder(childLocation, builder.schemaLoader, block)
    existing.addDependencySchema(key, builder.buildSubSchema(newProperty, DEPENDENCIES, key))
    builder[DEPENDENCIES] = existing.build()
  }

  operator fun set(key: String, schema: MutableSchema) {
    val existing = (builder[DEPENDENCIES] ?: DependenciesKeyword()).toBuilder()
    val childLocation = builder.location.child(Keywords.PROPERTIES).child(key)
    val newProperty = schema.withLocation(childLocation)
    existing.addDependencySchema(key, builder.buildSubSchema(newProperty, DEPENDENCIES, key))
    builder[DEPENDENCIES] = existing.build()
  }

  operator fun invoke(block: MutableSchemaDependencies.() -> Unit) {
    block()
  }
}
