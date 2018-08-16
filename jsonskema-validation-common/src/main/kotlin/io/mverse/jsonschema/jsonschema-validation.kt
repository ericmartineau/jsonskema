package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder

val defaultValidatorFactory = SchemaValidatorFactoryBuilder().build()
fun JsonSchema.validatorFactoryBuilder():SchemaValidatorFactoryBuilder = SchemaValidatorFactoryBuilder()
fun JsonSchema.createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
val JsonSchema.validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
fun JsonSchema.getValidator(schema:Schema): SchemaValidator = defaultValidatorFactory.createValidator(schema)
