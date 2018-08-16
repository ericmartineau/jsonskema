package lang

expect fun String.format(vararg args:Any?):String

expect open class Escaper(vararg escapes: Pair<Char, String>) {
  open fun escape(input: String): String
}

data class UnescapeSequence(val searchString: String, val replacement: String) {
  constructor(pair:Pair<String, String>): this(pair.first, pair.second)
}

expect class Escapers() {
  companion object {
    fun urlPathSegmentEscaper():Escaper
  }
}

expect fun ByteArray.toString(charset:String = "UTF-8") :String

expect class Splitter(splitOn:Char) {
  fun splitToList(input:String): List<String>
  fun split(input: String): Iterable<String>
}

expect class Joiner(separator:String, skipNulls:Boolean = true) {
  fun join(vararg items:Any?):String
}

expect fun String.codePointCount():Int
