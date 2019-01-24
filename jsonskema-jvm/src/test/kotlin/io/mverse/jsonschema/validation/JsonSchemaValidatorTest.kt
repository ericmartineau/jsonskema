package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import kotlinx.serialization.json.JsonNull
import lang.json.JsrNull
import lang.json.jsrNumber
import lang.json.toJsrValue
import org.junit.Test

class JsonSchemaValidatorTest {

  @Test
  fun validate_WhenValueIsNull_AppliesNullValidators() {
    val constSchema = JsonSchema.schema {
      constValue = jsrNumber(3.0)
    }

    val results = ValidationMocks.createTestValidator(constSchema).validate(JsrNull)
    assert(results).isNotNull()
  }
}
