package lang

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableSet

actual fun <T> Iterable<T>.freezeList(): List<T> = this.toImmutableList()
actual fun <T> Iterable<T>.freezeSet(): Set<T> = this.toImmutableSet()
actual fun <K, V> Map<K, V>.freezeMap(): Map<K, V> = this.toImmutableMap()
