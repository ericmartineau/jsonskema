package lang

expect class Pattern(regex:String) {
  fun find(subject: String): Boolean

  val regex: String
}
