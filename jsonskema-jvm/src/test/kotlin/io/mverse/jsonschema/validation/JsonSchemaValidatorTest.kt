package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import kotlinx.serialization.json.JsonNull
import lang.json.toJsonLiteral
import org.junit.Test

class JsonSchemaValidatorTest {

  @Test
  fun validate_WhenValueIsNull_AppliesNullValidators() {
    val constSchema = JsonSchema.schema {
      constValue = 3.0.toJsonLiteral()
    }

    val results = ValidationMocks.createTestValidator(constSchema).validate(JsonNull)
    assert(results).isNotNull()
  }
}
