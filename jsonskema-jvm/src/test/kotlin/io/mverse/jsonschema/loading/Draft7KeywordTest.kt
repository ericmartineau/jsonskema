package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.contains
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.assertj.asserts.hasIfSchema
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasProperty
import io.mverse.jsonschema.assertj.asserts.hasThenSchema
import io.mverse.jsonschema.assertj.asserts.hasValue
import io.mverse.jsonschema.assertj.asserts.isDraft7
import io.mverse.jsonschema.assertj.asserts.isReadOnly
import io.mverse.jsonschema.assertj.asserts.isWriteOnly
import io.mverse.jsonschema.assertj.asserts.withAssertion
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.enums.JsonSchemaVersion.Draft7
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.keyword.Keywords.COMMENT
import io.mverse.jsonschema.keyword.Keywords.CONTENT_ENCODING
import io.mverse.jsonschema.keyword.Keywords.CONTENT_MEDIA_TYPE
import io.mverse.jsonschema.keyword.Keywords.PATTERN
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.createSchemaReader
import org.junit.Before
import org.junit.Test

/**
 * @author ericm
 */
class Draft7KeywordTest {

  private lateinit var schema: Schema

  @Before
  fun readSchema() {
    val schemaReader = JsonSchema.createSchemaReader().withStrictValidation(Draft7)
    val loader = JsonSchema.resourceLoader(this::class)
    val schemaObject = loader.readJsonObject("/draft7-keywords.json")
    this.schema = schemaReader.readSchema(schemaObject)
  }

  @Test
  fun readOnlyKeywordIsLoaded() {
    assert(schema).isDraft7()
        .hasProperty("readMe")
        .isReadOnly()
  }

  @Test
  fun writeOnlyKeywordIsLoaded() {

    assert(schema).isDraft7()
        .hasProperty("writeMe")
        .isWriteOnly()
  }

  @Test
  fun ifKeywordIsLoaded() {
    assert(schema).isDraft7()
        .hasProperty("ifTest")
        .hasIfSchema()
        .hasKeyword(Keywords.TYPE)
        .withAssertion { type ->
          assert(type).isNotNull {
            it.contains(STRING)
          }
        }
  }

  @Test
  fun nestedifKeywordIsLoaded() {
    assert(schema).isDraft7()
        .hasProperty("ifTest")
        .hasThenSchema()
        .hasIfSchema()
        .hasKeyword(PATTERN)
  }

  @Test
  fun commentIsLoaded() {
    assert(schema).isDraft7()
        .hasKeyword(COMMENT)
        .hasValue("This is cool")
  }

  @Test
  fun contentEncodingIsLoaded() {
    assert(schema).isDraft7()
        .hasProperty("base64")
        .hasKeyword(CONTENT_ENCODING)
        .hasValue("base64")
  }

  @Test
  fun contentMediaTypeIsLoaded() {
    assert(schema).isDraft7()
        .hasProperty("base64")
        .hasKeyword(CONTENT_MEDIA_TYPE)
        .hasValue("image/png")
  }
}
