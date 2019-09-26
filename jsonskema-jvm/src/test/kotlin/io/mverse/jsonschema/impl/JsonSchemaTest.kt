package io.mverse.jsonschema.impl

import assertk.Assert
import assertk.assert
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.MergeActionType
import io.mverse.jsonschema.MergeReport
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.Keywords.CONST
import io.mverse.jsonschema.keyword.Keywords.DOLLAR_ID
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.utils.calculateJsonSchemaType
import lang.json.JsonPath
import lang.json.JsrTrue
import lang.json.jsrString
import lang.json.toJsonPath
import lang.net.URI
import org.junit.Test

class JsonSchemaTest {

  @Test
  fun testWithIdDraft7() {
    val theSchema = JsonSchemas.schema(id = "https://www.schema.org/foo") {}
    assertThat(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI)
    assertThat(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft6() {
    val theSchema: Draft6Schema = JsonSchemas.schema(id = "https://www.schema.org/foo") {}.draft6()
    assertThat(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).draft6()
    assertThat(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft4() {
    val theSchema: Draft4Schema = schema(id = "https://www.schema.org/foo") {}.draft4()
    assertThat(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).draft4()
    assertThat(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testWithIdDraft3() {
    val theSchema: Draft3Schema = schema(id = "https://www.schema.org/foo") {}.draft3()
    assertThat(theSchema.id).hasToString("https://www.schema.org/foo")
    val newURI = URI("https://google.com/hosted/foo/blah")
    val withId = theSchema.withId(newURI).draft3()
    assertThat(withId.id).isEqualTo(newURI)
  }

  @Test
  fun testToStringExtraProperties() {
    val schema = schema {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }
    }

    val schemaString = schema.draft7().toString(includeExtraProperties = true)
    assertThat(schemaString).isEqualIgnoringWhitespace("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{\"theNestStatus\":\"FULL\"}},\"bobsType\":\"Chainsaw Murderer\"}},\"bobTheBuilder\":true}")
  }

  @Test
  fun testGetOrNull() {
    val schema = JsonSchemas.schema {
      properties {
        "foo" required string
        "bar" required {
          definitions["rad"] = {

          }
          oneOfSchema {}
          allOfSchema {}
          anyOfSchema {}
          ifSchema {}
          thenSchema {}
          elseSchema {}
          containsSchema {}
          properties {
            "none" required {
              properties {
                "ya" required string
              }
            }
          }
        }
      }
    }
    assertAll {
      assertThat(schema.getOrNull("/properties/bar/properties/none".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/oneOf/0".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/anyOf/0".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/allOf/0".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/if".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/then".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/else".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/contains".toJsonPath())).isNotNull()
      assertThat(schema.getOrNull("/properties/bar/definitions/rad".toJsonPath())).isNotNull()
    }
  }

  @Test
  fun testToStringNoExtraProperties() {
    val schema = schema {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }
    }

    val schemaString = schema.draft7().toString(includeExtraProperties = false)
    assertThat(schemaString).isEqualIgnoringWhitespace("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{}}}}}")
  }

  @Test
  fun testToStringFieldOrder() {
    val schema = schema(id = "https://something.com/field-order") {
      extraProperties["bobTheBuilder"] = JsrTrue

      properties["childSchema"] = {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = {
          extraProperties["theNestStatus"] = jsrString("FULL")
        }
      }

      isUseSchemaKeyword = true
    }

    val schemaString = schema.draft7().toString(includeExtraProperties = false, indent = true)
    assertThat(schemaString.trim()).isEqualIgnoringWhitespace("{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "    \"\$id\": \"https://something.com/field-order\",\n" +
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
    val schema = JsonSchemas.schema(id = "https://something.com/custom-metaschema") {
      metaSchema = URI("http://custom-meta-schema.com")

      extraProperties["bobTheBuilder"] = JsrTrue
      properties["childSchema"] = {
        extraProperties["bobsType"] = jsrString("Chainsaw Murderer")

        properties["grandchildSchema"] = {
          extraProperties["theNestStatus"] to jsrString("FULL")
        }
      }
      isUseSchemaKeyword = true
    }

    val schemaString = schema.draft7().toString(includeExtraProperties = false, indent = true)
    assertThat(schemaString.trim()).isEqualIgnoringWhitespace("{\n" +
        "    \"\$schema\": \"http://custom-meta-schema.com\",\n" +
        "    \"\$id\": \"https://something.com/custom-metaschema\",\n" +
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
        "    \"\$id\": \"https://something.com/custom-metaschema\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"
    val readSchema = JsonSchemas.createSchemaReader().readSchema(schema).draft7()
    assertThat(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assertThat(readSchema.keywords.getValue(SCHEMA).value).isEqualTo(URI("http://custom-meta-schema.com"))
  }

  @Test fun testLoadStandardMetaSchema() {
    val schema = "{\n" +
        "    \"\$schema\": \"http://json-schema.org/draft-07/schema#\",\n" +
        "    \"\$id\": \"https://something.com/standard-metaschema\",\n" +
        "    \"properties\": {\n" +
        "        \"childSchema\": {\n" +
        "            \"properties\": {\n" +
        "                \"grandchildSchema\": {\n" +
        "                }\n" +
        "            }\n" +
        "        }\n" +
        "    }\n" +
        "}"
    val readSchema = JsonSchemas.createSchemaReader().readSchema(schema).draft7()
    assertThat(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assertThat(readSchema.keywords.getValue(SCHEMA).value).isEqualTo(emptyUri)
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
    val readSchema = JsonSchemas.createSchemaReader().readSchema(schema).draft7()
    assertThat(readSchema.keywords.containsKey(SCHEMA)).isTrue()
    assertThat(readSchema.keywords.get(SCHEMA)!!.value).isEqualTo(emptyUri)
  }

  @Test fun testMergeSchemas() {
    val schemaA = JsonSchemas.schema(id = "http://schemas/schemaA") {
      isUseSchemaKeyword = true
      properties {
        "name" required {
          minLength = 1
          oneOfSchema {
            type = STRING
            pattern = "^[A-Z]*$"
          }

          oneOfSchema {
            type = STRING
            pattern = "^[a-z]*$"
          }
        }
        "age" required string
        "address" required {
          properties {
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
      }
    }

    val schemaB = JsonSchemas.schema(id = "http://schemas/schemaB") {
      properties {
        "name" optional string {
          minLength = 3

          oneOfSchema {
            type = STRING
            const = "R2D2"
          }
          oneOfSchema {
            type = STRING
            pattern = "^[A-Za-z]$"
          }
        }
        "age" optional number {
          minimum = 1
        }
        "address" required {
          properties {
            "street1" required string
            "street2" optional string
            "state" optional string {
              maxLength = 2
            }
          }
        }
      }
    }

    val mergeReport = MergeReport()
    val mergedSchema = JsonSchemas.schemaMerger.merge(JsonPath.rootPath, schemaA, schemaB, mergeReport)

    assertThat(mergedSchema.absoluteURI, "Merged schema URI").isNotEqualTo(schemaA.absoluteURI)
    assertThat(mergedSchema.absoluteURI, "Merged schema uri").isNotEqualTo(schemaB.absoluteURI)

    val restricted = mergeReport.actions.firstOrNull {
      it.keyword == SCHEMA || it.keyword == DOLLAR_ID
    }

    assertThat(restricted, "restricted keyword").isNull()
    assertThat(mergeReport.isConflict).isTrue()
    assertThat(mergeReport)
        .contains(MergeActionType.CONFLICT, "/properties/name/minLength")
        .contains(MergeActionType.MERGE, "/required")
        .contains(MergeActionType.MERGE, "/properties/name/oneOf")
        .contains(MergeActionType.ADD, "/properties/address/properties/street2")
        .contains(MergeActionType.ADD, "/properties/age/minimum")
        .contains(MergeActionType.MERGE, "/properties/address/required")
        .contains(MergeActionType.MERGE, "/properties/age/type")
  }

  @Test fun testMergeSchemasDuringBuild() {
    val schemaA = JsonSchemas.schema(id = "http://schemas/schemaA") {
      isUseSchemaKeyword = true
      properties {
        "name" required {
          minLength = 1
          oneOfSchema {
            type = STRING
            pattern = "^[A-Z]*$"
          }

          oneOfSchema {
            type = STRING
            pattern = "^[a-z]*$"
          }
        }
        "age" required string
        "address" required {
          properties {
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
      }
    }

    val schemaB = JsonSchemas.schema(id = "http://schemas/schemaB") {
      baseSchema = schemaA
      definitions["copyOfA"] = {
        baseSchema = schemaA
        properties {
          "defined" required boolean
        }
      }
      properties {
        "name" optional string {
          minLength = 3

          oneOfSchema {
            type = STRING
            const = "R2D2"
          }
          oneOfSchema {
            type = STRING
            pattern = "^[A-Za-z]$"
          }
        }
        "age" optional number {
          minimum = 1
        }
        "address" required {
          properties {
            "street1" required string
            "street2" optional string
            "state" optional string {
              maxLength = 2
            }
          }
        }
      }
    }

    val mergedSchema = schemaB

    // Cache was updated appropriately
    val schemaBFromCache = JsonSchemas.schemaReader.readSchema(schemaURI = URI("http://schemas/schemaB"))
    assertThat(schemaBFromCache).isEqualTo(schemaB)

    assertThat(schemaBFromCache.draft7().properties["age"]?.draft7()?.types).isEqualTo(setOf(STRING,JsonSchemaType.NUMBER, JsonSchemaType.NULL))
    assertThat(schemaBFromCache.draft7()[JsonPath("/properties/address/properties/const")].draft7()).hasKeyword(CONST)
    assertThat(mergedSchema.absoluteURI, "Merged schema URI").isNotEqualTo(schemaA.absoluteURI)
    assertThat(mergedSchema.absoluteURI, "Merged schema uri").isEqualTo(schemaB.absoluteURI)
  }

  fun Assert<MergeReport>.contains(type: MergeActionType, path: String): Assert<MergeReport> {
    val partialMatch = this.actual.filter { it.path == JsonPath(path) }
    assert(partialMatch, "merge at $path").isNotEmpty()
    assert(partialMatch.first().type, "Type for merge at $path").isEqualTo(type)
    return this
  }
}

