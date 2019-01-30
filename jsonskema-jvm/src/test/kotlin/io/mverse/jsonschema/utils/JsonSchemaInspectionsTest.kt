package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.schema
import kotlinx.serialization.json.JsonNull
import lang.json.jsrArrayOf
import org.junit.Test

class JsonSchemaInspectionsTest {
  @Test
  fun testAmbiguous_EnumValues() {
    val draft6Schema = JsonSchemas.schema {
      enumValues = jsrArrayOf(1, true)
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(NULL)
  }

  @Test
  fun testArraySchema() {
    val draft6Schema = schema {
      maxItems = 23
    }
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(ARRAY)
  }

  @Test
  fun testBooleanSchema() {
    val draft6Schema = JsonSchemas.schema {
      type = BOOLEAN
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testBooleanSchema_EnumValues() {
    val draft6Schema = JsonSchemas.schema {
      enumValues = jsrArrayOf(true, false)
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testNullSchema() {
    val draft6Schema = JsonSchemas.schema {
      type = NULL
    }
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(NULL)
  }

  @Test
  fun testNullSchema_EnumValues() {
    val draft6Schema = JsonSchemas.schema {
      enumValues = jsrArrayOf(JsonNull)
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(NULL)
  }

  @Test
  fun testNumberSchema() {
    val draft6Schema = JsonSchemas.schema {
      maximum = 23
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(NUMBER)
  }

  @Test
  fun testNumberSchema_EnumValues() {
    val draft6Schema = JsonSchemas.schema {
      enumValues = jsrArrayOf(1, 4)
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(NUMBER)
  }

  @Test
  fun testObjectSchema() {
    val draft6Schema = JsonSchemas.schema {
      maxProperties = 23
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(OBJECT)
  }

  @Test
  fun testStringSchema() {
    val draft6Schema = JsonSchemas.schema {
      format = FormatType.DATE.toString()
    }.draft6()
    assert(draft6Schema.calculateJsonSchemaType()).isEqualTo(STRING)
  }
}
