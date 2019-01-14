package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import kotlinx.serialization.json.JsonNull
import lang.json.jsrArrayOf
import org.junit.Test

class JsonSchemaInspectionsTest {
  @Test
  fun testAmbiguous_EnumValues() {
    val draft6Schema = JsonSchema.schema {
      enumValues = jsrArrayOf(1, true)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testArraySchema() {
    val draft6Schema = JsonSchema.schema {
      maxItems = 23
    }
    assert(draft6Schema.calculateType()).isEqualTo(ARRAY)
  }

  @Test
  fun testBooleanSchema() {
    val draft6Schema = JsonSchema.schema {
      type = BOOLEAN
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testBooleanSchema_EnumValues() {
    val draft6Schema = JsonSchema.schema {
      enumValues = jsrArrayOf(true, false)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testNullSchema() {
    val draft6Schema = JsonSchema.schema {
      type = NULL
    }
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testNullSchema_EnumValues() {
    val draft6Schema = JsonSchema.schema {
      enumValues = jsrArrayOf(JsonNull)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testNumberSchema() {
    val draft6Schema = JsonSchema.schema {
      maximum = 23
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NUMBER)
  }

  @Test
  fun testNumberSchema_EnumValues() {
    val draft6Schema = JsonSchema.schema {
      enumValues = jsrArrayOf(1, 4)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NUMBER)
  }

  @Test
  fun testObjectSchema() {
    val draft6Schema = JsonSchema.schema {
      maxProperties = 23
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(OBJECT)
  }

  @Test
  fun testStringSchema() {
    val draft6Schema = JsonSchema.schema {
      format = FormatType.DATE.toString()
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(STRING)
  }
}
