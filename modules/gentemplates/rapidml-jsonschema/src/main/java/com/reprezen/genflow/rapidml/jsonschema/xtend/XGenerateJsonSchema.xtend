/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.xtend

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.Lists
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelOutputItem
import com.reprezen.genflow.rapidml.jsonschema.xtend.builder.JsonSchemaNodeFactory
import com.reprezen.rapidml.ZenModel
import java.util.Map.Entry

class XGenerateJsonSchema extends ZenModelOutputItem {

	val public static String IS_SWAGGER_FORMAT_PARAM = "isSwaggerFormat"
	final val mapper = new ObjectMapper()

	final private JsonSchemaNodeFactory jsonSchemaNodeFactory

	new() {
		this(JsonSchemaFormat::STANDARD)
	}

	new(JsonSchemaFormat schemaFormat) {
		this(new JsonSchemaNodeFactory(schemaFormat))
	}

	new(JsonSchemaNodeFactory jsonSchemaNodeFactory) {
		this.jsonSchemaNodeFactory = jsonSchemaNodeFactory
	}

	override init(IGenTemplateContext context) {
		super.init(context)
		jsonSchemaNodeFactory.options = Options.fromParams(context.genTargetParameters)
	}

	override generate(ZenModel model) {
		val result = mapper.writerWithDefaultPrettyPrinter.writeValueAsString(model.jsonSchemaNode)
		return result
	}

	def protected getJsonSchemaNode(ZenModel model) {
		val root = mapper.createObjectNode()
		root.put("$schema", "http://json-schema.org/draft-04/schema#"). //
		put("description", '''Schema for «model.name» model'''). //
		put("type", "object"). //
		put("title", model.name)
		val definitions = jsonSchemaNodeFactory.generateDefinitionsNode(model)
		root.set("definitions", definitions)
		val properties = root.putObject("properties")
		for (Entry<String, JsonNode> definition : Lists.newArrayList(definitions.fields)) {
			val String key = definition.getKey()
			properties.putObject(key).put("$ref", "#/definitions/" + key)
		}
		return root
	}

}
