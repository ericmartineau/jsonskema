package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.createTestValidator
import kotlinx.serialization.json.JsonNull
import org.junit.Test

class JsonSchemaValidatorTest {

  @Test
  fun validate_WhenValueIsNull_AppliesNullValidators() {
    val constSchema = JsonSchema.schemaBuilder()
        .constValueDouble(3.0)
        .build()

    val results = ValidationMocks.createTestValidator(constSchema).validate(JsonNull)
    assert(results).isNotNull()
  }
}
