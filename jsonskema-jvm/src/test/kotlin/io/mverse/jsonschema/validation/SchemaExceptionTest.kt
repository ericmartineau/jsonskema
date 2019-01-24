package io.mverse.jsonschema.validation

import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.enums.JsonSchemaType.NULL
import io.mverse.jsonschema.enums.JsonSchemaType.NUMBER
import io.mverse.jsonschema.enums.JsonSchemaType.STRING
import io.mverse.jsonschema.validation.TestErrorHelper.failure
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * @author ericmartineau
 */
class SchemaExceptionTest {

  @Rule
  @JvmField
  val expExc = ExpectedException.none()

  @Test
  fun nullWithMessage() {
    val schema = JsonSchema.schemaBuilder("#/required/2") { type = (NULL) }.build()
    val actual = failure(schema, STRING, NULL).message
    assertEquals("#/required/2: expected type: string, found: null", actual)
  }

  @Test
  fun testBuildMessageSingleExcType() {
    val actual = failure(NULL_SCHEMA, NUMBER, STRING).resolvedMessage
    assertEquals("expected type: number, found: string", actual)
  }

  companion object {
    private val NULL_SCHEMA = JsonSchema.schema { type = (NULL) }
  }
}
