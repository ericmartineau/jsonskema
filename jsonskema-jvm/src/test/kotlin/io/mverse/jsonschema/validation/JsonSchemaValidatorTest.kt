package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertThat
import assertk.assertions.isNotNull
import io.mverse.jsonschema.schema
import lang.json.JsrNull
import lang.json.jsrNumber
import org.junit.Test

class JsonSchemaValidatorTest {

  @Test
  fun validate_WhenValueIsNull_AppliesNullValidators() {
    val constSchema = schema {
      constValue = jsrNumber(3.0)
    }

    val results = ValidationMocks.createTestValidator(constSchema).validate(JsrNull)
    assertThat(results).isNotNull()
  }
}
