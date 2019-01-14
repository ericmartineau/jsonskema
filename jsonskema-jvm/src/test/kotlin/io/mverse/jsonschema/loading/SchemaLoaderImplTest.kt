package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertAll
import assertk.assertions.containsAll
import assertk.assertions.hasSize
import assertk.assertions.hasToString
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.google.common.collect.ImmutableSet
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.RefSchema
import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.assertj.asserts.isEqualIgnoringWhitespace
import io.mverse.jsonschema.assertj.asserts.isSchemaEqual
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.StringKeyword
import io.mverse.jsonschema.loading.reference.DefaultJsonDocumentClient
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import kotlinx.serialization.json.json
import lang.json.jsrObject
import lang.net.URI
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy

class SchemaLoaderImplTest : BaseLoaderTest("testschemas.json") {

  @Test
  fun booleanSchema() {
    val actual = getSchemaForKey("booleanSchema")
    Assert.assertNotNull(actual)
  }

  @Test
  fun emptyPatternProperties() {
    val actual = getSchemaForKey("emptyPatternProperties")
    assert(actual).isNotNull()
  }

  @Test
  fun emptySchema() {
    val emptySchema = getSchemaForKey("emptySchema")

    assert(emptySchema).isNotNull {
      it.hasToString("{}")
    }
  }

  @Test
  fun emptySchemaWithDefault() {
    val emptySchema = getSchemaForKey("emptySchemaWithDefault")
    val actual = jsrObject {
      "default" *= 0
    }

    assert(emptySchema.toString()).isEqualIgnoringWhitespace(actual.toString())
  }

