package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaLocation
import io.mverse.jsonschema.keyword.KeywordInfo
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull

object LoadingIssues {

  fun conflictingKeyword(foundKeyword: KeywordInfo<*>, conflictingKeyword: KeywordInfo<*>): LoadingIssue {
    return LoadingIssue(
        code = "keyword.conflicting",
        value = JsonNull,
        message = "Found keyword [%s] ($3s), but also found conflicting keyword [%s]",
        arguments = listOf(foundKeyword.key, conflictingKeyword.key)
    )
  }

  fun invalidKeywordValue(keyword: KeywordInfo<*>, message: String): LoadingIssue {
    return LoadingIssue(
        code = "keyword.invalid",
        value = JsonNull,
        message = "Invalid keyword [%s]: $message",
        arguments = listOf(keyword))
  }

  fun missingKeywordIssue(foundKeyword: KeywordInfo<*>, expectedKeyword: KeywordInfo<*>): LoadingIssue {
    return LoadingIssue(
        code = "keyword.missing",
        value = JsonNull,
        message = "Found keyword [%s], was expecting [%s]",
        arguments = listOf(foundKeyword.key, expectedKeyword.key))
  }

  fun keywordNotFoundIssue(foundKeywordName: String, foundValue: JsonValueWithPath): LoadingIssue {
    return LoadingIssue(
        code = "keyword.notFound",
        value = foundValue.wrapped,
        location = foundValue.location,
        message = "Found keyword [%s] that wasn't allowed in the schema",
        arguments = listOf(foundKeywordName))
  }

  fun typeMismatch(keyword: KeywordInfo<*>, value: JsonElement, location: SchemaLocation): LoadingIssue {
    return LoadingIssue(
        code = "keyword.type.mismatch",
        value = value,
        location = location,
        message = "Value [%s] was [%s], was expecting [%s]",
        arguments = listOf(value, value::class, keyword.applicableTypes))
  }

  fun typeMismatch(keyword: KeywordInfo<*>, value: JsonValueWithPath): LoadingIssue {
    return typeMismatch(keyword, value.wrapped, value.location)
  }
}
