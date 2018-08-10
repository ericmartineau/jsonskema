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
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import io.mverse.jsonschema.JsonSchema
import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.ValidationMocks
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.createValidatorFactory
import io.mverse.jsonschema.getValidator
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import lang.json.toJsonLiteral
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import java.util.*

typealias ValidationErrorConsumer= (ValidationError)->Unit
typealias ValidationErrorPredicater= (ValidationError)->Boolean

object ValidationTestSupport {

  fun buildWithLocation(builder: SchemaBuilder<*>): Schema {
    return builder.build()
  }

  fun countCauseByJsonPointer(root: ValidationError, pointer: String): Long {
    return root.causes
        ?.map { it.pathToViolation }
        ?.filter { ptr -> ptr.equals(pointer) }
        ?.count()
        ?.toLong() ?: 0
  }

  fun countMatchingMessage(messages: List<ValidationError>, expectedSubstring: String): Long {
    return messages.stream()
        .filter { message -> message.message.contains(expectedSubstring) }
        .count()
  }

  fun expectFailure(failingSchema: Schema,
                    expectedViolatedSchemaClass: Class<out Schema>,
                    expectedPointer: String, input: JsonElement) {

    val errors = test(failingSchema, expectedPointer, input)
    assert(errors).isNotNull()
    assert(errors!!.violatedSchema).isNotNull {
      it.isInstanceOf(expectedViolatedSchemaClass)
    }
  }

  fun expectFailure(failingSchema: Schema, num: Double) {
    expectFailure(failingSchema, null, num.toJsonLiteral())
  }

  fun expectFailure(failingSchema: Schema, input: JsonElement) {
    expectFailure(failingSchema, null, input)
  }

  fun expectFailure(failingSchema: Schema,
                    expectedViolatedSchema: Schema,
                    expectedPointer: String?, input: JsonElement) {

    val errors = test(failingSchema, expectedPointer, input)
    assert(errors).isNotNull()
    assert(errors!!.violatedSchema, "Matching violation schemas")
        .isEqualTo(expectedViolatedSchema)
  }

  fun expectFailure(failingSchema: Schema, expectedPointer: String?,
                    input: JsonElement) {
    expectFailure(failingSchema, failingSchema, expectedPointer, input)
  }

  fun expectFailure(failure: Failure): ValidationError {
    val validator = failure.validator()
        .orElse(ValidationMocks.createTestValidator(failure.schema()!!))
    if (failure.input() == null) {
      throw RuntimeException("Invalid test configuration.  Must provide input value")
    }
    val error = validator.validate(failure.input()!!)
    assert(error, failure.schema().toString() + " did not fail for " + failure.input()).isNotNull()

    error!!
    if (failure.expected() != null) {
      assert(failure.expected()!!.invoke(error), "Predicate failed validation").isTrue()
    }
    failure.expectedConsumer()?.invoke(error)
    assertEquals("Expected violated schema", failure.expectedViolatedSchema(), error.violatedSchema)
    assertEquals("Pointer to violation", failure.expectedPointer(), error.pathToViolation)
    assertEquals("Schema location", failure.expectedSchemaLocation(), error.schemaLocation.toString())
    if (failure.expectedKeyword() != null) {
      assertNotNull("Error expected to have a keyword, but didn't", error.keyword)
      assertEquals(failure.expectedKeyword(), error.keyword?.key)
    }
    if (failure.expectedMessageFragment() != null) {
      assert(error.message, "Message fragment matches").contains(failure.expectedMessageFragment()!!)
    }

    return error
  }

  fun failureOf(validator: SchemaValidator, schema: Schema): Failure {
    return Failure().schema(schema).validator(validator)
  }

  fun failureOf(validator: SchemaValidator): Failure {
    return Failure().schema(validator.schema).validator(validator)
  }

  fun failureOf(schema: Schema): Failure {
    return Failure().schema(schema)
  }

  fun failureOf(subjectBuilder: SchemaBuilder<*>): Failure {
    return failureOf(buildWithLocation(subjectBuilder))
  }

  private fun test(failingSchema: Schema, expectedPointer: String?,
                   input: JsonElement): ValidationError? {

    val error = JsonSchema.getValidator(failingSchema).validate(input)
    assert(error, failingSchema.toString() + " did not fail for " + input).isNotNull()
    if (expectedPointer != null) {
      assertEquals(expectedPointer, error!!.pathToViolation)
    }
    return error
  }

