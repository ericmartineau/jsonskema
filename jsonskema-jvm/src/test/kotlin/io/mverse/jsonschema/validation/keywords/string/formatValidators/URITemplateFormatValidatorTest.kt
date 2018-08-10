package io.mverse.jsonschema.validation.keywords.string.formatValidators

import assertk.assert
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test

class URITemplateFormatValidatorTest {

  @Test
  fun testValidUriTemplate() {
    val template = "/{foo:1}{/foo,thing*}{?query,test2}"
    val validate = URITemplateFormatValidator().validate(template)
    assert(validate).isNull()
  }

  @Test
  fun testInvalidUriTemplate() {
    val template = "/{foo::1}{/foo,thing*}{?query,test2}"
    val validate = URITemplateFormatValidator().validate(template)
    assert(validate).isNotNull()
  }
}
