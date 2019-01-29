package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaType
import lang.uuid.randomUUID

object Schemas {
  val nullSchema: Schema = JsonSchemas.schema(randomLocation(), JsonSchemas.schemaLoader) {
    type = JsonSchemaType.NULL
  }

  val emptySchema: Schema = JsonSchemas.schema(randomLocation(), JsonSchemas.schemaLoader)

  val falseSchema: Schema = JsonSchemas.schema(randomLocation(), JsonSchemas.schemaLoader) {
    notSchema { }
  }

  fun nullSchemaBuilder(): MutableSchema {
    return nullSchema.toMutableSchema()
  }

  fun falseSchemaBuilder(): MutableSchema {
    return falseSchema.toMutableSchema()
  }

  fun randomLocation() = SchemaPaths.fromNonSchemaSource(randomUUID())
}

val Schema.isNullSchema: Boolean get() = this == Schemas.nullSchema
val Schema.isFalseSchema: Boolean get() = this == Schemas.falseSchema
val Schema.isEmptySchema: Boolean get() = this == Schemas.emptySchema
