package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.net.URI

actual object JsonSchemas {
  actual var validatorFactory: SchemaValidatorFactory = defaultValidatorFactory
  actual var schemaReader: SchemaReader = defaultSchemaReader
  actual val schemaLoader: SchemaLoader get() = defaultSchemaReader.loader
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, loader: SchemaLoader, block: SchemaMutator): MutableSchema = MutableJsonSchema(loader, id = id).apply(block)

  actual fun schemaBuilder(location: SchemaLocation, loader: SchemaLoader, block: SchemaMutator): MutableSchema = MutableJsonSchema(schemaLoader = loader, location = location).apply(block)

  actual fun schema(id: URI, loader: SchemaLoader, block: SchemaMutator): Schema =
      MutableJsonSchema(loader, id).apply { block() }.build()

  actual fun schema(location: SchemaLocation, loader: SchemaLoader, block: SchemaMutator): Schema =
      MutableJsonSchema(loader, location).apply(block).build()

  actual fun draft3Schema(schema: Schema): Draft3Schema = schema as Draft3Schema
  actual fun draft4Schema(schema: Schema): Draft4Schema = schema as Draft4Schema
  actual fun draft6Schema(schema: Schema): Draft6Schema = schema as Draft6Schema
  actual fun draft7Schema(schema: Schema): Draft7Schema = schema as Draft7Schema
}
