package io.mverse.jsonschema.validation.keywords.string.formatValidators

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import kotlinx.serialization.json.JsonNull
import org.junit.Test

class JsonPointerValidatorTest {
  @Test
  fun validate_WhenInvalid_ValidationFails() {
    val validate = JsonPointerValidator().validate("not/a/valid   pointer")
    assert(validate).isEqualTo("invalid json-pointer syntax.  Must either be blank or start with a /")
  }

  @Test
  fun validate_WhenEmpty_ValidationPasses() {

    val validate = JsonPointerValidator().validate("")
    assert(validate).isNull()
  }

  @Test
  fun validate_WhenValid_ValidationPasses() {

    val validate = JsonPointerValidator().validate("/bob/is/cool")
    assert(validate).isNull()
  }

  @Test
  fun validate_WhenDoubleSlash_ValidationFails() {
    val validate = JsonPointerValidator().validate("/bob//is/cool")
    assert(validate, validate).isNotNull()
  }

  @Test
  fun validate_WhenEndsInSlash_ValidationFails() {

    val validate = JsonPointerValidator().validate("/bob/is/cool/")
    assert(validate, validate).isNotNull()
  }
}
