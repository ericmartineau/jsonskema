package io.mverse.jsonschema

import io.mverse.jsonschema.keyword.KeywordInfo
import lang.json.JsonPath

data class MergeReport(val actions: MutableList<MergeReportAction> = mutableListOf()) : Iterable<MergeReportAction> by actions {
  var isConflict: Boolean = false
  var isMerge: Boolean = false
  operator fun plusAssign(issue: MergeReportAction) {
    when (issue.type) {
      MergeActionType.CONFLICT-> isConflict = true
      MergeActionType.MERGE-> isMerge = true
    }
    this.actions += issue
  }
}

fun mergeAdd(path: JsonPath, keyword: KeywordInfo<*>) = MergeReportAction(path = path, keyword = keyword,
    type = MergeActionType.ADD)

fun mergeCombine(path: JsonPath, keyword: KeywordInfo<*>, first: Any?, second: Any?) = MergeReportAction(path = path, keyword = keyword,
    type = MergeActionType.MERGE, first = first, second = second)

fun mergeConflict(path: JsonPath, keyword: KeywordInfo<*>, first: Any?, second: Any?) = MergeReportAction(path = path, keyword = keyword,
    first = first, second = second, type = MergeActionType.CONFLICT)

fun mergeError(path: JsonPath, keyword: KeywordInfo<*>, e: MergeException) = MergeReportAction(path = path, keyword = keyword,
    type = MergeActionType.ERROR, message = e.message)

data class MergeReportAction(val path: JsonPath, val keyword: KeywordInfo<*>, val type: MergeActionType,
                             val first: Any? = null, val second: Any? = null, val message: String? = null)

enum class MergeActionType {
  ADD,
  MERGE,
  CONFLICT,
  ERROR
}

class MergeException(val reason: String? = null) : Exception(reason ?: "Can't merge these schemas")

fun mergeException(): Nothing = throw MergeException()
fun mergeException(reason: String): Nothing = throw MergeException(reason)