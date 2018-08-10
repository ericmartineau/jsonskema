package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.resourceLoader
import org.junit.Test

class SchemaLoaderImplVersionTest {

  @Test
  fun testStrictDraft6_FailsForDraft4() {
    val schemaReader = JsonSchema.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft6)
    val draft4Schema = loader.readJsonObject("valid-draft4-schema.json")
    draft4Schema.assertAsSchema(schemaReader) {
      isFailed(errorCount = 2) {
        failedAt(schemaLocation = "#/id") {
          hasLevel(LoadingIssueLevel.ERROR)
          hasCode("keyword.notFound")
        }

        failedAt(schemaLocation = "#/additionalItems") {
          hasLevel(LoadingIssueLevel.ERROR)
          hasCode("keyword.type.mismatch")
        }
      }
    }
  }

  @Test
  fun testStrictDraft6_Successful() {
    val schemaReader = JsonSchema.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft6)
    val draft6Schema = loader.readJsonObject("valid-draft6-schema.json")
    val schema = schemaReader.readSchema(draft6Schema)
    assert(schema.id).hasToString("https://schema.org/valid-draft6.json")
  }

  @Test
  fun testStrictDraft4_Successful() {
    val schemaReader = JsonSchema.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft4)
    val draft4Schema = loader.readJsonObject("valid-draft4-schema.json")
    val schema = schemaReader.readSchema(draft4Schema)
    assert(schema.id).hasToString("https://schema.org/valid-draft4.json")
  }

  @Test(expected = SchemaLoadingException::class)
  fun testStrictDraft4_FailsForDraft6() {
    val draft4Reader = JsonSchema.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft4)
    val draft6 = loader.readJsonObject("valid-draft6-schema.json")
    try {
      val schema = draft4Reader.readSchema(draft6)
    } catch (e: SchemaLoadingException) {
      assert(e.report.issues).hasSize(2)

      val issue = e.report.issues.get(0)
      assert(issue.level).isEqualTo(LoadingIssueLevel.ERROR)
      assert(issue.location!!.jsonPointerFragment).hasToString("#/contains")
      assert(issue.code).isEqualTo("keyword.notFound")
      throw e
    }
  }

  companion object {
    internal val loader = JsonSchema.resourceLoader(SchemaLoaderImplVersionTest::class)
  }
}
