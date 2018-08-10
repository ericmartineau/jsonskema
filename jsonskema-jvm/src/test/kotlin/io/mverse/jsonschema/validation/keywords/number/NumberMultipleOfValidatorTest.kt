package io.mverse.jsonschema.validation.keywords.number

import assertk.assert
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.utils.Schemas.nullSchema
import io.mverse.jsonschema.validation.SchemaValidatorFactoryBuilder
import lang.json.toJsonLiteral
import org.junit.Test

class NumberMultipleOfValidatorTest {

  @Test
  fun divisionBySmallNumber() {
    val validator = NumberMultipleOfValidator(numberKeyword = NumberKeyword(0.0001),
        schema = nullSchema, factory = SchemaValidatorFactoryBuilder().build())
    val validated = validator.validate(0.0075.toJsonLiteral())
    assert(validated).isNull()
  }

  @Test
  fun divisionBySmallNumber_NotDivisible() {
    val validator = NumberMultipleOfValidator(numberKeyword = NumberKeyword(0.0001),
        schema = nullSchema, factory = SchemaValidatorFactoryBuilder().build())
    val validated = validator.validate(0.00751.toJsonLiteral())
    assert(validated).isNotNull()
  }
}