  @Test
  fun enumSchema() {
    val actual = getSchemaForKey("enumSchema")
    assert(actual.enumValues).isNotNull {
      it.hasSize(4)
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
    assert(schema.minLength!!.toDouble()).isEqualTo(3.0)
    assert(schema.minimum!!.toDouble()).isEqualTo(5.0)
  }

  @Test(expected = SchemaException::class)
  fun invalidExclusiveMinimum() {
    getSchemaForKey("invalidExclusiveMinimum")
  }

  @Test(expected = SchemaException::class)
  fun invalidNumberSchema() {
    val input = getJsonObjectForKey("invalidNumberSchema")
    JsonSchema.createSchemaReader().readSchema(input)
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
    assert(jsonSchema.itemSchemas).hasSize(2)
    assert(jsonSchema.itemSchemas.get(1))
        .isInstanceOf(RefSchema::class.java)
  }

  @Test
  fun multipleTypes() {
    val multipleTypes = getSchemaForKey("multipleTypes")
    assert(multipleTypes).isNotNull()
    assert(multipleTypes.types)
        .containsAll(JsonSchemaType.STRING, JsonSchemaType.BOOLEAN)
  }

  @Test
  fun neverMatchingAnyOf() {
    val anyOfNeverMatches = getSchemaForKey("anyOfNeverMatches")
    assert(anyOfNeverMatches.types)
        .isEqualTo(ImmutableSet.of(JsonSchemaType.STRING))
  }

  @Test
  fun noExplicitObject() {
    val actual = getSchemaForKey("noExplicitObject")
    assert(actual.types).isEmpty()
  }

  @Test
  fun notSchema() {
    val actual = getSchemaForKey("notSchema")
    assert(actual.notSchema).isNotNull()
  }

  @Test
  fun nullSchema() {
    val actual = getSchemaForKey("nullSchema")
    assert(actual).isNotNull()
  }

  @Test
  fun numberSchema() {
    val schema = getSchemaForKey("numberSchema")
    assert(schema.types).containsAll(JsonSchemaType.NUMBER)
    assert(schema.minimum!!.toInt()).isEqualTo(10)
    assert(schema.maximum!!.toInt()).isEqualTo(20)
    assert(schema.exclusiveMaximum!!.toInt()).isEqualTo(21)
    assert(schema.exclusiveMinimum!!.toInt()).isEqualTo(11)
    assert(schema.multipleOf?.toInt()).isEqualTo(5)
  }

  @Test
  fun pointerResolution() {
    val actual = getSchemaForKey("pointerResolution")

    val rectangleSchema = actual.properties.get("rectangle")?.asDraft6()
    assert(rectangleSchema).isNotNull {
      val schemaA = rectangleSchema!!.properties.get("a")?.asDraft6()
      assert(schemaA).isNotNull {
        assert(schemaA!!.minimum!!.toInt()).isEqualTo(0)
      }
    }
  }

  @Test(expected = SchemaException::class)
  fun pointerResolutionFailure() {
    getSchemaForKey("pointerResolutionFailure")
  }

  @Test(expected = SchemaException::class)
  fun pointerResolutionQueryFailure() {
    getSchemaForKey("pointerResolutionQueryFailure")
  }

  @Test
  fun recursiveSchema() {
    getSchemaForKey("recursiveSchema")
  }

  @Test
  fun refWithType() {
    val actualRoot = getSchemaForKey("refWithType")
    assert(actualRoot).isNotNull()
    val prop = actualRoot.getPropertySchema("prop")
    assert(prop).isNotNull()
    assert(prop.requiredProperties).containsAll("a", "b")
  }

  @Test
  fun remotePointerResulion() {
    val documentClient = Mockito.spy(DefaultJsonDocumentClient())

    doReturn(jsrObject {}).`when`(documentClient).fetchDocument(URI("http://example.org/asd"))
    doReturn(jsrObject {}).`when`(documentClient).fetchDocument(URI("http://example.org/otherschema.json"))
    doReturn(jsrObject {}).`when`(documentClient).fetchDocument(URI("http://example.org/folder/subschemaInFolder.json"))

    val factory = SchemaLoaderImpl().withDocumentClient(documentClient)
    factory.readSchema(getJsonObjectForKey("remotePointerResolution"))
  }

  @Test
  fun resolutionScopeTest() {
    val jsonDocumentClient = DefaultJsonDocumentClient()
    val factory = SchemaLoaderImpl().withDocumentClient(jsonDocumentClient)
    factory.readSchema(getJsonObjectForKey("resolutionScopeTest"))
  }

  @Test
  fun schemaJsonIdIsRecognized() {
    val client = spy(DefaultJsonDocumentClient())
    val retval = jsrObject {}
    doReturn(retval).`when`(client).fetchDocument("http://example.org/schema/schema.json")
    doReturn(retval).`when`(client).fetchDocument(URI("http://example.org/schema/schema.json"))
    val schemaWithId = getJsonObjectForKey("schemaWithId")
    val factory = SchemaLoaderImpl().withDocumentClient(client)
    factory.readSchema(schemaWithId)
  }

  @Test
  fun schemaPointerIsPopulated() {
    val rawSchema = JsonSchema.resourceLoader(this::class).readJsonObject("objecttestschemas.json")
        .getJsonObject("objectWithSchemaDep")
    val actual = JsonSchema.createSchemaReader().readSchema(rawSchema).asDraft6()

    assertAll {
      assert(actual).isNotNull()

      assert(actual.propertySchemaDependencies)
          .isNotEmpty()
      val actualSchemaPointer = actual.propertySchemaDependencies["a"]
          ?.location
          ?.jsonPointerFragment
          .toString()
      assert(actualSchemaPointer).isEqualTo("#/dependencies/a")
    }
  }

  @Test
  fun selfRecursiveSchema() {
    getSchemaForKey("selfRecursiveSchema")
  }

  @Test
  fun sniffByFormat() {
    val schemaJson = jsrObject { "format" *= "hostname" }
    val actual = JsonSchema.createSchemaReader().readSchema(schemaJson).asDraft6()
    assert(actual.format).isEqualTo("hostname")
  }

  @Test
  fun stringSchema() {
    val actual = getSchemaForKey("stringSchema")
    assert(actual.minLength).isEqualTo(2)
    assert(actual.maxLength).isEqualTo(3)
  }

  @Test
  fun testCustomKeywordLoader() {

    //For testing, defines a keyword named "customKeyword" that's expected to be a string value
    val keyword = Keywords.stringKeyword("customKeyword").build()

    // This inputs a jsonvalue (from a document that's being read as a json-schema), and extracts a string keyword for the key "customKeyword"

    val loader = JsonSchema.schemaReader + customKeyword(keyword) { jsonValue ->
      StringKeyword(jsonValue.string!!)
    }
    val inputJson = jsrObject {
      "customKeyword" *= "boomIsThePassword"
    }
    val schema = loader.readSchema(inputJson)
    assert(schema.keywords.get(keyword)).isEqualTo(StringKeyword("boomIsThePassword"))
  }

  @Test
  fun tupleSchema() {
    val actual = getSchemaForKey("tupleSchema")
    assert(actual.allItemSchema).isNull()
    assert(actual.itemSchemas).hasSize(2)
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
    JsonSchema.schemaReader.readSchema(schema)
  }

  @Test
  fun testLoadedSchemaEquals() {
    val jsonA = "{\"\$id\":\"https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/showDog/jsonschema-draft6.json\",\"\$schema\":\"http://json-schema.org/draft-06/schema#\",\"properties\":{\"mostRecentShow\":{},\"awards\":{\"type\":\"array\",\"items\":{\"properties\":{\"awardDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"placement\":{\"type\":\"number\",\"minimum\":0},\"awardName\":{\"type\":\"string\"},\"show\":{}}}}}}"
    val jsonB = "{\"\$schema\":\"http://json-schema.org/draft-06/schema#\",\"\$id\":\"https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/showDog/jsonschema-draft6.json\",\"properties\":{\"mostRecentShow\":{},\"awards\":{\"type\":\"array\",\"items\":{\"properties\":{\"awardDate\":{\"type\":\"string\",\"format\":\"date-time\"},\"placement\":{\"type\":\"number\",\"minimum\":0},\"awardName\":{\"type\":\"string\"},\"show\":{}}}}}}"
    val schemaA = JsonSchema.schemaReader.readSchema(jsonA)
    val schemaB = JsonSchema.schemaReader.readSchema(jsonB)
    assert(schemaA).isEqualTo(schemaB)
  }

  /**
   * Fixed an issue where serialization/deserialization would cause a Number instance to be
   * converted (property) from a floating point, eg 32.0 to 32.  But, this would cause the two
   * schemas to fail an equals comparison.
   */
  @Test
  fun testEqualsWithNumberPrecision() {
    val schemaOne = JsonSchema.schema("https://storage.googleapis.com/mverse-test/mverse/petStore/0.0.1/schema/dog/jsonschema-draft6.json") {
      isUseSchemaKeyword = true
      properties["maxNumber"] = {
        minItems = 32
      }
    }.asDraft7()

    val asString = schemaOne.toString(Draft7)

    val deserialized = JsonSchema.createSchemaReader().readSchema(asString)
    assert(schemaOne).isSchemaEqual(deserialized)
  }
}
