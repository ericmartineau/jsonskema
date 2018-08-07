package lang

open class Unescaper(private val unescapes: Iterable<UnescapeSequence>) {
  constructor(vararg pairs:Pair<String, String>): this(pairs.map { UnescapeSequence(it) })

  open fun unescape(string: CharSequence?): String? {
    var builder = string ?: return null
    for ((searchString, replacement) in unescapes) {
      var pos = -1
      val l = searchString.length
      pos = builder.indexOf(searchString, pos)
      while (pos > -1) {
        builder = builder.replaceRange(pos, pos + l, replacement)
        pos = builder.indexOf(searchString, pos)
      }
    }
    return builder.toString()
  }
}
