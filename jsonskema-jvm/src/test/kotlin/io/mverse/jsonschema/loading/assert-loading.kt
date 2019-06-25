package io.mverse.jsonschema.loading

import assertk.Assert
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.fail
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.createSchemaReader
import lang.json.JsrObject

typealias SchemaLoadingAssert = Assert<LoadingReport?>
typealias SafeSchemaLoadingAssert = Assert<LoadingReport>
typealias LoadingIssueAssert = Assert<LoadingIssue?>
typealias SafeLoadingIssueAssert = Assert<LoadingIssue>

fun JsrObject.assertAsSchema(reader: SchemaReader = JsonSchemas.createSchemaReader(),
                             block: SafeSchemaLoadingAssert.() -> Unit = {}): SchemaLoadingAssert {
  return try {
    reader.readSchema(this)
    assertThat(null as LoadingReport?)
  } catch (e: SchemaLoadingException) {
    return assertThat(e.report).apply(block)
  }
}

fun SchemaLoadingAssert.failedAt(schemaLocation: String? = null,
                                 block: SafeLoadingIssueAssert.() -> Unit = {}) {
  given { actual ->
    if (schemaLocation != null) {
      val found = actual?.issues?.firstOrNull {
        schemaLocation == it.location?.jsonPointerFragment?.toString()
      }
      if (found == null) {
        fail("No error found at location $schemaLocation, but found errors at ${actual?.issues?.map { it.location }}")
      } else {
        assertThat(found).block()
      }
    }
  }
}

fun SchemaLoadingAssert.isFailed(errorCount: Int? = null, block: SafeSchemaLoadingAssert.() -> Unit = {}) {
  given { actual ->
    if (actual?.hasErrors() != true) {
      fail("Unexpected success! Try harder to fail.")
    } else {
      assertThat(actual.issues.size, "error count").isEqualTo(errorCount)
      assertThat(actual).block()
    }
  }
}

fun LoadingIssueAssert.isNotNull(block: SafeLoadingIssueAssert.() -> Unit) {
  given { actual ->
    if (actual == null) {
      fail("Expected loading errors to not be null")
    } else {
      assertThat(actual).block()
    }
  }
}

fun LoadingIssueAssert.hasLevel(errorLevel: LoadingIssueLevel) {
  isNotNull().transform { it.level }.isEqualTo(errorLevel)
}

fun LoadingIssueAssert.hasCode(code: String) {
  isNotNull().transform { it.code }.isEqualTo(code)
}

