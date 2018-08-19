package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder

actual object JsonSchema {
  actual val validatorFactory: SchemaValidatorFactory get() = defaultValidatorFactory
  actual fun createValidatorFactory(): SchemaValidatorFactory = SchemaValidatorFactoryBuilder().build()
}
