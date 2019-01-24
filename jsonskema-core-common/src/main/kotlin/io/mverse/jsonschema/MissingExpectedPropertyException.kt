package io.mverse.jsonschema

import lang.json.KtObject

class MissingExpectedPropertyException(source: KtObject, property: String) :
    RuntimeException("Found ${source.keys}, but was expecting $property")
