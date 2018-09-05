package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.URI

actual object JsonSchema {
  actual val validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, block:SchemaBuilder.()->Unit): SchemaBuilder = JsonSchemaBuilder(id=id).apply(block)
  actual fun schemaBuilder(id: String, block:SchemaBuilder.()->Unit): SchemaBuilder = JsonSchemaBuilder(id=URI(id)).apply(block)

  actual val schemaBuilder: SchemaBuilder
    get() = JsonSchemaBuilder()
}
