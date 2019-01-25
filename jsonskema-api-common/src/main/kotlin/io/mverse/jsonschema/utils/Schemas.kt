package io.mverse.jsonschema.utils

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaType

object Schemas {
  val nullSchema: Schema = JsonSchema.schema { type = JsonSchemaType.NULL }
  val emptySchema: Schema = JsonSchema.schema { }

  val falseSchema: Schema = JsonSchema.schema {
    notSchema = JsonSchema.schemaBuilder()
  }

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
