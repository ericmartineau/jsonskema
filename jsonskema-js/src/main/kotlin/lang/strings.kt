package lang

import kotlinx.serialization.stringFromUtf8Bytes

val regex = Regex("%([sd])")

actual open class Escaper actual constructor(vararg escapes: Pair<Char, String>) {
  private val escapes = escapes.toList()
  actual open fun escape(input: String): String {
    var output = input
    for ((find, replace) in escapes) {
      output = output.replace(find.toString(), replace)
    }
    return output
  }
}

actual fun String.format(vararg args: Any?): String {
  val argIterator = args.iterator()
  return regex.replace(this) {
    argIterator.next().toString()
  }
}

internal val URL_PATH_OTHER_SAFE_CHARS_LACKING_PLUS = (
    "-._~" // Unreserved characters.

        + "!$'()*,;&=" // The subdelim characters (excluding '+').

        + "@:") // The gendelim characters permitted in paths.

actual class Escapers actual constructor() {
  actual companion object {
    actual fun urlPathSegmentEscaper(): Escaper = object:Escaper() {
      override fun escape(input: String): String = encodeURIComponent(input)
    }
  }
}

actual fun ByteArray.toString(charset: String): String {
  return stringFromUtf8Bytes(this)
}

actual class Splitter actual constructor(private val splitOn: Char) {
  actual fun splitToList(input: String): List<String> = input.split(splitOn)
  actual fun split(input: String): Iterable<String> = input.split(splitOn)
}

actual class Joiner actual constructor(val separator: String, val skipNulls: Boolean) {
  actual fun join(vararg items: Any?): String {
    return items.filter { skipNulls || it != null }.joinToString(separator = separator)
  }
}

actual fun String.codePointCount(): Int = this.length
