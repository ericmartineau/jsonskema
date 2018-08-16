package io.mverse.jsonschema

import kotlinx.serialization.json.JsonObject

class MissingExpectedPropertyException(source: kotlinx.serialization.json.JsonObject, property: String) :
    RuntimeException("Found ${source.keys}, but was expecting $property")
