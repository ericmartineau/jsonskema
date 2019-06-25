package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.resourceLoader
import org.junit.Test

class SchemaLoaderImplVersionTest {

  @Test
  fun testStrictDraft6_FailsForDraft4() {
    val schemaReader = JsonSchemas.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft6)
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
    val schemaReader = JsonSchemas.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft6)
    val draft6Schema = loader.readJsonObject("valid-draft6-schema.json")
    val schema = schemaReader.readSchema(draft6Schema)
    assertThat(schema.id).hasToString("https://schema.org/valid-draft6.json")
  }

  @Test
  fun testStrictDraft4_Successful() {
    val schemaReader = JsonSchemas.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft4)
    val draft4Schema = loader.readJsonObject("valid-draft4-schema.json")
    val schema = schemaReader.readSchema(draft4Schema)
    assertThat(schema.id).hasToString("https://schema.org/valid-draft4.json")
  }

  @Test(expected = SchemaLoadingException::class)
  fun testStrictDraft4_FailsForDraft6() {
    val draft4Reader = JsonSchemas.createSchemaReader().withStrictValidation(JsonSchemaVersion.Draft4)
    val draft6 = loader.readJsonObject("valid-draft6-schema.json")
    try {
      draft4Reader.readSchema(draft6)
    } catch (e: SchemaLoadingException) {
      assertThat(e.report.issues).hasSize(2)

      val issue = e.report.issues.get(0)
      assertThat(issue.level).isEqualTo(LoadingIssueLevel.ERROR)
      assertThat(issue.location!!.jsonPointerFragment).hasToString("#/contains")
      assertThat(issue.code).isEqualTo("keyword.notFound")
      throw e
    }
  }

  companion object {
    internal val loader = JsonSchemas.resourceLoader(SchemaLoaderImplVersionTest::class)
  }
}
