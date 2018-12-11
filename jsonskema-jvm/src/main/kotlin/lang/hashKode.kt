package lang

actual fun hashKode(vararg items: Any?): Int = io.mverse.hashkode.hashKode(fields = *items)
