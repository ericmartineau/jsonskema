package lang

actual object URLDecoder {
  actual fun decode(input: String, charset:String): String {
    return java.net.URLDecoder.decode(input, charset)
  }
}
