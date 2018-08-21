package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.jsonschema
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.json
import org.junit.Test

class ValidationReportTest {

  @Test
  fun toStringTest() {
    val report = ValidationReport()
    val testSubject = JsonValueWithPath.fromJsonValue(json {})
    val stringSchema = JsonSchema.schemaBuilder()
        .pattern("[a-z]+")
        .minLength(12)
        .type(JsonSchemaType.STRING)
        .build()
    report += buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN)

    assert(report.toString()).isNotNull()
  }

  @Test
  fun testSerialization() {
    val report = ValidationReport()

    val testSubject = JsonValueWithPath.fromJsonValue(json {
      "name" to "George Jones"
    })
    val stringSchema = jsonschema {
      propertySchema("name") {
        pattern("[a-z]+")
        minLength(12)
        type(JsonSchemaType.STRING)
      }
    }

    report += ValidationError(violatedSchema = stringSchema,
        messageTemplate = "Foo has %s things",
        arguments = listOf(23),
        pointerToViolation = JsonPath.parseJsonPointer("/name"),
        code = "error.lots",
        keyword = Keywords.ANY_OF,
        causes = listOf(buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN)))

    val toString = JSON.stringify(report)
    assert(toString).isEqualTo("{\"errors\":[{\"pointerToViolation\":\"/name\",\"code\":\"error.lots\",\"messageTemplate\":\"Foo has %s things\",\"causes\":[{\"pointerToViolation\":\"\",\"code\":\"validation.keyword.pattern\",\"messageTemplate\":null,\"causes\":[],\"keyword\":\"pattern\",\"arguments\":null,\"pathToViolation\":\"#\",\"schemaLocation\":\"#\",\"violationCount\":1}],\"keyword\":\"anyOf\",\"arguments\":[[\"kotlin.Int\",23]],\"pathToViolation\":\"#/name\",\"schemaLocation\":\"#\",\"violationCount\":1}],\"foundError\":true,\"isValid\":false}")
  }
}
