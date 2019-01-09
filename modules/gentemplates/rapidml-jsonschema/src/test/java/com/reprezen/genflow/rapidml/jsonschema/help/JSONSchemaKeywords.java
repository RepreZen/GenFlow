/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

@SuppressWarnings("nls")
public interface JSONSchemaKeywords {

    static final String SCHEMA_VERSION_FIELD_NAME = "$schema";
    static final String SCHEMA_VERSION_DEFAULT = "http://json-schema.org/draft-04/schema#";

    static final String TYPE = "type";
    static final String ENUM = "enum";
    static final String DESCRIPTION = "description";
    static final String PROPERTIES = "properties";
    static final String REQUIRED = "required";
    static final String PROPERTIES_DEF_PREFIX = "#/definitions/";
    static final String DEFINITIONS_FIELD_NAME = "definitions";
    static final String REF_FIELD_NAME = "$ref";
    static final String FORMAT = "format";
    static final String MEDIA = "media";
    static final String BINARY_ENCODING = "binaryEncoding";
    static final String ENCODING_BASE64 = "base64";
    static final String MIN_ITEMS = "minItems";
    static final String MAX_ITEMS = "maxItems";
    static final String ITEMS = "items";

    static final String TYPE_OBJECT = "object";
    static final String TYPE_ARRAY = "array";
    static final String TYPE_STRING = "string";
    static final String TYPE_BOOLEAN = "boolean";
    static final String TYPE_NUMBER = "number";
    static final String TYPE_INTEGER = "integer";

    static final String FORMATE_DATE = "date";
    static final String FORMATE_DATETIME = "date-time";
    static final String FORMATE_DOUBLE = "double";
    static final String FORMATE_FLOAT = "float";
    static final String FORMATE_INT64 = "int64";

    // constraints
    static final String MIN_LENGTH = "minLength";
    static final String MAX_LENGTH = "maxLength";
    static final String PATTERN = "pattern";
    static final String MINIMUM = "minimum";
    static final String EXCLUSIVE_MINIMUM = "exclusiveMinimum";
    static final String MAXIMUM = "maximum";
    static final String EXCLUSIVE_MAXIMUM = "exclusiveMaximum";
}
