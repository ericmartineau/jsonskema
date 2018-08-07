package io.mverse.jsonschema.utils

import lang.UnescapeSequence
import lang.Unescaper

object Unescapers {

  fun builder(): Builder {
    return Builder()
  }

  class Builder {
    private val replacements = mutableListOf<UnescapeSequence>()

    fun addUnescape(matcher: String, replacement: String): Builder {
      replacements.add(UnescapeSequence(matcher, replacement))
      return this
    }

    fun build(): Unescaper {
      return DefaultUnescaper(replacements)
    }
  }

  class DefaultUnescaper(unescapers: List<UnescapeSequence>) : Unescaper() {
    private val unescapers: Array<UnescapeSequence> = unescapers.toTypedArray()

    override fun unescape(string: CharSequence?): String? {
      if (string == null) {
        return null
      }
      val builder = StringBuilder(string)
      for (unescaper in unescapers) {
        var pos = -1
        val l = unescaper.searchString.length
        while (pos > -1) {
          pos = builder.indexOf(unescaper.searchString, pos)
          builder.replaceRange(pos, pos + l, unescaper.replacement)
        }
      }
      return builder.toString()
    }
  }
}
