package io.mverse.jsonschema.validation.keywords.number

import assertk.assert
import assertk.assertThat
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.keyword.NumberKeyword
import io.mverse.jsonschema.utils.Schemas.nullSchema
import lang.json.toJsrValue
import org.junit.Test

class NumberMultipleOfValidatorTest {

  @Test
  fun divisionBySmallNumber() {
    val validator = NumberMultipleOfValidator(numberKeyword = NumberKeyword(0.0001),
        schema = nullSchema)
    val validated = validator.validate(0.0075.toJsrValue())
    assertThat(validated).isNull()
  }

  @Test
  fun divisionBySmallNumber_NotDivisible() {
    val validator = NumberMultipleOfValidator(numberKeyword = NumberKeyword(0.0001),
        schema = nullSchema)
    val validated = validator.validate(0.00751.toJsrValue())
    assertThat(validated).isNotNull()
  }
}
