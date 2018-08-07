package lang

expect class URI(uri: String) {
  constructor(scheme: String?, schemeSpecificPart: String?, fragment: String?)

  val isAbsolute: Boolean
  val isOpaque: Boolean
  fun resolve(against: URI): URI
  fun withNewFragment(newFragment: URI): URI
  fun resolve(against: String): URI

  val fragment: String?
  val scheme: String?
  val schemSpecificPart: String?
  val query: String?
  val path: String?
}

val URI.isFragmentOnly: Boolean
  get() {
    return this.fragment != null &&
        query?.isNotEmpty() != true &&
        path?.isNotEmpty() != true &&
        schemSpecificPart?.isNotEmpty() != true
  }

expect object URLDecoder {
  fun decode(input: String, charset: String): String
}
