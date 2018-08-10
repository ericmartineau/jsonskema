package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.all
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.key
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.argumentsContainsAll
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.codeEquals
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.pointerToViolationEquals
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.schemaLocationEquals
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.keyword.SchemaKeyword
import io.mverse.jsonschema.validation.ValidationError
import lang.URI

typealias SchemaValidationAssert = Assert<ValidationError?>
typealias ValidationListAssert = Assert<List<ValidationError>>

//  private val errors: List<ValidationError>
//  private val flattened: List<ValidationError>

fun SchemaValidationAssert.isValid(): SchemaValidationAssert {
  assert(this.actual, "violations").isNull()
  return this
}

fun SchemaValidationAssert.isNotValid(): SchemaValidationAssert {
  assert(this.actual, "should have failures, but didn't").isNotNull {
    assert(it.actual.allMessages.size, "should have at least one violation").isGreaterThan(0)
  }
  return this
}

//  constructor(errors: List<ValidationError>) : super(stream(errors)
//      .flatCollection(???(
//  { it.getAllMessages() }))
//  .toImmutableList())
//  {
//    this.errors = ImmutableList.copyOf(errors)
//    this.flattened = ImmutableList.copyOf(this.actual)
//  }

//  constructor(errors: ValidationErrors) : super(stream(checkNotNull(errors, "errors").actual())
//      .flatCollection(???(
//  { it.getAllMessages() }))
//  .toImmutableList())
//  {
//    this.errors = ImmutableList.copyOf(errors.actual())
//    this.flattened = ImmutableList.copyOf(this.actual)
//  }
//
//  constructor(rootError: Optional<ValidationError>) : this(rootError
//      .map<List<ValidationError>>(Function<ValidationError, List<ValidationError>> { listOf(it) })
//      .orElse(emptyList<ValidationError>())) {
//  }

fun SchemaValidationAssert.hasViolationCount(expected: Int): SchemaValidationAssert {
  assert(this.actual, "violations").isNotNull {
    assert(it.actual.allMessages.size, "violation count").isEqualTo(expected)
  }
  return this
}

fun SchemaValidationAssert.assertValidation(asserter: (SchemaValidationAssert) -> Unit) {
  this.all {
    asserter(this)
  }
}

fun SchemaValidationAssert.hasViolationsAt(vararg paths: String): SchemaValidationAssert {
  val expected = paths.toList()
  val actual = this.actual?.allMessages
      ?.map { it.pathToViolation }
      ?.filterNotNull()
      ?.distinct()
      ?.toSet() ?: emptySet()
  assert(actual, "violations").containsAll(*expected.toTypedArray())
  return this
}

fun SchemaValidationAssert.hasErrorCode(errorCode: String): SchemaValidationAssert {
  return filter(codeEquals(errorCode))
}

fun SchemaValidationAssert.hasViolationAt(pointerToViolation: String): SchemaValidationAssert {
  return filter(pointerToViolationEquals(pointerToViolation))
}

fun SchemaValidationAssert.hasErrorArguments(vararg args: Any): SchemaValidationAssert {
  return filter(argumentsContainsAll(*args))
}

inline fun <reified K : JsonSchemaKeyword<*>, reified I : KeywordInfo<K>> SchemaValidationAssert.hasKeyword(keyword: I): SchemaValidationAssert {
  assert(actual?.keyword, "Has keyword").isEqualTo(keyword)
  return this
}

fun SchemaValidationAssert.hasSchemaLocation(uri: String): SchemaValidationAssert {
  return hasSchemaLocation(URI(uri))
}

fun SchemaValidationAssert.hasSchemaLocation(uri: URI): SchemaValidationAssert {
  return filter(schemaLocationEquals(uri))
}

private fun SchemaValidationAssert.filter(vararg filters: ValidationErrorPredicate): SchemaValidationAssert {
  val filteredErrors = this.actual
      ?.allMessages
      ?.filter { e ->
        filters.toSet()
            .all { predicate -> predicate(e) }
      } ?: emptyList()

  val schemaValidationAssert = assert(filteredErrors,
      "violations ${ValidationErrorPredicate.toString(*filters)}")

  schemaValidationAssert.isNotEmpty()
  return this
}

