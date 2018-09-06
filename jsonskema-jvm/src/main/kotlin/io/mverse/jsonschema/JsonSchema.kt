package io.mverse.jsonschema

import io.mverse.jsonschema.builder.JsonSchemaBuilder
import io.mverse.jsonschema.builder.SchemaBuilderDsl
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.URI

actual object JsonSchema {
  actual val validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, block: SchemaBuilderDsl.()->Unit): SchemaBuilderDsl
      = SchemaBuilderDsl(JsonSchemaBuilder(id=id)).apply(block)
  actual fun schemaBuilder(id: String, block: SchemaBuilderDsl.()->Unit): SchemaBuilderDsl
      = SchemaBuilderDsl(JsonSchemaBuilder(id=URI(id))).apply(block)
  actual fun schemaBuilder(block: SchemaBuilderDsl.() -> Unit): SchemaBuilderDsl = SchemaBuilderDsl().apply(block)
  actual fun createSchemaBuilder(): SchemaBuilder = JsonSchemaBuilder()
  actual fun createSchemaBuilder(id: URI): SchemaBuilder = JsonSchemaBuilder(id)
  actual fun schema(id: URI, block: SchemaBuilderDsl.() -> Unit): Schema  {
    return SchemaBuilderDsl(JsonSchemaBuilder(id)).apply { block() }.build()
  }
  actual fun schema(id: String, block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl(JsonSchemaBuilder(URI(id))).apply { block() }.build()
  }
  actual fun schema(block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl().apply(block).build()
  }
}
