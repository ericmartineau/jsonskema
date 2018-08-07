package lang

fun java.net.URI.toCommonURI():URI = URI(this)

typealias JURI = java.net.URI

actual object URLDecoder {
  actual fun decode(input: String, charset:String): String {
    return java.net.URLDecoder.decode(input, charset)
  }
}
