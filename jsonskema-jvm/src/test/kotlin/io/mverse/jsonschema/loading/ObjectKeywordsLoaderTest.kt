package io.mverse.jsonschema.loading

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
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
    assertThat<String, Schema>(propertySchemas).isNotNull()
    assertThat<String, Schema>(propertySchemas).hasSize(2)
    val boolProp = actual.getPropertySchema("boolProp")
    assert(boolProp).isNotNull()

    assertThat<String>(actual.requiredProperties).hasSize(2)
    assert(actual.minProperties).isEqualTo(2)
    assert(actual.maxProperties).isEqualTo(3)
  }

  @Test(expected = SchemaException::class)
  fun objectInvalidAdditionalProperties() {
    getSchemaForKey("objectInvalidAdditionalProperties")
  }

  @Test
  fun objectWithAdditionalPropSchema() {
    val actual = getSchemaForKey("objectWithAdditionalPropSchema")
    val addtlPropSchema = actual.additionalPropertiesSchema
    assert(addtlPropSchema)
        .isEqualTo(BOOLEAN_SCHEMA)
  }

  @Test
  fun objectWithPropDep() {
    val actual = getSchemaForKey("objectWithPropDep")
    assert(actual.propertyDependencies.get("isIndividual"))
        .isNotNull {
          it.hasSize(1)
        }
  }

  @Test
  fun objectWithSchemaDep() {
    val actual = getSchemaForKey("objectWithSchemaDep")
    assertThat<String, Schema>(actual.propertySchemaDependencies).hasSize(1)
  }

  @Test
  fun patternProperties() {
    val actual = getSchemaForKey("patternProperties")
    assert(actual).isNotNull()
    assertThat<String, Schema>(actual.patternProperties).hasSize(2)
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
    val BOOLEAN_SCHEMA = JsonSchema.schema {
      type = JsonSchemaType.BOOLEAN
    }
  }
}
