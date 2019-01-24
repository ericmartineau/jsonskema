
var jsonskema = require("../../../build/node_modules/jsonskema-js.js");
var jk = jsonskema.io.mverse.jsonschema;

var schema = {
  properties: {
    name: {
      minLength: 12,
      maxLength:20
    }
  }
};

var schemaInput = JSON.stringify(schema);
var jsonSchema = jk.readJsonSchema(schemaInput);

var properties = jsonSchema[jk.keyword.PROPERTIES]

// builder.propertySchema("name", function(schema) {
//   schema.minLength(12);
//   schema.maxLength(20);
// });
// var schema = builder.build();
console.log(jsonSchema.toString());
