package lang

actual fun hashKode(vararg items: Any?): Int = nl.pvdberg.hashkode.hashKode(fields = *items)
