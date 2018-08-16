package io.mverse.jsonschema.loading

import assertk.Assert
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.fail
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.schemaReader
import kotlinx.serialization.json.JsonObject

typealias SchemaLoadingAssert = Assert<LoadingReport?>
typealias SafeSchemaLoadingAssert = Assert<LoadingReport>
typealias LoadingIssueAssert = Assert<LoadingIssue?>
typealias SafeLoadingIssueAssert = Assert<LoadingIssue>

fun JsonObject.assertAsSchema(reader: SchemaReader = JsonSchema.schemaReader(),
                              block:SafeSchemaLoadingAssert.()->Unit = {}): SchemaLoadingAssert {
  return try {
    reader.readSchema(this)
    assert(null as LoadingReport?)
  } catch (e: SchemaLoadingException) {
    return assert(e.report).apply(block)
  }
}

fun SchemaLoadingAssert.failedAt(schemaLocation:String? = null,
                               block: SafeLoadingIssueAssert.()->Unit = {}) {
  if (schemaLocation != null) {
    val found = actual?.issues?.firstOrNull {
      schemaLocation == it.location?.jsonPointerFragment?.toString()
    }
    if (found == null) {
      fail("No error found at location $schemaLocation, but found errors at ${actual?.issues?.map { it.location }}")
    } else {
      assert(found).block()
    }
  }
}

fun SchemaLoadingAssert.isFailed(errorCount:Int? = null, block: SafeSchemaLoadingAssert.()->Unit = {}) {
  if (actual?.hasErrors() != true) {
    fail("Unexpected success! Try harder to fail.")
  } else {
    assert(actual!!.issues.size, "error count").isEqualTo(errorCount)
    assert(actual!!).block()
  }
}

fun LoadingIssueAssert.isNotNull(block: SafeLoadingIssueAssert.()->Unit) {
  if (actual == null) {
    fail("Expected loading errors to not be null")
  } else {
    assert(actual!!).block()
  }
}

fun LoadingIssueAssert.hasLevel(errorLevel: LoadingIssueLevel) {
  isNotNull {
    assert(actual.level).isEqualTo(errorLevel)
  }
}

fun LoadingIssueAssert.hasCode(code: String)  {
  isNotNull {
    assert(actual.code).isEqualTo(code)
  }
}

