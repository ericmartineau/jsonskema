package io.mverse.jsonschema.integration

import io.mverse.json.jsr353.clean
import io.mverse.json.jsr353.raw
import io.mverse.json.jsr353.untyped
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.utils.SchemaPaths
import io.mverse.jsonschema.validation.SchemaValidator
import io.mverse.jsonschema.validation.SchemaValidatorFactoryImpl
import io.mverse.jsonschema.validation.ValidationReport
import lang.json.JsrObject
import org.junit.Test
import java.util.*

class PerfBenchmark {

  @Test
  fun testPerformance() {
    val draft6 = JsonSchema.resourceLoader().readJsonObject("json-schema-draft-06.json")
    val draft6Schema = JsonSchema.createSchemaReader()
        .readSchema(draft6)
    val validator = SchemaValidatorFactoryImpl.createValidatorForSchema(draft6Schema)

    val jsonObject = JsonSchema.resourceLoader().readJsonObject("perftest.json")
    val testSubjects = ArrayList<JsonValueWithPath>()
    jsonObject.raw.get<JsrObject>("schemas").forEach { (_, v) ->
      testSubjects.add(JsonValueWithPath.fromJsonValue(v, v, SchemaPaths.fromNonSchemaSource(v)))
    }

    val startAt = System.currentTimeMillis()
    val report = doValidations(testSubjects, validator)
    System.out.println(report.toString())

    val endAt = System.currentTimeMillis()
    val execTime = endAt - startAt
    println("total time: $execTime ms")
  }

  companion object {

    fun doValidations(testSubjects: List<JsonValueWithPath>, validator: SchemaValidator): ValidationReport {
      val report = ValidationReport()

      val startAt = System.currentTimeMillis()
      for (i in 0..499) {
        for (testSubject in testSubjects) {
          if (!validator.validate(testSubject, report)) {
            throw IllegalStateException("OOPS: " + report.errors)
          }
        }

        if (i % 20 == 0) {
          println("Iteration " + i + " (in " + (System.currentTimeMillis() - startAt) + "ms)")
        }
      }

      return report
    }
  }
}
