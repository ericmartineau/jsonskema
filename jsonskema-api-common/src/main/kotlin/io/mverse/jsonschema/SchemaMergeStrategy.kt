package io.mverse.jsonschema

import io.mverse.jsonschema.builder.MutableSchema
import io.mverse.jsonschema.keyword.Keyword
import io.mverse.jsonschema.keyword.Keywords
import io.mverse.jsonschema.utils.calculateMergeURI
import lang.json.JsonPath
import lang.net.URI
import lang.suppress.Suppressions

interface SchemaMergeStrategy {
  /**
   * Merged this schema with another this schema, a copy is returned.
   */
  fun mergeOrNull(path: JsonPath, baseSchema: Schema?, override: Schema?, report: MergeReport,
                  mergedId: URI? = baseSchema?.absoluteURI?.calculateMergeURI(override?.absoluteURI)): Schema? =
      when (baseSchema) {
        null -> null
        else -> merge(path, baseSchema, override, report, mergedId)
      }

  /**
   * Merged this schema with another this schema, a copy is returned.
   */
  fun merge(path: JsonPath, baseSchema: Schema, override: Schema?, report: MergeReport,
            mergedId: URI? = baseSchema.absoluteURI.calculateMergeURI(override?.absoluteURI)): Schema {
    return when {
      baseSchema.isRefSchema -> override ?: baseSchema
      else -> {
        val overrides = override ?: return baseSchema
        val source = if (mergedId == null) baseSchema else baseSchema.withDocumentURI(mergedId)
        val mutable = source.toMutableSchema()
        merge(path, mutable, overrides, report)
        mutable.build()
      }
    }
  }

  /**
   * Merged this builder from values found in another schema
   */
  fun merge(path: JsonPath, baseSchema: MutableSchema, override: Schema, report: MergeReport) = baseSchema.run {
    val merger = this@SchemaMergeStrategy
    override.extraProperties.forEach { (k, v) ->
      extraProperties[k] = v
    }

    override.keywords
        .filter { (keyword) -> keyword != Keywords.DOLLAR_ID && keyword != Keywords.ID }
        .forEach { (keyword, value) ->
          val kwPath = path.child(keyword.key)
          if (keyword !in this) {
            unsafeSet(keyword, value)
            report += mergeAdd(kwPath, keyword)
          } else {
            @Suppress(Suppressions.UNCHECKED_CAST)
            val thisValue = keywords[keyword] as Keyword<Any>
            @Suppress(Suppressions.UNCHECKED_CAST)
            val otherValue = value as Keyword<Any>
            try {
              val mergeKeyword = thisValue.merge(merger, kwPath, keyword, otherValue, report)
              unsafeSet(keyword, mergeKeyword)
            } catch (e: MergeException) {
              report += mergeError(kwPath, keyword, e)
            }
          }
        }
  }
}
