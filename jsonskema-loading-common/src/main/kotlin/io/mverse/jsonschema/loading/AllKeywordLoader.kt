package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.SchemaBuilder
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.LoadingIssues.keywordNotFoundIssue
import lang.MutableSetMultimap
import lang.SetMultimap

typealias KeywordInfoAndDigester = Pair<KeywordInfo<*>, KeywordDigester<*>>

data class AllKeywordLoader(val allExtractors: List<KeywordDigester<*>>,
                            val defaultVersions: Set<JsonSchemaVersion>,
                            val schemaLoader: SchemaLoader,
                            private val strict: Boolean = false) {

  private val filteredExtractors: SetMultimap<String, KeywordInfoAndDigester>

  init {
    val filtered = MutableSetMultimap<String, KeywordInfoAndDigester>()

    // This instance is provided with a list of valid versions to process.
    // In this block of code, we filter out any keywords that aren't supported
    for (keywordLoader in allExtractors) {
      for (processedKeyword in keywordLoader.includedKeywords) {
        if (processedKeyword.applicableVersions.intersect(defaultVersions).isNotEmpty()) {
          filtered.add(processedKeyword.key, processedKeyword to keywordLoader)
        }
      }
    }

    this.filteredExtractors = filtered.freeze()
  }

  fun loadKeywordsForSchema(jsonObject: JsonValueWithPath, builder: SchemaBuilder, report: LoadingReport) {
    //Process keywords we know:
    jsonObject.forEachKey { prop, jsonElement ->
      val matches = mutableMapOf<JsonSchemaVersion, KeywordDigester<*>>()
      val nonMatches = mutableListOf<LoadingIssue>()
      for (infoAndLoader in filteredExtractors[prop]) {
        val possibleKeyword = infoAndLoader.first
        val keywordLoader = infoAndLoader.second
        val expectedType = possibleKeyword.expects
        if (jsonElement.type !== expectedType) {
          nonMatches.add(LoadingIssues.typeMismatch(possibleKeyword, jsonElement)
              .copy(level = LoadingIssueLevel.ERROR))
        } else {
          matches[possibleKeyword.mostRecentVersion] = keywordLoader
        }
      }
      if (matches.isNotEmpty()) {
        for (draftVersion in JsonSchemaVersion.publicVersions) {
          val foundMatch = matches.get(draftVersion)
          if (foundMatch != null) {
            if (processKeyword(foundMatch, jsonObject, builder, schemaLoader, report)) {
              break
            }
          }
        }
      } else if (nonMatches.size > 0) {
        nonMatches.forEach { report.log(it) }
      } else if (!strict) {
        report.warn(keywordNotFoundIssue(prop, jsonElement))
        builder.extraProperties += prop to jsonElement.wrapped
      } else {
        report.error(keywordNotFoundIssue(prop, jsonElement))
      }
    }
  }

  private fun <K : Keyword<*>> processKeyword(digester: KeywordDigester<K>, jsonElement: JsonValueWithPath,
                                              builder: SchemaBuilder, factory: SchemaLoader,
                                              report: LoadingReport): Boolean {
    val digest = digester.extractKeyword(jsonElement, builder, factory, report)
    return when(digest) {
      null-> false
      else-> {
        builder[digest.keyword] = digest.kvalue
        true
      }
    }
  }
}
