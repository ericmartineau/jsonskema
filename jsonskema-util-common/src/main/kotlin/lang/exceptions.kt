package lang

fun illegalState(message: String = "Unspecified error"): Nothing {
  throw IllegalStateException(message)
}
