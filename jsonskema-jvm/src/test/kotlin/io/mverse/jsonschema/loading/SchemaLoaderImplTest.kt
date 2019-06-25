package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertAll
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.google.common.collect.ImmutableSet
import io.mverse.assertk.assertThrowing
import io.mverse.jsonschema.AllKeywords
import io.mverse.jsonschema.Draft6Schema
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.assertj.asserts.isSchemaEqual
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.keyword.JsrIterable
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.loading.reference.DefaultJsonDocumentClient
import io.mverse.jsonschema.resolver.FetchedDocument
import io.mverse.jsonschema.resolver.JsonDocumentFetcher
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schema
import io.mverse.jsonschema.validation.ValidationMocks
import io.mverse.logging.mlogger
import lang.exception.illegalState
import lang.json.jsrObject
import lang.net.URI
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test

class SchemaLoaderImplTest : BaseLoaderTest("testschemas.json") {

  @Test
  fun booleanSchema() {
    val actual = getSchemaForKey("booleanSchema")
    Assert.assertNotNull(actual)
  }

  @Test
  fun emptyPatternProperties() {
    val actual = getSchemaForKey("emptyPatternProperties")
    assertThat(actual).isNotNull()
  }

  @Test
  fun emptySchema() {
    val emptySchema = getSchemaForKey("emptySchema")

    assertThat(emptySchema).isNotNull { it: assertk.Assert<Draft6Schema> ->
      assertThat(it.actual.toString()).isEqualIgnoringWhitespace("{}")
    }
  }

  @Test
  fun emptySchemaWithDefault() {
    val emptySchema = getSchemaForKey("emptySchemaWithDefault")
    val actual = jsrObject {
      "default" *= 0
    }

    assertThat(emptySchema.toString()).isEqualIgnoringWhitespace(actual.toString())
  }

  @Test
  fun enumSchema() {
    val actual = getSchemaForKey("enumSchema")
    assertThat(actual.enumValues).isNotNull { it: assertk.Assert<JsrIterable> ->
      assertThat(it.actual.count(), "count").isEqualTo(4)
    }
  }

  @Test
  fun genericProperties() {
    val actual = getSchemaForKey("genericProperties")
    assertEquals("myId", actual.id!!.toString())
    assertEquals("my title", actual.title)
    assertEquals("my description", actual.description)
  }

  @Test
  fun implicitAnyOfLoadsTypeProps() {
    val schema = getSchemaForKey("multipleTypesWithProps")
    assertThat(schema.minLength!!.toDouble()).isEqualTo(3.0)
    assertThat(schema.minimum!!.toDouble()).isEqualTo(5.0)
  }

  @Test(expected = SchemaException::class)
  fun invalidExclusiveMinimum() {
    getSchemaForKey("invalidExclusiveMinimum")
  }

  @Test(expected = SchemaException::class)
  fun invalidNumberSchema() {
    val input = getJsonObjectForKey("invalidNumberSchema")
    JsonSchemas.createSchemaReader().readSchema(input)
  }

  @Test(expected = SchemaException::class)
  fun invalidStringSchema() {
    getSchemaForKey("invalidStringSchema")
  }

  @Test(expected = SchemaException::class)
  fun invalidType() {
    getSchemaForKey("invalidType")
  }

  @Test
  fun jsonPointerInArray() {
    val jsonSchema = getSchemaForKey("jsonPointerInArray")
    assertThat(jsonSchema.itemSchemas).hasSize(2)
    assertThat(jsonSchema.itemSchemas[1])
        .isInstanceOf(AllKeywords::class) {
          assertThat(it.actual.isRefSchema, "Is ref schema").isTrue()
        }
  }

  @Test
  fun multipleTypes() {
    val multipleTypes = getSchemaForKey("multipleTypes")
    assertThat(multipleTypes).isNotNull()
    assertThat(multipleTypes.types)
        .containsAll(JsonSchemaType.STRING, JsonSchemaType.BOOLEAN)
  }

  @Test
  fun neverMatchingAnyOf() {
    val anyOfNeverMatches = getSchemaForKey("anyOfNeverMatches")
    assertThat(anyOfNeverMatches.types)
        .isEqualTo(ImmutableSet.of(JsonSchemaType.STRING))
  }

  @Test
  fun noExplicitObject() {
    val actual = getSchemaForKey("noExplicitObject")
    assertThat(actual.types).isEmpty()
  }

