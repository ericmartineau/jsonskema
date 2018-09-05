package lang

expect class URI(scheme: String?, schemeSpecificPart: String?, fragment: String?) {
  constructor(uri: String?)

  fun isAbsolute(): Boolean
  fun isOpaque(): Boolean
  fun relativize(child: URI): URI

  fun getFragment(): String?
  fun getScheme(): String?
  fun getSchemeSpecificPart(): String?
  fun getQuery(): String?
  fun getPath(): String?
}

expect fun URI.readFully(charset: String = "UTF-8"): String

expect fun URI.withNewFragment(newFragment: URI): URI

val URI.isAbsolute: Boolean get() = isAbsolute()
val URI.isOpaque: Boolean get() = isOpaque()
val URI.fragment: String? get() = getFragment()
val URI.scheme: String? get() = getScheme()
val URI.schemeSpecificPart: String? get() = getSchemeSpecificPart()
val URI.query: String? get() = getQuery()
val URI.path: String? get() = getPath()

fun URI.resolveUri(input:String):URI {
  return this.resolveUri(URI(input))
}

expect fun URI.resolveUri(against:URI):URI

val URI.isFragmentOnly: Boolean
  get() {
    return this.fragment != null &&
        query?.isNotEmpty() != true &&
        path?.isNotEmpty() != true &&
        schemeSpecificPart?.isNotEmpty() != true
  }

expect object URLDecoder {
  fun decode(input: String, charset: String): String
}

