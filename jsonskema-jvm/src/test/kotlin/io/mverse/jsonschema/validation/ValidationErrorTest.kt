/*
 * Copyright (C) 2017 MVerse (http://mverse.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.mverse.jsonschema.validation

import assertk.assert
import assertk.assertAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.fail
import com.google.common.collect.Lists.newArrayList
import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.assertThat
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.loading.readFully
import io.mverse.jsonschema.resourceLoader
import io.mverse.jsonschema.schemaBuilder
import io.mverse.jsonschema.validation.ValidationMocks.mockBooleanSchema
import io.mverse.jsonschema.validation.ValidationMocks.mockNullSchema
import io.mverse.jsonschema.validation.ValidationTestSupport.expectSuccess
import io.mverse.jsonschema.validation.ValidationTestSupport.verifyFailure
import kotlinx.serialization.json.JSON
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.*
import java.util.Collections.emptyList

class ValidationErrorTest {

  private val loader = JsonSchema.resourceLoader()
  private val rootSchema = JsonSchema.schemaBuilder().build()

  @Test
  fun testConstructor() {
    val exc = createTestValidationError()
    Assert.assertEquals("#", exc.pathToViolation)
  }

  @Test
  fun testToJson() {
    val subject = ValidationError(
        violatedSchema = mockBooleanSchema.build(),
        pointerToViolation = JsonPath.parseFromURIFragment("#/a/b"),
        errorMessage = "exception message",
        keyword = Keywords.TYPE)

    val expected = loader.readJsonObject("exception-to-json.json")
    val actual = subject.toJson()
    assertEquals(expected, actual)
  }

  @Test
  fun testToJsonWithSchemaLocation() {
    val failedSchema = mockBooleanSchema("#/schema/location").build()
    val subject = ValidationError(
        violatedSchema = failedSchema,
        code = "code",
        errorMessage = "exception message",
        keyword = Keywords.TYPE,
        pointerToViolation = JsonPath.parseFromURIFragment("#/a/b"))

    val expected = loader.readJsonObject("exception-to-json-with-schema-location.json")
    val actual = subject.toJson()
    assert(actual).isEqualTo(expected)
  }

  @Test
  fun throwForMultipleFailures() {
    val failedSchema = mockNullSchema.build{}
    val input1 = ValidationError(
        violatedSchema = failedSchema,
        code = "code",
        errorMessage = "msg1",
        keyword = Keywords.TYPE,
        pointerToViolation = "#".toJsonPointer())

    val input2 = ValidationError(
        violatedSchema = failedSchema,
        code = "code",
        errorMessage = "msg2",
        keyword = Keywords.TYPE,
        pointerToViolation = "#".toJsonPointer())

    val e = ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), Arrays.asList(input1, input2))
    assert(e).isNotNull()

    Assert.assertSame(rootSchema, e!!.violatedSchema)
    Assert.assertEquals("#: 2 schema violations found", e.message)
    val causes = e.causes
    Assert.assertEquals(2, causes.size)
    Assert.assertSame(input1, causes[0])
    Assert.assertSame(input2, causes[1])
  }

  @Test
  fun throwForNoFailure() {
    expectSuccess { ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), emptyList()) }
  }

  @Test
  fun collectError_WhenSingleFailure_ThenFailureIsReturned() {
    val failedSchema = mockNullSchema.build()
    val input = ValidationError(
        violatedSchema = failedSchema,
        code = "code",
        errorMessage = "msg",
        keyword = Keywords.TYPE,
        pointerToViolation = "#".toJsonPointer())

    val actual = verifyFailure { ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), newArrayList(input)) }
    Assert.assertSame(input, actual)
  }

  @Test
  fun toJsonNullPointerToViolation() {
    val subject = ValidationError(
        violatedSchema = mockBooleanSchema.build(),
        code = "exception message",
        errorMessage = "msg",
        keyword = null,
        pointerToViolation = null)
    val actual = subject.toJson()

    actual["pointerToViolation"].assertThat().isEqualTo(JsonNull)
  }

  @Test
  fun toJsonWithCauses() {
    val cause = ValidationError(
        violatedSchema = mockNullSchema.build(),
        code = "code",
        messageTemplate = "cause msg %s",
        keyword = Keywords.TYPE,
        pointerToViolation = "#/a/0".toJsonPointer(),
        arguments = listOf("foo", "bar"))

    val subject = ValidationError(
        violatedSchema = mockNullSchema.build(),
        errorMessage = "exception message",
        keyword = null,
        causes = listOf(cause),
        pointerToViolation = "#/a".toJsonPointer())

    val expected = loader.readJsonObject("exception-to-json-with-causes.json")
    val actual = subject.toJson()
    assertEquals(expected, actual)
  }

  @Test
  fun toStringWithCauses() {
    val subject = subjectWithCauses(subjectWithCauses(subjectWithCauses(), subjectWithCauses()),
        subjectWithCauses())
    subject?.message?.assertThat()?.isEqualTo("#: 3 schema violations found")
  }

  @Test
  fun violationCountWithCauses() {
    val subject = subjectWithCauses(subjectWithCauses(), subjectWithCauses())
    subject?.violationCount?.assertThat()?.isEqualTo(2)
  }

  @Test
  fun violationCountWithNestedCauses() {
    val subject = subjectWithCauses(
        subjectWithCauses(),
        subjectWithCauses(subjectWithCauses(),
            subjectWithCauses(subjectWithCauses(), subjectWithCauses())))
    Assert.assertEquals(4, subject!!.violationCount)
  }

  @Test
  fun violationCountWithoutCauses() {
    subjectWithCauses()
        .assertThat { violationCount }
        .isEqualTo(1)
  }

  private fun createTestValidationError(): ValidationError {
    return ValidationError(
        violatedSchema = mockBooleanSchema.build(),
        code = "code",
        errorMessage = "Failed Validation",
        keyword = Keywords.TYPE,
        pointerToViolation = "#".toJsonPointer())
  }

  private fun createDummyException(pointer: String): ValidationError {
    return ValidationError(
        violatedSchema = mockBooleanSchema.build(),
        code = "code",
        errorMessage = "stuff went wrong",
        keyword = Keywords.TYPE,
        pointerToViolation = pointer.toJsonPointer())
  }

  private fun subjectWithCauses(vararg causes: ValidationError?): ValidationError? {
    return if (causes.isEmpty()) {
      ValidationError(
          violatedSchema = mockBooleanSchema.build(),
          code = "code",
          errorMessage = "Failure",
          keyword = Keywords.TYPE,
          pointerToViolation = "#".toJsonPointer())
    } else ValidationError.collectErrors(rootSchema, JsonPath.rootPath(), causes.filterNotNull().toList())
  }

  private fun String.toJsonPointer(): JsonPath = JsonPath.parseFromURIFragment(this)
}

fun assertk.Assert<JsonObject>.isEqualTo(other: JsonObject, path: String = "") {
  val prefix = if(!path.isBlank()) "" else "$path: "

  if (actual.keys != other.keys) {
    fail("${prefix}Key mismatch: Extra:${actual.keys.minus(other.keys)}, Missing:${other.keys.minus(actual.keys)}")
    return
  }

  assertAll {
    actual.forEach { (k, v) ->
      val otherAtKey = other[k]
      when {
        v is JsonObject && otherAtKey is JsonObject -> assert(v).isEqualTo(otherAtKey, "$path/$k")
        else -> assert(v, "Value for key '$k'").isEqualTo(otherAtKey)
      }
    }
  }
}
