package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableJsonSchema
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.builder.SchemaBuilderDsl
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.net.URI

actual object JsonSchema {
  actual val validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
  actual fun schemaBuilder(id: URI, block: SchemaBuilderDsl.()->Unit): SchemaBuilderDsl
      = SchemaBuilderDsl(MutableJsonSchema(id=id)).apply(block)
  actual fun schemaBuilder(id: String, block: SchemaBuilderDsl.()->Unit): SchemaBuilderDsl
      = SchemaBuilderDsl(MutableJsonSchema(id=URI(id))).apply(block)
  actual fun schemaBuilder(block: SchemaBuilderDsl.() -> Unit): SchemaBuilderDsl = SchemaBuilderDsl().apply(block)
  actual fun createSchemaBuilder(): MutableSchema = MutableJsonSchema()
  actual fun createSchemaBuilder(id: URI): MutableSchema = MutableJsonSchema(id)
  actual fun schema(id: URI, block: SchemaBuilderDsl.() -> Unit): Schema  {
    return SchemaBuilderDsl(MutableJsonSchema(id)).apply { block() }.build()
  }
  actual fun schema(id: String, block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl(MutableJsonSchema(URI(id))).apply { block() }.build()
  }
  actual fun schema(block: SchemaBuilderDsl.() -> Unit): Schema {
    return SchemaBuilderDsl().apply(block).build()
  }
}
