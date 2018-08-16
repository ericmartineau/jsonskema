package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType


object Schemas {
  val nullSchema:Schema = JsonSchemaBuilder().type(JsonSchemaType.NULL).build()
  val emptySchema:Schema = JsonSchemaBuilder().build()
  val falseSchema:Schema = JsonSchemaBuilder().notSchema(JsonSchemaBuilder()).build()

  val Schema.isNullSchema: Boolean get() = this == nullSchema
  val Schema.isFalseSchema: Boolean get() = this == falseSchema
  val Schema.isEmptySchema: Boolean get() = this == emptySchema

  fun nullSchemaBuilder(): SchemaBuilder<*> {
    return nullSchema.toBuilder<JsonSchemaBuilder>()
  }

  fun falseSchemaBuilder(): SchemaBuilder<*> {
    return falseSchema.toBuilder<JsonSchemaBuilder>()
  }
}
