package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import lang.hashKode
import kotlinx.serialization.json.ElementType
import lang.range

/**
 * Represents a single keyword and its applicable versions, and acceptable input types.
 *
 *
 * This class is used when loading schemas to validate that the input is of the correct type,
 * and in some cases, simplify the loading of simple values.
 *
 *
 * @param <K> The type of value this keyword produces (a schema, a string, an array, etc)
</K> */
class KeywordInfo<K : JsonSchemaKeyword<*>> {

  val key: String

  /**
   * Which versions this keyword applies to.  eg. additionalProperties expects a boolean or an object
   * up until Draft6, when it requires a schema.
   */
  val applicableVersions: Set<JsonSchemaVersion>

  /**
   * The most recent version this configuration applies to.
   */
  val mostRecentVersion: JsonSchemaVersion

  /**
   * Which types of values this keyword applies to: string, boolean, object, array
   */
  private val forSchemas: Set<JsonSchemaType>

  /**
   * Which json types (correlates to [.forSchemas]
   */
  val applicableTypes: Set<ElementType>

  /**
   * The type of json value expected for this keyword.  Each instance of the keyword can only consuem
   * a single type of value, but they can be linked together using variants.
   */
  val expects: ElementType

  val variants: Map<ElementType, KeywordInfo<K>>

  internal constructor(mainInfo: KeywordVersionInfoBuilder<K>, allVersions: List<KeywordVersionInfoBuilder<K>>) {

    val mainDefinition = mainInfo.build()
    // Copy values from most current builder
    this.key = mainDefinition.key
    this.forSchemas = mainDefinition.forSchemas
    this.applicableTypes = mainDefinition.applicableTypes
    this.expects = mainDefinition.expects
    this.applicableVersions = mainDefinition.applicableVersions
    this.mostRecentVersion = mainDefinition.mostRecentVersion

    this.variants = allVersions
        .map { mainDefinition.copyDefaults(it) }
        .map { it.build() }
        .map { it.expects to it }
        .toMap()
  }

  internal constructor(key: String,
                       forSchemas: Collection<JsonSchemaType>,
                       expects: ElementType,
                       since: JsonSchemaVersion?,
                       until: JsonSchemaVersion?) {
    var sinceVar = since

    sinceVar = sinceVar ?: JsonSchemaVersion.Draft3
    this.mostRecentVersion = until ?: JsonSchemaVersion.latest()
    if (forSchemas.isEmpty()) {
      this.forSchemas = JsonSchemaType.values().toHashSet()
    } else {
      this.forSchemas = forSchemas.toHashSet()
    }
    this.applicableTypes = this.forSchemas
        .map { it.appliesTo }
        .toHashSet()
    this.key = key
    this.expects = expects
    this.applicableVersions = JsonSchemaVersion.values().range(sinceVar, mostRecentVersion)
    this.variants = emptyMap()
  }

  fun getTypeVariant(valueType: ElementType): KeywordInfo<K>? {
    return variants[valueType]
  }

  /**
   * Returns a sublist of variants on this keyword.  Used for digesters operating on older versions of the spec.
   * @param types List of types we're looking for
   * @return A list
   */
  fun getTypeVariants(vararg types: ElementType): List<KeywordInfo<K>> {
    val versionsForType = types
        .map { variants[it] }
        .filterNotNull()
    check(versionsForType.isNotEmpty()) { "[$key] not valid type for [$types]" }
    return versionsForType
  }

  private fun copyDefaults(builder: KeywordVersionInfoBuilder<K>): KeywordVersionInfoBuilder<K> {
    builder.setKeyIfBlank(key)
    if (builder.forSchemas.isEmpty() && !this.forSchemas.isEmpty()) {
      builder.forSchemas += this.forSchemas
    }
    return builder
  }

  override fun toString(): String {
    return key
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is KeywordInfo<*> -> false
      key != other.key -> false
      expects != other.expects -> false
      else -> true
    }
  }

  override fun hashCode(): Int {
    return hashKode(key, expects)
  }

  class KeywordInfoBuilder<K : JsonSchemaKeyword<*>> {

    private lateinit var key: String
    private var main: KeywordVersionInfoBuilder<K>
    private var current: KeywordVersionInfoBuilder<K>
    private val versions: MutableList<KeywordVersionInfoBuilder<K>> = mutableListOf()
    /**
     * Which types of values this keyword applies to: string, boolean, object, array
     */
    private val forSchemas: MutableSet<JsonSchemaType> = mutableSetOf()

    init {
      this.current = KeywordVersionInfoBuilder()
      this.main = current
    }

    fun build(): KeywordInfo<K> {
      this.versions.add(current)
      return KeywordInfo(main, versions)
    }

    fun onlyForVersion(version: JsonSchemaVersion): KeywordInfoBuilder<K> {
      current.onlyForVersion(version)
      return this
    }

    fun additionalDefinition(): KeywordInfoBuilder<K> {
      this.versions.add(current)
      current = KeywordVersionInfoBuilder()
      return this
    }

    fun until(until: JsonSchemaVersion): KeywordInfoBuilder<K> {
      current.until(until)
      return this
    }

    fun since(since: JsonSchemaVersion): KeywordInfoBuilder<K> {
      current.since(since)
      return this
    }

    fun key(key: String): KeywordInfoBuilder<K> {
      current.key(key)
      return this
    }

    fun expects(firstType: ElementType): KeywordInfoBuilder<K> {
      current.expects(firstType)
      return this
    }

    fun validates(type: JsonSchemaType, vararg more: JsonSchemaType): KeywordInfoBuilder<K> {
      current.validates(type, *more)
      return this
    }

    fun from(fromVersion: JsonSchemaVersion): KeywordInfoBuilder<K> {
      current.from(fromVersion)
      return this
    }
  }

  companion object {
    inline fun <reified X : JsonSchemaKeyword<*>> builder(): KeywordInfoBuilder<X> {
      return KeywordInfoBuilder()
    }
  }

  class KeywordVersionInfoBuilder<K : JsonSchemaKeyword<*>> {
    private var since: JsonSchemaVersion? = null
    private var until: JsonSchemaVersion? = null
    private var key: String? = null
    private var expects: ElementType? = null
    internal val forSchemas: MutableSet<JsonSchemaType> = mutableSetOf()

    fun build(): KeywordInfo<K> {
      return KeywordInfo(key!!, forSchemas, expects!!, since, until)
    }

    fun until(version: JsonSchemaVersion): KeywordVersionInfoBuilder<K> {
      return this.apply {
        this.until = version
      }
    }

    fun key(key: String): KeywordVersionInfoBuilder<K> {
      return apply { this.key = key }
    }

    fun since(version: JsonSchemaVersion): KeywordVersionInfoBuilder<K> {
      return this.apply {
        this.since = version
      }
    }

    fun onlyForVersion(version: JsonSchemaVersion): KeywordVersionInfoBuilder<K> {
      from(version).until(version)
      return this
    }

    fun setKeyIfBlank(key: String): KeywordVersionInfoBuilder<K> {
      if (this.key == null) {
        this.key = key
      }
      return this
    }

    fun expects(firstType: ElementType): KeywordVersionInfoBuilder<K> {
      this.expects = firstType
      return this
    }

    fun validates(type: JsonSchemaType, vararg more: JsonSchemaType): KeywordVersionInfoBuilder<K> {
      return this.apply {
        this.forSchemas += type
        this.forSchemas += more
      }
    }

    fun from(fromVersion: JsonSchemaVersion): KeywordVersionInfoBuilder<K> {
      return this.since(fromVersion)
    }
  }
}
