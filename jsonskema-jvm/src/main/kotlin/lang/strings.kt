package lang

import com.google.common.base.Utf8
import com.google.common.net.UrlEscapers
import java.util.*

typealias GuavaSplitter = com.google.common.base.Splitter
typealias GuavaEscaper = com.google.common.escape.Escaper
typealias GuavaEscapers = com.google.common.escape.Escapers
typealias GuavaJoiner = com.google.common.base.Joiner

actual class Splitter actual constructor(splitOn: Char) {
  val splitter:GuavaSplitter = GuavaSplitter.on(splitOn)
  actual fun splitToList(input: String): List<String> = splitter.splitToList(input)
  actual fun split(input: String): Iterable<String> = splitter.split(input)
}

actual class Escapers actual constructor() {
  actual companion object {
    actual fun urlPathSegmentEscaper(): Escaper = UrlPathSegmentEscaper()
  }
}

class UrlPathSegmentEscaper:Escaper() {
  override fun escape(input: String): String = UrlEscapers.urlPathSegmentEscaper().escape(input)
}

actual open class Escaper actual constructor(vararg escapes: Pair<Char, String>) {
  val escaper:GuavaEscaper = GuavaEscapers.builder().apply {
    for ((char,replace) in escapes) {
      addEscape(char, replace)
    }
  }.build()

  actual open fun escape(input: String): String = escaper.escape(input)
}

actual fun String.format(vararg args: Any?): String = this.format(locale = Locale.US, args = *args)

actual class Joiner actual constructor(separator: String, skipNulls: Boolean) {
  val joiner: GuavaJoiner = GuavaJoiner.on(separator).apply {
    if (skipNulls) {
      skipNulls()
    }
  }

  actual fun join(vararg items: Any?): String = joiner.join(items)
}

actual fun String.codePointCount(): Int {
  return this.codePointCount(0, this.length)
}

actual fun ByteArray.toString(charset:String): String = this.toString(charset(charset))
