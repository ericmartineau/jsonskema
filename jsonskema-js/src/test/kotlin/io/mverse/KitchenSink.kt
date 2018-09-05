package io.mverse

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.defaultValidatorFactory
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schema
import lang.json.toJsonLiteral
import kotlin.test.Test

class KitchenSink {

  @Test
  fun testSchemas() {
    val schema = JsonSchema.schema {
      type = JsonSchemaType.STRING
      minLength = 8
      maxLength = 12
    }

    val validator = defaultValidatorFactory.createValidator(schema)
    val results = validator.validate("Bob".toJsonLiteral())
    assert(results).isNotNull {
      assert(it.actual.violationCount).isEqualTo(1)
      assert(it.actual.keyword).isEqualTo(Keywords.MIN_LENGTH)
    }
  }
}
