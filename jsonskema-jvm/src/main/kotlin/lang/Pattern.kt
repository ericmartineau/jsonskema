package lang

actual data class Pattern(actual val regex: String, val jpattern: java.util.regex.Pattern) {
  actual constructor(regex: String) : this(regex, java.util.regex.Pattern.compile(regex))
  constructor(jpattern: java.util.regex.Pattern) : this(jpattern = jpattern, regex = jpattern.pattern())

  actual fun find(subject: String): Boolean = jpattern.matcher(subject).find()
}
