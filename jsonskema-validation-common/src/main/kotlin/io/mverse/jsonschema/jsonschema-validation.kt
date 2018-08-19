package io.mverse.jsonschema

import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder

val defaultValidatorFactory = SchemaValidatorFactoryBuilder().build()
