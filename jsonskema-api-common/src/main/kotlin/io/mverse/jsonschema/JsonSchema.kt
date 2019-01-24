package io.mverse.jsonschema

import io.mverse.jsonschema.builder.SchemaBuilderDsl
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.net.URI

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  val validatorFactory: SchemaValidatorFactory

  fun schemaBuilder(id: URI, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl
  fun schemaBuilder(id: String, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl
  fun schemaBuilder(block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl

  fun schema(id: URI, block: SchemaBuilderDsl.() -> Unit = {}): Schema
  fun schema(id: String, block: SchemaBuilderDsl.() -> Unit = {}): Schema
  fun schema(block: SchemaBuilderDsl.() -> Unit = {}): Schema

  fun createSchemaBuilder(): SchemaBuilder
  fun createSchemaBuilder(id: URI): SchemaBuilder
}

fun JsonSchema.getValidator(schema: Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)
