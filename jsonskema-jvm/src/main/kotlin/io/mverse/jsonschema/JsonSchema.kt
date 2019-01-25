package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.builder.SchemaBuilderDsl
import io.mverse.jsonschema.loading.SchemaLoader
import io.mverse.jsonschema.loading.SchemaReader
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.net.URI

actual object JsonSchema {
  actual var validatorFactory: SchemaValidatorFactory = defaultValidatorFactory
  actual var schemaReader: SchemaReader = defaultSchemaReader
  actual val schemaLoader: SchemaLoader get() = defaultSchemaReader.loader
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, loader: SchemaLoader, block: SchemaBuilderDsl.() -> Unit): SchemaBuilderDsl = SchemaBuilderDsl(MutableJsonSchema(loader, id = id)).apply(block)
  actual fun schemaBuilder(loader: SchemaLoader, block: SchemaBuilderDsl.() -> Unit): SchemaBuilderDsl = SchemaBuilderDsl(createSchemaBuilder(loader)).apply(block)
  actual fun createSchemaBuilder(loader: SchemaLoader): MutableSchema = MutableJsonSchema(loader)
  actual fun createSchemaBuilder(id: URI, loader: SchemaLoader): MutableSchema = MutableJsonSchema(loader, id)
  actual fun schema(id: URI, loader: SchemaLoader, block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl(MutableJsonSchema(loader, id)).apply { block() }.build()
  }

  actual fun schema(loader: SchemaLoader, block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl(createSchemaBuilder(loader)).apply(block).build()
  }
}
