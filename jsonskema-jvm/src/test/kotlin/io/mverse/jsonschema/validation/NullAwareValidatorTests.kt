package io.mverse.jsonschema.validation

import assertk.assertThat
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.validation.nullaware.nullableValidator
import lang.json.JsrNull
import lang.json.jsrObject
import org.junit.Test

class NullAwareValidatorTests {
  val schemaToTest = JsonSchemas.schema {
    properties {
      "myField" required {
        type = JsonSchemaType.STRING
      }
      "myOtherField" optional {
        type = JsonSchemaType.STRING
      }
    }
    type = JsonSchemaType.STRING
  }

  @Test
  fun nullValueTest() {
    val subject = JsonSchemas.schema {
      type = JsonSchemaType.STRING
    }
    assertThat(subject)
        .validating(JsrNull, SchemaValidatorFactory.nullableValidator())
        .isValid()
  }

  @Test
  fun nullValueRequiredTest() {
    assertThat(schemaToTest)
        .validating(jsrObject {
          "myField" *= JsrNull
          "myOtherField" *= JsrNull
        }, SchemaValidatorFactory.nullableValidator())
        .isNotValid()
        .hasViolationAt("#")
  }
}