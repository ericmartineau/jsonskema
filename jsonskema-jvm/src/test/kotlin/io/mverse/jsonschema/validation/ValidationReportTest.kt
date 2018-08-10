package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationErrorHelper.buildKeywordFailure
import kotlinx.serialization.json.json
import org.junit.Test

class ValidationReportTest {

  @Test
  fun toStringTest() {
    val report = ValidationReport()
    val testSubject = JsonValueWithPath.fromJsonValue(json{})
    val stringSchema = JsonSchema.schemaBuilder()
        .pattern("[a-z]+")
        .minLength(12)
        .type(JsonSchemaType.STRING)
        .build()
    report += buildKeywordFailure(testSubject, stringSchema, Keywords.PATTERN)

    assert(report.toString()).isNotNull()
  }
}
