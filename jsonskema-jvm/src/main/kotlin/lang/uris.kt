package lang

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import java.nio.charset.Charset

actual typealias URI = java.net.URI

actual fun URI.readFully(charset: String): String {
  return toURL().readText(Charset.forName(charset))
}

actual fun URI.withNewFragment(newFragment: URI): URI {
  check(newFragment.isFragmentOnly) { "Must only be a fragment" }
  return URI(scheme, schemeSpecificPart, newFragment.fragment)
}

actual fun URI.resolveUri(against: URI): URI {
  return if (this.isOpaque && against.isFragmentOnly) {
    withNewFragment(against)
  } else {
    resolve(against)
  }
}

@Serializer(forClass = java.net.URI::class)
object URISerializer:KSerializer<java.net.URI> {
  override fun deserialize(input: Decoder): java.net.URI = URI(input.decodeString())
  override fun serialize(output: Encoder, obj: java.net.URI) = output.encodeString(obj.toString())
}