package io.mverse.jsonschema.keyword

import io.mverse.jsonschema.enums.JsonSchemaType
import io.mverse.jsonschema.enums.JsonSchemaVersion
import io.mverse.jsonschema.enums.appliesTo
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.json.ElementType
import kotlinx.serialization.withName
import lang.SerializableWith
import lang.enums.range
import lang.exception.illegalState
import lang.hashKode

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
@SerializableWith(KeywordInfoSerializer::class)
data class KeywordInfo<K : Keyword<*>>(
    val key: String,

    /**
     * Which versions this keyword applies to.  eg. additionalProperties expects a boolean or an object
     * up until Draft6, when it requires a schema.
     */
    val applicableVersions: Set<JsonSchemaVersion>,

    /**
     * The most recent version this keyword applies to.
     */
    val mostRecentVersion: JsonSchemaVersion,

    /**
     * Which types of values this keyword applies to: string, boolean, object, array
     */
    private val forSchemas: Set<JsonSchemaType>,

    /**
     * Which json types are validated by this keyword (correlates to [.forSchemas]
     */
    val applicableTypes: Set<ElementType>,

    /**
     * The type of json value expected for this keyword.  Each instance of the keyword can only consuem
     * a single type of value, but they can be linked together using variants.
     */
    val expects: ElementType,

    val variants: Map<ElementType, KeywordInfo<K>> = emptyMap()) {

  internal constructor(mainInfo: KeywordInfo<K>, allVersions: List<KeywordVersionInfoBuilder<K>>)
      : this(key = mainInfo.key,
      forSchemas = mainInfo.forSchemas,
      applicableTypes = mainInfo.applicableTypes,
      expects = mainInfo.expects,
      applicableVersions = mainInfo.applicableVersions,
      mostRecentVersion = mainInfo.mostRecentVersion,
      variants = allVersions
          .map { mainInfo.copyDefaults(it) }
          .map { it.build() }
          .map { it.expects to it }
          .toMap()
  )

  internal constructor(mainInfo: KeywordVersionInfoBuilder<K>, allVersions: List<KeywordVersionInfoBuilder<K>>)
      : this(mainInfo = mainInfo.build(), allVersions = allVersions)

  internal constructor(key: String,
                       forSchemas: Collection<JsonSchemaType> = JsonSchemaType.values().toHashSet(),
                       expects: ElementType,
                       since: JsonSchemaVersion?,
                       until: JsonSchemaVersion?) : this(
      key = key,
      expects = expects,
      mostRecentVersion = until ?: JsonSchemaVersion.latest,
      forSchemas = forSchemas.toHashSet(),
      applicableTypes = forSchemas
          .map { it.appliesTo }
          .toHashSet(),
      applicableVersions = JsonSchemaVersion.values().range(since ?: JsonSchemaVersion.Draft3, until
          ?: JsonSchemaVersion.latest)
  )

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

  class KeywordInfoBuilder<K : Keyword<*>> {

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
    inline fun <reified X : Keyword<*>> builder(): KeywordInfoBuilder<X> {
      return KeywordInfoBuilder()
    }
  }

  class KeywordVersionInfoBuilder<K : Keyword<*>> {
    private var since: JsonSchemaVersion? = null
    private var until: JsonSchemaVersion? = null
    private var key: String? = null
    private var expects: ElementType? = null
    internal val forSchemas: MutableSet<JsonSchemaType> = mutableSetOf()

    fun build(): KeywordInfo<K> {
      if (forSchemas.isEmpty()) {
        forSchemas.addAll(JsonSchemaType.values())
      }
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

class KeywordInfoSerializer() : KSerializer<KeywordInfo<out Keyword<*>>> {
  constructor(ser: KSerializer<Any>) : this()

  override val descriptor: SerialDescriptor = StringDescriptor.withName("KeywordInfo")

  override fun deserialize(input: Decoder): KeywordInfo<Keyword<*>> {
    illegalState("Unable to deserialize keyword info")
  }

  override fun serialize(output: Encoder, obj: KeywordInfo<out Keyword<*>>) {
    output.encodeString(obj.key)
  }
}
