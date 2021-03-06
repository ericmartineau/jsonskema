package io.mverse.jsonschema.validation.keywords

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.isValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.keyword.Keywords
import lang.json.jsrArrayOf
import lang.json.toJsrValue
import kotlin.test.Test

class EnumValidatorTest {

  @Test
  fun testEnumValidator_Vararg() {
    val schema = JsonSchema.schema {
      enumValues = jsrArrayOf("Bob", "Richard")
    }

    schema.validating("Enrique".toJsrValue())
        .isNotValid()
        .hasKeyword(Keywords.ENUM)

    schema.validating("Bob".toJsrValue())
        .isValid()
  }

  @Test
  fun testEnumValidator_JsonArray() {
    val schema = JsonSchema.schema {
      enumValues = jsrArrayOf("Bob", "Richard")
    }

    schema.validating("Enrique".toJsrValue())
        .isNotValid()
        .hasKeyword(Keywords.ENUM)

    schema.validating("Bob".toJsrValue())
        .isValid()
  }
}
