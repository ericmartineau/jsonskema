package io.mverse.jsonschema.loading

import io.mverse.jsonschema.SchemaException
import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.schema
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * @author erosb
 */
class ObjectKeywordsLoaderTest : BaseLoaderTest("objecttestschemas.json") {

  @Test
  fun objectSchema() {
    val actual = getSchemaForKey("objectSchema")

    val propertySchemas = actual.properties
    assertThat(propertySchemas).isNotNull
    assertThat(propertySchemas).hasSize(2)
    val boolProp = actual.properties["boolProp"]
    assertThat(boolProp).isNotNull

    assertThat(actual.requiredProperties).hasSize(2)
    assertThat(actual.minProperties).isEqualTo(2)
    assertThat(actual.maxProperties).isEqualTo(3)
  }

  @Test(expected = SchemaException::class)
  fun objectInvalidAdditionalProperties() {
    getSchemaForKey("objectInvalidAdditionalProperties")
  }

  @Test
  fun objectWithAdditionalPropSchema() {
    val actual = getSchemaForKey("objectWithAdditionalPropSchema")
    val addtlPropSchema = actual.additionalPropertiesSchema
    assertThat(addtlPropSchema)
        .isEqualTo(BOOLEAN_SCHEMA)
  }

  @Test
  fun objectWithPropDep() {
    val actual = getSchemaForKey("objectWithPropDep")
    assertThat(actual.propertyDependencies.get("isIndividual"))
        .isNotNull.hasSize(1)
  }

  @Test
  fun objectWithSchemaDep() {
    val actual = getSchemaForKey("objectWithSchemaDep")
    assertThat(actual.propertySchemaDependencies).hasSize(1)
  }

  @Test
  fun patternProperties() {
    val actual = getSchemaForKey("patternProperties")
    assertThat(actual).isNotNull
    assertThat(actual.patternProperties).hasSize(2)
  }

  @Test(expected = SchemaException::class)
  fun invalidDependency() {
    getSchemaForKey("invalidDependency")
  }

  @Test
  fun emptyDependencyList() {
    getSchemaForKey("emptyDependencyList")
  }

  companion object {
    val BOOLEAN_SCHEMA = schema {
      type = JsonSchemaType.BOOLEAN
    }
  }
}
