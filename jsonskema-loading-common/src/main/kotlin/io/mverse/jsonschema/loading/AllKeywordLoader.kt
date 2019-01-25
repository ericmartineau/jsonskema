package io.mverse.jsonschema.loading

import io.mverse.jsonschema.JsonValueWithPath
import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.KeywordInfo
import io.mverse.jsonschema.loading.LoadingIssues.keywordNotFoundIssue
import lang.collection.MutableSetMultimap
import lang.collection.SetMultimap

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

  fun loadKeywordsForSchema(jsonObject: JsonValueWithPath, builder: MutableSchema, report: LoadingReport) {
    //Process keywords we know:
    jsonObject.forEachKey { prop, jsrValue ->
      val matches = mutableMapOf<JsonSchemaVersion, KeywordDigester<*>>()
      val nonMatches = mutableListOf<LoadingIssue>()
      for (infoAndLoader in filteredExtractors[prop]) {
        val possibleKeyword = infoAndLoader.first
        val keywordLoader = infoAndLoader.second
        val expectedType = possibleKeyword.expects
        if (jsrValue.type !== expectedType) {
          nonMatches.add(LoadingIssues.typeMismatch(possibleKeyword, jsrValue)
              .copy(level = LoadingIssueLevel.ERROR))
        } else {
          matches[possibleKeyword.mostRecentVersion] = keywordLoader
        }
      }
      if (matches.isNotEmpty()) {
        for (draftVersion in JsonSchemaVersion.publicVersions) {
          val foundMatch = matches[draftVersion]
          if (foundMatch != null) {
            if (processKeyword(foundMatch, jsonObject, builder, schemaLoader, report)) {
              break
            }
          }
        }
      } else if (nonMatches.size > 0) {
        nonMatches.forEach { report.log(it) }
      } else if (!strict) {
        report.warn(keywordNotFoundIssue(prop, jsrValue))
        builder.extraProperties[prop] = jsrValue.wrapped
      } else {
        report.error(keywordNotFoundIssue(prop, jsrValue))
      }
    }
  }

  private fun <K : Keyword<*>> processKeyword(digester: KeywordDigester<K>, JsrValue: JsonValueWithPath,
                                              builder: MutableSchema, factory: SchemaLoader,
                                              report: LoadingReport): Boolean {
    val digest = digester.extractKeyword(JsrValue, builder, factory, report)
    return when (digest) {
      null -> false
      else -> {
        builder[digest.keyword] = digest.kvalue
        true
      }
    }
  }
}
