package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.net.URI
import lang.net.toURI
import kotlin.reflect.KClass

typealias SchemaMutator = MutableSchema.()->Unit

expect object JsonSchemas {
  fun createValidatorFactory(): SchemaValidatorFactory
  var validatorFactory: SchemaValidatorFactory
  var schemaReader: SchemaReader
  val schemaLoader: SchemaLoader

  fun schemaBuilder(id: URI, loader: SchemaLoader, block: SchemaMutator = {}): MutableSchema
  fun schemaBuilder(location: SchemaLocation, loader: SchemaLoader, block: SchemaMutator = {}): MutableSchema
  fun schema(id: URI, loader: SchemaLoader, block: SchemaMutator = {}): Schema
  fun schema(location: SchemaLocation, loader: SchemaLoader, block: SchemaMutator = {}): Schema
  fun draft3Schema(schema:Schema): Draft3Schema
  fun draft4Schema(schema:Schema): Draft4Schema
  fun draft6Schema(schema:Schema): Draft6Schema
  fun draft7Schema(schema:Schema): Draft7Schema
}

fun JsonSchemas.schema(id: String, loader: SchemaLoader, block: SchemaMutator = {}): Schema =
    JsonSchemas.schema(id.toURI(), loader, block)

fun JsonSchemas.schemaBuilder(id: String, loader: SchemaLoader, block: SchemaMutator = {}): MutableSchema =
    JsonSchemas.schemaBuilder(id.toURI(), loader, block)

fun JsonSchemas.getValidator(schema: Schema): SchemaValidator = JsonSchemas.validatorFactory.createValidator(schema)
