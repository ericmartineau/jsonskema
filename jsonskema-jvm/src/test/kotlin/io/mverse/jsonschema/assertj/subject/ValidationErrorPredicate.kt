package io.mverse.jsonschema.assertj.subject

import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.validation.ValidationError
import lang.Joiner
import lang.URI
import lang.format

interface ValidationErrorPredicate : (ValidationError) -> Boolean {
  companion object {

    val AND = Joiner(" && ", true)
    val EXPRESSION_FORMAT = "%s.%s(%s)"

    fun of(field: String, operator: String, rhs: Any?, predicate: (ValidationError) -> Boolean): ValidationErrorPredicate {
      return of(
          EXPRESSION_FORMAT.format(field, operator, rhs.toString()),
          predicate)
    }

    fun of(toString: String, check: (ValidationError) -> Boolean): ValidationErrorPredicate {
      return object : ValidationErrorPredicate {
        override fun invoke(validationError: ValidationError): Boolean {
          return check(validationError)
        }

        override fun toString(): String {
          return toString
        }
      }
    }

    fun pointerToViolationEquals(pathToError: String): ValidationErrorPredicate {
      return of("pointerToViolation", "eq", pathToError) { e ->
        pathToError.equals(e.pathToViolation!!, ignoreCase = true)
      }
    }

    fun messageContains(message: String): ValidationErrorPredicate {
      return of("message", "contains", message) { e ->
        e.message != null && e.message.contains(message)
      }
    }

    fun codeEquals(code: String): ValidationErrorPredicate {
      return of("code", "contains", code) { e ->
        e.code != null && e.code!!.contains(code)
      }
    }

    fun argumentsContainsAll(vararg arguments: Any): ValidationErrorPredicate {
      return of("arguments", "containsAll", arguments.contentToString()) { e ->
        setOf(*arguments).all { e.arguments!!.contains(it) }
      }
    }

    fun keywordEquals(keyword: KeywordInfo<*>?): ValidationErrorPredicate {
      return of("keyword", "eq", keyword) { e ->
        keyword != null && keyword == e.keyword
      }
    }

    fun schemaLocationEquals(schemaLocation: URI): ValidationErrorPredicate {
      return of("schemaLocation", "eq", schemaLocation) { e ->
        e.schemaLocation != null && schemaLocation == e.schemaLocation
      }
    }

    fun toString(vararg predicates: ValidationErrorPredicate): String {
      return AND.join(*predicates)
    }
  }
}
