package io.mverse.jsonschema.validation.nullaware

import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.SchemaValidatorFactory
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder

fun SchemaValidatorFactoryBuilder.nullableValidator(): SchemaValidatorFactoryBuilder {
  replaceValidator(Keywords.REQUIRED) { keyword, schema, factory -> NullAwareRequiredPropertyValidator(keyword, schema) }
  replaceValidator(Keywords.TYPE) { keyword, schema, factory -> NullAwareTypeValidator(keyword, schema, factory) }
  return this
}

/**
 * Creates a validator that uses nullable types - allowing type=null for any other type declaration.
 * as a countermeasure, any required field cannot be null
 */
fun SchemaValidatorFactory.Companion.nullableValidator(): SchemaValidatorFactory {
  return SchemaValidatorFactoryBuilder()
      .withCommonValidators()
      .nullableValidator()
      .build()
}