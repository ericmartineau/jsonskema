package io.mverse.jsonschema.assertj.asserts

import assertk.Assert
import assertk.all
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
import assertk.assertions.isNotNull
import assertk.fail
import io.mverse.jsonschema.JsonPath
import io.mverse.jsonschema.assertThat
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.argumentsContainsAll
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.codeEquals
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.pointerToViolationEquals
import io.mverse.jsonschema.assertj.subject.ValidationErrorPredicate.Companion.schemaLocationEquals
import io.mverse.jsonschema.keyword.JsonSchemaKeyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.validation.ValidationError
import lang.URI
import kotlin.test.fail

typealias SchemaValidationAssert = Assert<ValidationError?>
typealias ValidationListAssert = Assert<List<ValidationError>>

//  private val errors: List<ValidationError>
//  private val flattened: List<ValidationError>

fun SchemaValidationAssert.isValid(): SchemaValidationAssert {
  if(this.actual != null) {
    assertk.fail("Expecting no validation errors, but found: ${
    this.actual!!.allMessages.joinToString(separator = "\n - ", prefix = "\n - ") { it.message }
    }")
  }
  return this
}

fun SchemaValidationAssert.isNotValid(): SchemaValidationAssert {
  if(this.actual?.allMessages?.size == 0) {
    fail("Was unexpectedly valid. We should have encountered errors")
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
  return apply {filter(codeEquals(errorCode))}
}

fun SchemaValidationAssert.hasViolationAt(pointerToViolation: String): SchemaValidationAssert {
  val filtered = filter(pointerToViolationEquals(pointerToViolation))
  return ValidationError.collectErrors(actual!!.violatedSchema!!,
      JsonPath.parseFromURIFragment(pointerToViolation),
      filtered)
      .assertThat()

}

fun SchemaValidationAssert.hasErrorArguments(vararg args: Any): SchemaValidationAssert {
  return apply {filter(argumentsContainsAll(*args))}
}

fun <K : JsonSchemaKeyword<*>, I : KeywordInfo<K>> SchemaValidationAssert.hasKeyword(keyword: I): SchemaValidationAssert {
  assert(actual?.keyword, "Has keyword").isEqualTo(keyword)
  return this
}

fun SchemaValidationAssert.hasSchemaLocation(uri: String): SchemaValidationAssert {
  return hasSchemaLocation(URI(uri))
}

fun SchemaValidationAssert.hasSchemaLocation(uri: URI): SchemaValidationAssert {
  return apply {filter(schemaLocationEquals(uri)) }
}

private fun SchemaValidationAssert.filter(vararg filters: ValidationErrorPredicate): List<ValidationError> {
  val filteredErrors = this.actual
      ?.allMessages
      ?.filter { e ->
        filters.toSet()
            .all { predicate -> predicate(e) }
      } ?: emptyList()

  val missingErrors = filteredErrors.isEmpty()
  if (missingErrors && this.actual?.allMessages?.isNotEmpty() == true) {
    val allErrors = this.actual?.allMessages?.joinToString(separator = "\n -", prefix = "\n -") { it.message }
    fail("Expected violation: [${ValidationErrorPredicate.toString(*filters)}] Found: $allErrors")

  } else if(missingErrors) {
    fail("Expected violation: [${ValidationErrorPredicate.toString(*filters)}] No violations were found")
  }

  return filteredErrors
}

