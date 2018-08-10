package io.mverse.jsonschema.utils

import assertk.assert
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.enums.FormatType
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.BOOLEAN
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.jsonschema
import kotlinx.serialization.json.JsonNull
import lang.json.jsonArrayOf
import org.junit.Test

class JsonSchemaInspectionsTest {
  @Test
  fun testAmbiguous_EnumValues() {
    val draft6Schema = jsonschema {
      enumValues(jsonArrayOf(1, true))
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testArraySchema() {
    val draft6Schema = jsonschema {
      maxItems(23)
    }
    assert(draft6Schema.calculateType()).isEqualTo(ARRAY)
  }

  @Test
  fun testBooleanSchema() {
    val draft6Schema = jsonschema {
      type(BOOLEAN)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testBooleanSchema_EnumValues() {
    val draft6Schema = jsonschema {
      enumValues(jsonArrayOf(true, false))
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(BOOLEAN)
  }

  @Test
  fun testNullSchema() {
    val draft6Schema = jsonschema {
      type(NULL)
    }
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testNullSchema_EnumValues() {
    val draft6Schema = jsonschema {
      enumValues(jsonArrayOf(JsonNull))
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NULL)
  }

  @Test
  fun testNumberSchema() {
    val draft6Schema = jsonschema {
      maximum(23)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NUMBER)
  }

  @Test
  fun testNumberSchema_EnumValues() {
    val draft6Schema = jsonschema {
      enumValues(jsonArrayOf(1, 4))
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(NUMBER)
  }

  @Test
  fun testObjectSchema() {
    val draft6Schema = jsonschema {
      maxProperties(23)
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(OBJECT)
  }

  @Test
  fun testStringSchema() {
    val draft6Schema = jsonschema {
      format(FormatType.DATE.toString())
    }.asDraft6()
    assert(draft6Schema.calculateType()).isEqualTo(STRING)
  }
}
