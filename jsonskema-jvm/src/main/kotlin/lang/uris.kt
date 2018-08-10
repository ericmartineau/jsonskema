package lang

import kotlinx.io.InputStream

actual data class URI(private val juri: JURI) {
  actual constructor(uri: String) : this(JURI.create(uri))
  actual constructor(scheme: String?, schemeSpecificPart: String?, fragment: String?) :
      this(java.net.URI(scheme, schemeSpecificPart, fragment))

  actual val isAbsolute: Boolean = juri.isAbsolute
  actual val isOpaque: Boolean = juri.isOpaque
  actual fun resolve(against: URI): URI {
    return if (this.isOpaque && against.isFragmentOnly) {
      this.withNewFragment(against)
    } else {
      juri.resolve(against.juri).toCommonURI()
    }
  }

  actual fun resolve(against: String): URI = this.resolve(URI(against))

  actual fun withNewFragment(newFragment: URI): URI {
    check(newFragment.isFragmentOnly) { "Must only be a fragment" }
    return URI(scheme, schemSpecificPart, newFragment.fragment)
  }

  actual val fragment: String? = juri.fragment
  actual val scheme: String? = juri.scheme
  actual val schemSpecificPart: String? = juri.schemeSpecificPart
  actual val query: String? = juri.query
  actual val path: String? = juri.path

  override fun toString(): String = juri.toString()
  actual fun relativize(uri: URI): URI = juri.relativize(uri.juri).toCommonURI()
  actual fun toStream(): InputStream = juri.toURL().openStream()
}
