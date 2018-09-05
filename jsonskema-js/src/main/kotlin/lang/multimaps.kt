package lang

actual interface SetMultimap<K, V> {
  actual fun asMap(): Map<K, Collection<V>>
  actual operator fun get(key: K?): Set<V>
  actual fun size(): Int
}

actual class MutableSetMultimap<K, V> constructor(backing: Map<K, Collection<V>> = emptyMap()) : SetMultimap<K, V> {
  private val backing = mutableMapOf<K, MutableSet<V>>().apply {
    backing.entries.forEach { (k, v) ->
      put(k, v.toMutableSet())
    }
  }

  actual constructor() : this(backing = emptyMap())

  override fun asMap(): Map<K, Collection<V>> = backing
  override fun get(key: K?): Set<V> = backing[key] ?: emptySet()
  override fun size(): Int = backing.values.sumBy { it.size }

  /**
   * Weird naming due to conflicts with the name "put"
   */
  actual fun add(key: K?, value: V?): Boolean {
    val set = backing.getOrPut(key!!) {
      mutableSetOf()
    }
    return set.add(value!!)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    add(pair.first, pair.second)
  }

  actual fun <K, V> freeze(): SetMultimap<K, V> {
    return this as SetMultimap<K, V>
  }
}

actual fun <K, V> SetMultimap<K, V>.toMutableSetMultimap(): MutableSetMultimap<K, V> {
  return MutableSetMultimap(backing = this.asMap())
}

actual interface ListMultimap<K, V> {
  actual fun asMap(): Map<K, Collection<V>>
  actual operator fun get(key: K?): List<V>
  actual fun size(): Int
}

actual class MutableListMultimap<K, V> constructor(backing: Map<K, Collection<V>> = emptyMap()) : ListMultimap<K, V> {
  actual constructor() : this(backing = emptyMap())

  private val backing = mutableMapOf<K, MutableList<V>>().apply {
    backing.entries.forEach { (k, v) ->
      put(k, v.toMutableList())
    }
  }

  override fun asMap(): Map<K, Collection<V>> = backing
  override fun get(key: K?): List<V> = backing[key] ?: emptyList()
  override fun size(): Int = backing.values.sumBy { it.size }

  /**
   * Weird naming due to conflicts with the name "put"
   */
  actual fun add(key: K?, value: V?): Boolean {
    val set = backing.getOrPut(key!!) {
      mutableListOf()
    }
    return set.add(value!!)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    add(pair.first, pair.second)
  }

  actual fun <K, V> freeze(): ListMultimap<K, V> {
    return this as ListMultimap<K, V>
  }
}

actual fun <K, V> ListMultimap<K, V>.toMutableListMultimap(): MutableListMultimap<K, V> {
  return MutableListMultimap(asMap())
}

actual object Multimaps {
  actual fun <K, V> emptySetMultimap(): SetMultimap<K, V> {
    return MutableSetMultimap()
  }

  actual fun <K, V> emptyListMultimap(): ListMultimap<K, V> {
    return MutableListMultimap()
  }
}