  class Failure {

    private var subject: Schema? = null
    private var validator: SchemaValidator? = null
    private var expectedViolatedSchema: Schema? = null
    private var expectedPointer = "#"
    private var expectedSchemaLocation = "#"
    private var expectedKeyword: String? = null
    private var input: JsonElement? = null
    private var expectedMessageFragment: String? = null
    private var expectedPredicate: ValidationErrorPredicate? = null
    private var expectedConsumer: ValidationErrorConsumer? = null

    fun expect() {
      expectFailure(this)
    }

    fun expectedKeyword(keyword: KeywordInfo<*>): Failure {
      this.expectedKeyword = keyword.key
      return this
    }

    fun expectedKeyword(keyword: String): Failure {
      this.expectedKeyword = keyword
      return this
    }

    fun expectedKeyword(): String? {
      return expectedKeyword
    }

    fun expected(): ValidationErrorPredicate? {
      return expectedPredicate
    }

    fun expectedConsumer(): ValidationErrorConsumer? {
      return expectedConsumer
    }

    fun expected(expected: ValidationErrorPredicate): Failure {
      this.expectedPredicate = expected
      return this
    }

    fun expectedConsumer(expected: ValidationErrorConsumer): Failure {
      this.expectedConsumer = expected
      return this
    }

    fun expectedMessageFragment(): String? {
      return expectedMessageFragment
    }

    fun expectedMessageFragment(expectedFragment: String): Failure {
      this.expectedMessageFragment = expectedFragment
      return this
    }

    fun expectedPointer(expectedPointer: String): Failure {
      this.expectedPointer = expectedPointer
      return this
    }

    fun expectedPointer(): String {
      return expectedPointer
    }

    fun expectedSchemaLocation(expectedSchemaLocation: String): Failure {
      this.expectedSchemaLocation = expectedSchemaLocation
      return this
    }

    fun expectedSchemaLocation(): String {
      return expectedSchemaLocation
    }

    fun expectedViolatedSchema(expectedViolatedSchema: Schema): Failure {
      this.expectedViolatedSchema = expectedViolatedSchema
      return this
    }

    fun expectedViolatedSchema(): Schema? {
      return if (expectedViolatedSchema != null) {
        expectedViolatedSchema
      } else subject
    }

    fun nullInput(): Failure {
      this.input = JsonNull
      return this
    }

    fun input(input: String): Failure {
      this.input = input.toJsonLiteral()
      return this
    }

    fun input(input: Boolean): Failure {
      this.input = input.toJsonLiteral()
      return this
    }

    fun input(i: Int): Failure {
      this.input = i.toJsonLiteral()
      return this
    }

    fun input(input: JsonElement): Failure {
      this.input = input
      return this
    }

    fun input(): JsonElement? {
      return input
    }

    fun schema(subject: Schema): Failure {
      this.subject = subject
      return this
    }

    fun validator(validator: SchemaValidator): Failure {
      this.validator = validator
      return this
    }

    fun schema(): Schema? {
      return subject
    }

    fun validator(): Optional<SchemaValidator> {
      return Optional.ofNullable(validator)
    }
  }

  fun verifyFailure(validationFn: ()->ValidationError?): ValidationError {
    val error = validationFn()
    assert(error, "Should have failed").isNotNull()
    return error!!
  }

  fun expectSuccess(validationFn: ()->ValidationError?) {
    val error = validationFn()
    assert(error, "Should have succeeded: $error").isNull()
  }

  fun expectSuccess(schema: Schema, input: Long) {
    expectSuccess(schema, input.toJsonLiteral())
  }

  fun expectSuccess(schema: Schema, input: Double) {
    expectSuccess(schema, input.toJsonLiteral())
  }

  fun expectSuccess(schema: Schema, input: String) {
    expectSuccess(schema, input.toJsonLiteral())
  }

  fun expectSuccess(schema: Schema, input: Boolean) {
    expectSuccess(schema, input.toJsonLiteral())
  }

  fun expectSuccess(schema: Schema, input: JsonElement) {
    val error = JsonSchema.createValidatorFactory().createValidator(schema).validate(input)
    assert(error, "Found validation: " + error.toString()).isNull()
  }
}
