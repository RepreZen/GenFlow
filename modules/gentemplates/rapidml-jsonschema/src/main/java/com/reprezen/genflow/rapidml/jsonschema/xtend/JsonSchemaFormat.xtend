/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.xtend

class JsonSchemaFormat {
	public final boolean inlineSimpleTypes
	/** Use boolean, not arrays for the value of "required" property as in JSON Schema v3*/
	public final boolean defineRequiredElementsInJsonSchemaV3Style
	public final boolean useSwaggerStyleBase64Binary
	
	new(boolean inlineSimpleTypes, boolean useJsonSchemaV3Required, boolean useSwaggerStyleBase64Binary) {
		this.inlineSimpleTypes = inlineSimpleTypes
		this.defineRequiredElementsInJsonSchemaV3Style = useJsonSchemaV3Required
		this.useSwaggerStyleBase64Binary = useSwaggerStyleBase64Binary
	}
	val public static STANDARD = new JsonSchemaFormat(false, false, false);
	val public static SWAGGER = new JsonSchemaFormat(false, false, true);
	val public static RAML = new JsonSchemaFormat(true, true, false);
	val public static JSON_SCHEMA_V3 = new JsonSchemaFormat(false, true, false);
	
}