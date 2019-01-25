package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.builder.SchemaBuilderDsl
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.net.URI
import lang.net.toURI

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  var validatorFactory: SchemaValidatorFactory
  var schemaReader: SchemaReader
  val schemaLoader: SchemaLoader

  fun schemaBuilder(id: URI, loader: SchemaLoader = schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl
  fun schemaBuilder(loader: SchemaLoader = schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl

  fun schema(id: URI, loader: SchemaLoader = schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): Schema
  fun schema(loader: SchemaLoader = schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): Schema

  fun createSchemaBuilder(loader: SchemaLoader = schemaReader.loader): MutableSchema
  fun createSchemaBuilder(id: URI, loader: SchemaLoader = schemaReader.loader): MutableSchema
}

fun JsonSchema.schema(id: String, loader: SchemaLoader = JsonSchema.schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): Schema =
    JsonSchema.schema(id.toURI(), loader, block)

fun JsonSchema.schemaBuilder(id: String, loader: SchemaLoader = JsonSchema.schemaReader.loader, block: SchemaBuilderDsl.() -> Unit = {}): SchemaBuilderDsl =
    JsonSchema.schemaBuilder(id.toURI(), loader, block)

fun JsonSchema.getValidator(schema: Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)
