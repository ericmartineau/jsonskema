package io.mverse.jsonschema.integration

import assertk.assert
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertj.asserts.assertValidation
import io.mverse.jsonschema.assertj.asserts.hasErrorArguments
import io.mverse.jsonschema.assertj.asserts.hasErrorCode
import io.mverse.jsonschema.assertj.asserts.hasKeyword
import io.mverse.jsonschema.assertj.asserts.hasSchemaLocation
import io.mverse.jsonschema.assertj.asserts.hasViolationAt
import io.mverse.jsonschema.assertj.asserts.hasViolationsAt
import io.mverse.jsonschema.assertj.asserts.isNotValid
import io.mverse.jsonschema.assertj.asserts.validating
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.parseJsonObject
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaReader
import org.junit.Test

class EndToEndTest {

  @Test
  fun testParseAndValidate() {
    val primitives = JsonSchema.resourceLoader().getStream("primitives.json")
    val jsonSchema = JsonSchema.resourceLoader().getStream("mverse-account-profile.json")
    val jsonData = JsonSchema.resourceLoader().readJson("account-data.json").jsonObject
    val preloadedSchema = primitives.parseJsonObject()
    val loadedSchema = JsonSchema.schemaReader()
        .withPreloadedDocument(preloadedSchema)
        .readSchema(jsonSchema)
    assert(loadedSchema)
        .validating(jsonData)
        .isNotValid()
        .assertValidation { errors ->
          errors.hasViolationsAt("#/secondary_color", "#", "#/contact", "#/contact/email")
          errors.hasViolationAt("#/secondary_color")
              .hasKeyword(Keywords.PATTERN)
              .hasSchemaLocation("#/properties/secondary_color")
              .hasErrorCode("validation.keyword.pattern")
              .hasErrorArguments("badbadleroybrown", "^#?(?:(?:[0-9a-fA-F]{2}){3}|(?:[0-9a-fA-F]){3})$")
          errors.hasViolationAt("#/contact/email")
              .hasKeyword(Keywords.FORMAT)
              .hasErrorCode("validation.keyword.format")
          errors.hasViolationAt("#/contact")
              .hasErrorArguments("first_name")
          errors.hasViolationAt("#/contact")
              .hasErrorArguments("last_name")
          errors.hasViolationAt("#/contact")
              .hasErrorArguments("phone")
        }
  }
}
