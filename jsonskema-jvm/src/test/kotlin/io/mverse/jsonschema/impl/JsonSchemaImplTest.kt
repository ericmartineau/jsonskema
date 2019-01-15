package io.mverse.jsonschema.impl

import assertk.Assert
import assertk.assert
import assertk.assertAll
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isTrue
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.MergeActionType
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import lang.json.JsonPath
import lang.json.JsrTrue
import lang.json.jsrString
import lang.net.URI
import org.junit.Test

class JsonSchemaImplTest {

  @Test
  fun testWithIdDraft7() {
    val theSchema = JsonSchema.schema("https://www.schema.org/foo")
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI)
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft6() {
    val theSchema: Draft6Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft6()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft6()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft4() {
    val theSchema: Draft4Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft4()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft4()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft3() {
    val theSchema: Draft3Schema = JsonSchema.schema("https://www.schema.org/foo").asDraft3()
    assert(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).asDraft3()
    assert(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testToStringExtraProperties() {
    val schema = JsonSchema.schema {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = schemaBuilder {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = true, version = Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{\"theNestStatus\":\"FULL\"}},\"bobsType\":\"Chainsaw Murderer\"}},\"bobTheBuilder\":true}")
  }

  @Test
  fun testToStringNoExtraProperties() {
    val schema = JsonSchema.schema {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = schemaBuilder {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{}}}}}")
  }

  @Test
  fun testToStringFieldOrder() {
    val schema = JsonSchema.schema("https://something.com") {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = schemaBuilder {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }

      isUseSchemaKeyword = true
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = Draft7, indent = true)
    assert(schemaString.trim()).isEqualIgnoringWhitespace("{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "    \"\$id\": \"https://something.com\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}")
  }

  @Test
  fun testToStringCustomSchema() {
    val schema = JsonSchema.schema("https://something.com") {
      metaSchema = URI("http://custom-meta-schema.com")

      extraProperties["bobTheBuilder"] = JsrTrue
      properties["childSchema"] = schemaBuilder {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties["theNestStatus"] to jsrString("FULL")
        }
      }
      isUseSchemaKeyword = true
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = Draft7, indent = true)
    assert(schemaString.trim()).isEqualIgnoringWhitespace("{\n" +
        "    \"\$schema\": \"http://custom-meta-schema.com\",\n" +
        "    \"\$id\": \"https://something.com\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}")
  }

  @Test fun testLoadCustomMetaSchema() {
    val schema = "{\n" +
        "    \"\$schema\": \"http://custom-meta-schema.com\",\n" +
        "    \"\$id\": \"https://something.com\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"
    val readSchema = JsonSchema.createSchemaReader().readSchema(schema).asDraft7()
    assert(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assert(readSchema.keywords.get(SCHEMA)!!.value).isEqualTo(URI("http://custom-meta-schema.com"))
  }

  @Test fun testLoadStandardMetaSchema() {
    val schema = "{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "    \"\$id\": \"https://something.com\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"
    val readSchema = JsonSchema.createSchemaReader().readSchema(schema).asDraft7()
    assert(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assert(readSchema.keywords.get(SCHEMA)!!.value).isEqualTo(emptyUri)
  }

  @Test fun testLoadStandardMetaSchemaNoFragment() {
    val schema = "{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft-07/schema\",\n" +
        "    \"\$id\": \"https://something.com\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"
    val readSchema = JsonSchema.createSchemaReader().readSchema(schema).asDraft7()
    assert(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assert(readSchema.keywords.get(SCHEMA)!!.value).isEqualTo(emptyUri)
  }

  @Test fun testMergeSchemas() {
    val schemaA = JsonSchema.schema {

      "name" required schemaBuilder {
        minLength = 1
        oneOfSchemas += schemaBuilder {
          type = STRING
          pattern = "^[A-Z]*$"
        }

        oneOfSchemas += schemaBuilder {
          type = STRING
          pattern = "^[a-z]*$"
        }
      }
      "age" required string
      "address" required schemaBuilder {
        "street1" required string {
          minLength = 10
        }
        "number" required number {
          exclusiveMaximum = 10
        }
        "const" required string {
          const = "10"
        }
      }
    }

    val schemaB = JsonSchema.schema {
      "name" optional string {
        minLength = 3

        oneOfSchemas += schemaBuilder {
          type = STRING
          const = "R2D2"
        }
        oneOfSchemas += schemaBuilder {
          type = STRING
          pattern = "^[A-Za-z]$"
        }
      }
      "age" optional number {
        minimum = 1
      }
      "address" required schemaBuilder {
        "street1" required string
        "street2" optional string
        "state" optional string {
          maxLength = 2
        }
      }
    }

    val mergeReport = MergeReport()
    val mergedSchema = schemaA.merge(JsonPath.rootPath, schemaB, mergeReport)

    assertAll {
      assert(mergeReport.isConflict).isTrue()
      assert(mergeReport)
          .contains(MergeActionType.CONFLICT, "/properties/name/minLength")
          .contains(MergeActionType.MERGE, "/required")
          .contains(MergeActionType.MERGE, "/properties/name/oneOf")
          .contains(MergeActionType.ADD, "/properties/address/properties/street2")
          .contains(MergeActionType.ADD, "/properties/age/minimum")
          .contains(MergeActionType.MERGE, "/properties/address/required")
          .contains(MergeActionType.MERGE, "/properties/age/type")
    }

    val mergedSchemaString = mergedSchema.toString(Draft7, includeExtraProperties = true, indent = true)
  }

  fun Assert<MergeReport>.contains(type: MergeActionType, path: String): Assert<MergeReport> {
    val partialMatch = this.actual.filter { it.path == JsonPath(path) }
    assert(partialMatch, "merge at $path").isNotEmpty()
    assert(partialMatch.first().type, "Type for merge at $path").isEqualTo(type)
    return this
  }
}

