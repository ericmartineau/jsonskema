package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.URI

actual object JsonSchema {
  actual val validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI): SchemaBuilder = JsonSchemaBuilder(id=id)

  actual fun schemaBuilder(id: String): SchemaBuilder = JsonSchemaBuilder(id=URI(id))

  actual fun schemaBuilder(block: SchemaBuilder.() -> Unit): SchemaBuilder =
      JsonSchemaBuilder().apply(block)
}
