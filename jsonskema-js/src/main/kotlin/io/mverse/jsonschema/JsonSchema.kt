package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryImpl
import lang.URI

actual object JsonSchema {
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryImpl.builder().build()
  actual val validatorFactory: SchemaValidatorFactory = createValidatorFactory()
  actual fun schemaBuilder(id: URI): SchemaBuilder = JsonSchemaBuilder(id=id)
  actual fun schemaBuilder(id: String): SchemaBuilder = JsonSchemaBuilder(id=URI(id))
  actual fun schemaBuilder(block: SchemaBuilder.() -> Unit): SchemaBuilder = JsonSchemaBuilder().apply(block)
}
