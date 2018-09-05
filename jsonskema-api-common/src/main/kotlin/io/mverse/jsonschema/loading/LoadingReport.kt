package io.mverse.jsonschema.loading

import lang.Name

data class LoadingReport(
    @Name("issues")
    val issues: MutableList<LoadingIssue> = mutableListOf(),
    private var hasError: Boolean = false) {

  fun warn(issue: LoadingIssue): LoadingReport {
    return apply {
      issues += issue.copy(level = LoadingIssueLevel.WARN)
    }
  }

  fun error(issue: LoadingIssue): LoadingReport {
    return apply {
      issues += issue.copy(level = LoadingIssueLevel.ERROR)
      hasError = true
    }
  }

  fun log(issue: LoadingIssue): LoadingReport {
    return apply {
      issues.add(issue)
      if (issue.level == LoadingIssueLevel.ERROR) {
        hasError = true
      }
      return this
    }
  }

  fun hasErrors(): Boolean {
    return hasError
  }

  override fun toString(): String {
    if (!hasErrors()) {
      return "No validation"
    }
    val output = StringBuilder("")
    output.append("${issues.size} validation found while loading:\n")
    for (issue in issues) {
      output.append("\t$issue\n")
    }
    return output.toString()
  }
}
