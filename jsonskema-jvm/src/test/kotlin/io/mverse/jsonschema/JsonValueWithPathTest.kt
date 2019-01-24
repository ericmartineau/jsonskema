package io.mverse.jsonschema

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import io.mverse.jsonschema.JsonValueWithPath.Companion.fromJsonValue
import io.mverse.jsonschema.TestUtils.createJsonArrayWithLocation
import io.mverse.jsonschema.TestUtils.createJsonNumberWithLocation
import io.mverse.jsonschema.TestUtils.createJsonObjectWithLocation
import io.mverse.jsonschema.TestUtils.createJsonStringWithLocation
import io.mverse.jsonschema.TestUtils.createValue
import io.mverse.jsonschema.enums.JsonSchemaType.ARRAY
import io.mverse.jsonschema.enums.JsonSchemaType.OBJECT
import lang.json.JsonKey
import lang.json.JsrNull
import lang.json.JsrType
import lang.json.get
import lang.json.jsrObject
import lang.json.toJsrValue
import lang.json.type
import lang.json.unboxAsAny
import nl.jqno.equalsverifier.EqualsVerifier
import nl.jqno.equalsverifier.Warning
import javax.json.JsonException
import kotlin.test.Test

class JsonValueWithPathTest {

  @Test
  fun testEquals() {
    EqualsVerifier.forClass(JsonValueWithPath::class.java)
        .suppress(Warning.STRICT_INHERITANCE)
        .withOnlyTheseFields("wrapped", "location")
        .verify()
  }

  @Test
  fun testValueType() {
    val jsonValue = jsrObject {
      "foo" *= "bar"
      "num" *= 3
    }
    val value = fromJsonValue(jsonValue)
    assert(value.type).isEqualTo(JsrType.OBJECT)
  }

  @Test
  fun testGetValueType_WhenNull_ThenNULL() {
    val value = JsrNull
    assert(value.type).isEqualTo(JsrType.NULL)
  }

  @Test
  fun testAsJsonObject_HasValue() {
    val value = createJsonObjectWithLocation()
    val jsonValue = value.jsonObject!![JsonKey("foo")].unboxAsAny()
    assert(jsonValue).isEqualTo("bar")
  }

  @Test
  fun testAsJsonArray_HasValue() {
    val value = createJsonArrayWithLocation()
    val jsonArray = value.jsonArray
    val expected = "foo".toJsrValue()
    assert(jsonArray!![0]).isEqualTo(expected)
  }

  @Test
  fun testGetJsonSchemaType_WhenObject_ReturnObject() {
    val value = createJsonObjectWithLocation()
    assert(value.jsonSchemaType).isEqualTo(OBJECT)
  }

  @Test
  fun testGetJsonSchemaType_WhenArray_ReturnArray() {
    val value = createJsonArrayWithLocation()
    assert(value.jsonSchemaType).isEqualTo(ARRAY)
  }

  @Test
  fun testArraySize_WhenArray_ReturnsSize() {
    val value = createJsonArrayWithLocation()
    assert(value.jsonArray!!.size).isEqualTo(5)
  }

  @Test
  fun testArraySize_WhenNotArray_ReturnsZero() {
    assert(createJsonObjectWithLocation().size).isEqualTo(2)
  }

  @Test
  fun testJsonArray_WhenNotArray_ReturnsNull() {
    assert(createJsonObjectWithLocation().jsonArray).isNull()
  }

  @Test
  fun testAsJsonNumber_WhenNumber_ReturnsJsonNumber() {
    assert(createJsonNumberWithLocation(34.4).number).isNotNull()
  }

  @Test(expected = JsonException::class)
  fun testAsJsonNumber_WhenNotNumber_ThrowsUVE() {
    createJsonObjectWithLocation().number
  }

  @Test
  fun testAsJsonObject_WhenObject_ReturnsJsonObject() {
    assert(createJsonObjectWithLocation().jsonObject).isNotNull()
  }

  @Test
  fun testAsJsonObject_WhenNotObject_ReturnsNull() {
    assert(createJsonArrayWithLocation().jsonObject).isNull()
  }

  @Test
  fun testAsJsonString_WhenString_ReturnsJsonString() {
    assert(createJsonStringWithLocation("joe").string).isEqualTo("joe")
  }

  @Test
  fun testAsJsonString_WhenNull_ReturnsNull() {
    assert(createValue(JsrNull).string).isNull()
  }
}
