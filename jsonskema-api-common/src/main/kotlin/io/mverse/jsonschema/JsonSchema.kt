package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import lang.URI

expect object JsonSchema {
  fun createValidatorFactory(): SchemaValidatorFactory
  val validatorFactory: SchemaValidatorFactory
  fun schemaBuilder(id:URI): SchemaBuilder
  fun schemaBuilder(id:String): SchemaBuilder
  fun schemaBuilder(block:SchemaBuilder.()->Unit = {}):SchemaBuilder
}

fun JsonSchema.getValidator(schema:Schema): SchemaValidator = JsonSchema.validatorFactory.createValidator(schema)
fun JsonSchema.schema(block:SchemaBuilder.()->Unit):Schema = schemaBuilder().apply(block).build()
