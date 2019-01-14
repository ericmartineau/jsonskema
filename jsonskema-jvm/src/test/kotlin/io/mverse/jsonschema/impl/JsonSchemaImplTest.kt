package io.mverse.jsonschema.impl

import assertk.assert
import assertk.assertions.hasToString
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import io.mverse.jsonschema.Draft3Schema
import io.mverse.jsonschema.Draft4Schema
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.DollarSchemaKeyword.Companion.emptyUri
import io.mverse.jsonschema.keyword.Keywords.SCHEMA
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
      extraProperties += ("bobTheBuilder" to JsrTrue)

      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to jsrString("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to jsrString("FULL"))
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = true, version = JsonSchemaVersion.Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{\"theNestStatus\":\"FULL\"}},\"bobsType\":\"Chainsaw Murderer\"}},\"bobTheBuilder\":true}")
  }

  @Test
  fun testToStringNoExtraProperties() {
    val schema = JsonSchema.schema {
      extraProperties += ("bobTheBuilder" to JsrTrue)

      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to jsrString("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to jsrString("FULL"))
        }
      }
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = JsonSchemaVersion.Draft7)
    assert(schemaString).isEqualTo("{\"properties\":{\"childSchema\":{\"properties\":{\"grandchildSchema\":{}}}}}")
  }

  @Test
  fun testToStringFieldOrder() {
    val schema = JsonSchema.schema("https://something.com") {
      extraProperties += ("bobTheBuilder" to JsrTrue)

      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to jsrString("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to jsrString("FULL"))
        }
      }

      isUseSchemaKeyword = true
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = JsonSchemaVersion.Draft7, indent = true)
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

      extraProperties += ("bobTheBuilder" to JsrTrue)
      properties["childSchema"] = schemaBuilder {
        extraProperties += ("bobsType" to jsrString("Chainsaw Murderer"))

        properties["grandchildSchema"] = schemaBuilder {
          extraProperties += ("theNestStatus" to jsrString("FULL"))
        }
      }
      isUseSchemaKeyword = true
    }

    val schemaString = schema.toString(includeExtraProperties = false, version = JsonSchemaVersion.Draft7, indent = true)
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
}