  @Test
  fun notSchema() {
    val actual = getSchemaForKey("notSchema")
    assertThat(actual.notSchema).isNotNull()
  }

  @Test
  fun nullSchema() {
    val actual = getSchemaForKey("nullSchema")
    assertThat(actual).isNotNull()
  }

  @Test
  fun numberSchema() {
    val schema = getSchemaForKey("numberSchema")
    assertThat(schema.types).containsAll(JsonSchemaType.NUMBER)
    assertThat(schema.minimum!!.toInt()).isEqualTo(10)
    assertThat(schema.maximum!!.toInt()).isEqualTo(20)
    assertThat(schema.exclusiveMaximum!!.toInt()).isEqualTo(21)
    assertThat(schema.exclusiveMinimum!!.toInt()).isEqualTo(11)
    assertThat(schema.multipleOf?.toInt()).isEqualTo(5)
  }

  @Test
  fun pointerResolution() {
    val actual = getSchemaForKey("pointerResolution")

    val rectangleSchema = actual.properties.get("rectangle")?.draft6()
    assertThat(rectangleSchema).isNotNull().let { it: assertk.Assert<Draft6Schema> ->
      val schemaA = rectangleSchema!!.properties.get("a")?.draft6()
      assertThat(schemaA).isNotNull().let { it: assertk.Assert<Draft6Schema> ->
        assertThat(schemaA!!.minimum!!.toInt()).isEqualTo(0)
      }
    }
  }

  @Test
  fun pointerResolutionFailure() {
    val schema = getSchemaForKey("pointerResolutionFailure")
    assertThrowing<SchemaException> {
      val validator = ValidationMocks.createTestValidator(schema)
      validator.validate(jsrObject{})
    }
  }

  @Test
  fun pointerResolutionQueryFailure() {
    val schema = getSchemaForKey("pointerResolutionQueryFailure")
    assertThat(schema).isNotNull()
    assertThrowing<SchemaException> {
      val validator = ValidationMocks.createTestValidator(schema)
      validator.validate(jsrObject{})
    }
  }

  @Test
  fun recursiveSchema() {
    getSchemaForKey("recursiveSchema")
  }

  @Test
  fun refWithType() {
    val actualRoot = getSchemaForKey("refWithType")
    assertThat(actualRoot).isNotNull()
    val prop = actualRoot.properties.getValue("prop").draft7()
    assertThat(prop).isNotNull()
    assertThat(prop.requiredProperties).containsAll("a", "b")
  }

  @Test
  fun remotePointerResulion() {
    val client = DefaultJsonDocumentClient()
    val testURIs = setOf("http://example.org/asd", "http://example.org/otherschema.json",
        "http://example.org/folder/subschemaInFolder.json")
    client += object : JsonDocumentFetcher {
      override suspend fun fetchDocument(uri: URI): FetchedDocument {
        if (uri.toString() in testURIs) {
          return FetchedDocument(this::class, uri, uri, "{}")
        }
        illegalState("Don't know about this type")
      }
    }

    val factory = JsonSchemas.createSchemaReader().withDocumentClient(client)
    assert { factory.readSchema(getJsonObjectForKey("remotePointerResolution")) }.doesNotThrowAnyException()
  }

  @Test
  fun resolutionScopeTest() {
    val jsonDocumentClient = DefaultJsonDocumentClient()
    val factory = JsonSchemas.createSchemaReader().withDocumentClient(jsonDocumentClient)
    factory.readSchema(getJsonObjectForKey("resolutionScopeTest"))
  }

  @Test
  fun schemaJsonIdIsRecognized() {
    val client = DefaultJsonDocumentClient()
    val testURI = URI("http://example.org/schema/schema.json")
    client += object : JsonDocumentFetcher {
      override suspend fun fetchDocument(uri: URI): FetchedDocument {
        if (uri == testURI) {
          return FetchedDocument(this::class, testURI, testURI, "{}")
        }
        illegalState("Don't know about this type")
      }
    }

    val schemaWithId = getJsonObjectForKey("schemaWithId")
    val factory = JsonSchemas.createSchemaReader().withDocumentClient(client)
    factory.readSchema(schemaWithId)
  }

