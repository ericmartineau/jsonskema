package lang

actual open class SetMultimap<K, V> actual constructor(protected open val internal: Map<K, Set<V>>) {

  val size: Int by lazy {
    internal.values.map { it.size }.sum()
  }

  actual fun asMap(): Map<K, Set<V>> = internal

  actual operator fun get(key: K): Set<V> = internal.getOrElse(key) { setOf() }

  actual fun toMutableSetMultimap(): MutableSetMultimap<K, V> = MutableSetMultimap(internal
      .mapValues { it.value.toHashSet() }
      .toMutableMap())
}

actual open class MutableSetMultimap<K, V> actual constructor(override val internal: MutableMap<K, HashSet<V>>)
  : SetMultimap<K, V>(internal) {

  actual fun put(key: K, value: V): Boolean {
    return internal.getOrPut(key) { HashSet() }.add(value)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
  }

  actual fun toSetMultimap(): SetMultimap<K, V> = SetMultimap(internal.toMap())
}

actual open class ListMultimap<K, V> actual constructor(protected open val internal: Map<K, List<V>>) {
  actual fun asMap(): Map<K, List<V>> = internal

  actual operator fun get(key: K): List<V> = internal.getOrElse(key) { emptyList() }

  actual fun toMutableListMultimap(): MutableListMultimap<K, V> = MutableListMultimap(internal
      .mapValues { entry->
        ArrayList<V>().apply {
          this += entry.value
        }
      }
      .toMutableMap())
}

actual open class MutableListMultimap<K, V> actual constructor(override val internal: MutableMap<K, ArrayList<V>>) : ListMultimap<K, V>() {

  val size: Int get() = internal.values.map { it.size }.sum()

  actual fun put(key: K, value: V): Boolean {
    return internal.getOrPut(key) { arrayListOf() }.add(value)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
  }

  actual fun toListMultimap(): ListMultimap<K, V> = ListMultimap(internal.toMap())
}
