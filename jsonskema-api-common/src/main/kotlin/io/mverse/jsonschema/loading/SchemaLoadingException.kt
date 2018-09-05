package io.mverse.jsonschema.loading

import io.mverse.jsonschema.SchemaException
import lang.Name
import lang.URI

/**
 * Exception raised during the loading of a schema, if the provided document is invalid.
 */
class SchemaLoadingException(
    @Name("location")
    val location: URI,
    @Name("report")
    val report: LoadingReport) :
    SchemaException(location, report.toString())