  @Test
  fun schemaPointerIsPopulated() {
    val rawSchema = JsonSchemas.resourceLoader(this::class).readJsonObject("objecttestschemas.json")
        .getJsonObject("objectWithSchemaDep")
    val actual = JsonSchemas.createSchemaReader().readSchema(rawSchema).draft6()

    assertAll {
      assertThat(actual).isNotNull()

      assertThat(actual.propertySchemaDependencies)
          .isNotEmpty()
      val actualSchemaPointer = actual.propertySchemaDependencies["a"]
          ?.location
          ?.jsonPointerFragment
          .toString()
      assertThat(actualSchemaPointer).isEqualTo("#/dependencies/a")
    }
  }

  @Test
  fun selfRecursiveSchema() {
    getSchemaForKey("selfRecursiveSchema")
  }

  @Test
  fun sniffByFormat() {
    val schemaJson = jsrObject { "format" *= "hostname" }
    val actual = JsonSchemas.createSchemaReader().readSchema(schemaJson).draft6()
    assertThat(actual.format).isEqualTo("hostname")
  }

  @Test
  fun stringSchema() {
    val actual = getSchemaForKey("stringSchema")
    assertThat(actual.minLength).isEqualTo(2)
    assertThat(actual.maxLength).isEqualTo(3)
  }

  @Test
  fun testCustomKeywordLoader() {

    //For testing, defines a keyword named "customKeyword" that's expected to be a string value
    val keyword = Keywords.stringKeyword("customKeyword").build()

    // This inputs a jsonvalue (from a document that's being read as a json-schema), and extracts a string keyword for the key "customKeyword"

    val loader = JsonSchemas.schemaReader + customKeyword(keyword) { jsonValue ->
      StringKeyword(jsonValue.string!!)
    }
    val inputJson = jsrObject {
      "customKeyword" *= "boomIsThePassword"
    }
    val schema = loader.readSchema(inputJson)
    assertThat(schema.keywords.get(keyword)).isEqualTo(StringKeyword("boomIsThePassword"))
  }

  @Test
  fun tupleSchema() {
    val actual = getSchemaForKey("tupleSchema")
    assertThat(actual.allItemSchema).isNull()
    assertThat(actual.itemSchemas).hasSize(2)
  }

  //todo:ericm Test nulls everywhere
  @Test(expected = SchemaException::class)
  fun unknownSchema() {
    getSchemaForKey("unknown")
  }

  @Test
  fun unsupportedFormat() {
    val schema = jsrObject {
      "type" *= "string"
      "format" *= "unknown"
    }
    JsonSchemas.schemaReader.readSchema(schema)
  }

  @Test
  fun testLoadedSchemaEquals() {
    val jsonA = "{\"\$id\":\"https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/showDog/jsonschema-draft6.json\",\"\$schema\":\"http://json-schema.org/draft-06/schema#\",\"properties\":{\"mostRecentShow\":{},\"awards\":{\"type\":\"array\",\"items\":{\"properties\":{\"awardDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"placement\":{\"type\":\"number\",\"minimum\":0},\"awardName\":{\"type\":\"string\"},\"show\":{}}}}}}"
    val jsonB = "{\"\$schema\":\"http://json-schema.org/draft-06/schema#\",\"\$id\":\"https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/showDog/jsonschema-draft6.json\",\"properties\":{\"mostRecentShow\":{},\"awards\":{\"type\":\"array\",\"items\":{\"properties\":{\"awardDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"placement\":{\"type\":\"number\",\"minimum\":0},\"awardName\":{\"type\":\"string\"},\"show\":{}}}}}}"
    val schemaA = JsonSchemas.schemaReader.readSchema(jsonA)
    val schemaB = JsonSchemas.schemaReader.readSchema(jsonB)
    assertThat(schemaA).isEqualTo(schemaB)
  }

  /**
   * Fixed an issue where serialization/deserialization would cause a Number instance to be
   * converted (property) from a floating point, eg 32.0 to 32.  But, this would cause the two
   * schemas to fail an equals comparison.
   */
  @Test
  fun testEqualsWithNumberPrecision() {
    val schemaOne = schema(id = URI("https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/dog/jsonschema-draft6.json")) {
      isUseSchemaKeyword = true
      properties["maxNumber"] = {
        minItems = 32
      }
    }

    val asString = schemaOne.draft7().toString(includeExtraProperties = true)

    val deserialized = JsonSchemas.createSchemaReader().readSchema(asString)
    assertThat(schemaOne).isSchemaEqual(deserialized)
  }

  companion object {
    val log = mlogger {}
  }
}
