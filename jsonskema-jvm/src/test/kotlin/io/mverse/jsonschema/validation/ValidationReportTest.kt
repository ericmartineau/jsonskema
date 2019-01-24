package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import lang.json.JsonPath
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.json
import kotlinx.serialization.stringify
import lang.json.JSON
import lang.json.jsrObject
import org.junit.Test

class ValidationReportTest {

  @Test
  fun toStringTest() {
    val report = ValidationReport()
    val testSubject = JsonValueWithPath.fromJsonValue(jsrObject {})
    val stringSchema = JsonSchema.schema {
      pattern = "[a-z]+"
      minLength = 12
      type = JsonSchemaType.STRING
    }
    report += buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN)

    assert(report.toString()).isNotNull()
  }

  @Test
  @UseExperimental(ImplicitReflectionSerializer::class)
  fun testSerialization() {
    val report = ValidationReport()

    val testSubject = JsonValueWithPath.fromJsonValue(jsrObject {
      "name" *= "George Jones"
    })
    val stringSchema = JsonSchema.schema {
      properties["name"] = {
        pattern = "[a-z]+"
        minLength = 12
        type = JsonSchemaType.STRING
      }
    }

    report += ValidationError(violatedSchema = stringSchema,
        messageTemplate = "Foo has %s things",
        arguments = listOf(23),
        pointerToViolation = JsonPath("/name"),
        code = "error.lots",
        keyword = Keywords.ANY_OF,
        causes = listOf(buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN)))


    val toString = JSON.stringify(report)
    assert(toString).isEqualTo("{\"errors\":[{\"pointerToViolation\":\"/name\",\"code\":\"error.lots\",\"messageTemplate\":\"Foo has %s things\",\"causes\":[{\"pointerToViolation\":\"\",\"code\":\"validation.keyword.pattern\",\"messageTemplate\":null,\"causes\":[],\"keyword\":\"pattern\",\"arguments\":null,\"pathToViolation\":\"#\",\"schemaLocation\":\"#\",\"violationCount\":1}],\"keyword\":\"anyOf\",\"arguments\":[[\"kotlin.Int\",23]],\"pathToViolation\":\"#/name\",\"schemaLocation\":\"#\",\"violationCount\":1}],\"foundError\":true,\"isValid\":false}")
  }
}
