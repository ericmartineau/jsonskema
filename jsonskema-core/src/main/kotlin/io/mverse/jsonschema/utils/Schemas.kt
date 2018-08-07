package io.mverse.jsonschema.utils

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaType

object Schemas {
  private val EMPTY_SCHEMA = JsonSchemaBuilder().build()
  private val NULL_SCHEMA_BUILDER = JsonSchemaBuilder().type(JsonSchemaType.NULL)
  private val NULL_SCHEMA = NULL_SCHEMA_BUILDER.build()
  private val FALSE_SCHEMA_BUILDER = JsonSchemaBuilder().notSchema(JsonSchemaBuilder())
  private val FALSE_SCHEMA = FALSE_SCHEMA_BUILDER.build()

  val Schema.isNullSchema: Boolean get() = this== NULL_SCHEMA
  val Schema.isFalseSchema: Boolean get() = this== FALSE_SCHEMA
  val Schema.isEmptySchema: Boolean get() = this== EMPTY_SCHEMA

  fun nullSchema(): Schema {
    return NULL_SCHEMA
  }

  fun nullSchemaBuilder(): SchemaBuilder<*> {
    return NULL_SCHEMA_BUILDER
  }

  fun falseSchema(): Schema {
    return FALSE_SCHEMA
  }

  fun emptySchema(): Schema {
    return EMPTY_SCHEMA
  }

  fun falseSchemaBuilder(): SchemaBuilder<*> {
    return FALSE_SCHEMA_BUILDER
  }
}
