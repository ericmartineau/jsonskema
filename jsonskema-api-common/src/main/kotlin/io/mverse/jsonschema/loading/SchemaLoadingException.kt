package io.mverse.jsonschema.loading

import io.mverse.jsonschema.Schema
import io.mverse.jsonschema.SchemaException
import lang.URI

/**
 * Exception raised during the loading of a schema, if the provided document is invalid.
 */
class SchemaLoadingException(
    val location: URI, val report: LoadingReport, schema: Schema) :
    SchemaException(location, report.toString())
