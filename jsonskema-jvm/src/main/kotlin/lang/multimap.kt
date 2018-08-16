package lang

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableSetMultimap

actual typealias SetMultimap<K, V> = com.google.common.collect.SetMultimap<K, V>
actual typealias ListMultimap<K, V> = com.google.common.collect.ListMultimap<K, V>

actual class MutableSetMultimap<K, V>(
    val hash: HashMultimap<K, V> = HashMultimap.create()) : SetMultimap<K, V> by hash {

  actual constructor() : this(HashMultimap.create())

  actual fun add(key: K?, value: V?): Boolean {
    return hash.put(key, value)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
  }

  actual fun <K, V> freeze(): SetMultimap<K, V> {
    @Suppress("UNCHECKED_CAST")
    return ImmutableSetMultimap.copyOf(hash) as SetMultimap<K, V>
  }
}

actual fun <K, V> SetMultimap<K, V>.toMutableSetMultimap(): MutableSetMultimap<K, V> {
  return MutableSetMultimap(HashMultimap.create(this))
}

actual class MutableListMultimap<K, V>(
    val hash: ArrayListMultimap<K, V> = ArrayListMultimap.create())

  : ListMultimap<K, V> by hash {

  actual constructor() : this(ArrayListMultimap.create())
  actual fun add(key: K?, value: V?): Boolean {
    return hash.put(key, value)
  }

  actual operator fun plusAssign(pair: Pair<K, V>) {
    put(pair.first, pair.second)
  }

  actual fun <K, V> freeze(): ListMultimap<K, V> {
    @Suppress("UNCHECKED_CAST")
    return ImmutableListMultimap.copyOf(hash) as ListMultimap<K, V>
  }
}

actual fun <K, V> ListMultimap<K, V>.toMutableListMultimap(): MutableListMultimap<K, V> {
  return MutableListMultimap(ArrayListMultimap.create(this))
}

actual object Multimaps {
  actual fun <K, V> emptySetMultimap(): SetMultimap<K, V> {
    return ImmutableSetMultimap.of()
  }

  actual fun <K, V> emptyListMultimap(): ListMultimap<K, V> {
    return ImmutableListMultimap.of()
  }
}
