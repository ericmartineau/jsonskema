package io.mverse.jsonschema.loading

import assertk.Assert
import assertk.assert
import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchemas
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.assertj.asserts.hasIfSchema
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasProperty
import io.mverse.jsonschema.assertj.asserts.hasThenSchema
import io.mverse.jsonschema.assertj.asserts.hasValue
import io.mverse.jsonschema.assertj.asserts.isReadOnly
import io.mverse.jsonschema.assertj.asserts.isWriteOnly
import io.mverse.jsonschema.assertj.asserts.withAssertion
import io.mverse.jsonschema.createSchemaReader
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.resourceLoader
import org.junit.Before
import org.junit.Test

/**
 * @author ericm
 */
class Draft7KeywordTest {

  private lateinit var schema: Schema

  @Before
  fun readSchema() {
    val schemaReader = JsonSchemas.createSchemaReader().withStrictValidation(Draft7)
    val loader = JsonSchemas.resourceLoader(this::class)
    val schemaObject = loader.readJsonObject("/draft7-keywords.json")
    this.schema = schemaReader.readSchema(schemaObject)
  }

  @Test
  fun readOnlyKeywordIsLoaded() {
    assertThat(schema)
        .hasProperty("readMe")
        .isReadOnly()
  }

  @Test
  fun writeOnlyKeywordIsLoaded() {
    assertThat(schema)
        .hasProperty("writeMe")
        .isWriteOnly()
  }

  @Test
  fun ifKeywordIsLoaded() {
    assertThat(schema)
        .hasProperty("ifTest")
        .hasIfSchema()
        .hasKeyword(Keywords.TYPE)
        .withAssertion { type:Set<JsonSchemaType>? ->
          assertThat(type).isNotNull { it: Assert<Set<JsonSchemaType>> ->
            it.contains(STRING)
          }
        }
  }

  @Test
  fun nestedifKeywordIsLoaded() {
    assertThat(schema)
        .hasProperty("ifTest")
        .hasThenSchema()
        .hasIfSchema()
        .hasKeyword(PATTERN)
  }

  @Test
  fun commentIsLoaded() {
    assertThat(schema)
        .hasKeyword(COMMENT)
        .hasValue("This is cool")
  }

  @Test
  fun contentEncodingIsLoaded() {
    assertThat(schema)
        .hasProperty("base64")
        .hasKeyword(CONTENT_ENCODING)
        .hasValue("base64")
  }

  @Test
  fun contentMediaTypeIsLoaded() {
    assertThat(schema)
        .hasProperty("base64")
        .hasKeyword(CONTENT_MEDIA_TYPE)
        .hasValue("image/png")
  }
}
