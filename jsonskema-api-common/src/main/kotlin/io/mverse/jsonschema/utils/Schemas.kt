package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaType
import lang.uuid.randomUUID

object Schemas {
  val nullSchema: Schema = JsonSchema.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID())) {
    type = JsonSchemaType.NULL
  }.build()
  val emptySchema: Schema = JsonSchema.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID())).build()

  val falseSchema: Schema = JsonSchema.schemaBuilder(SchemaPaths.fromNonSchemaSource(randomUUID())) {
    notSchema { }
  }.build()

  fun nullSchemaBuilder(): MutableSchema {
    return nullSchema.toMutableSchema()
  }

  fun falseSchemaBuilder(): MutableSchema {
    return falseSchema.toMutableSchema()
  }
}

val Schema.isNullSchema: Boolean get() = this == Schemas.nullSchema
val Schema.isFalseSchema: Boolean get() = this == Schemas.falseSchema
val Schema.isEmptySchema: Boolean get() = this == Schemas.emptySchema
