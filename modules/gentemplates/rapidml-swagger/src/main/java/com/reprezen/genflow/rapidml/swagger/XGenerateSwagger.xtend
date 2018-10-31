/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.google.common.base.Splitter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelOutputItem
import com.reprezen.genflow.common.jsonschema.JsonSchemaHelper
import com.reprezen.genflow.common.xtend.ExtensionsHelper
import com.reprezen.genflow.common.xtend.ZenModelHelper
import com.reprezen.rapidml.AuthenticationFlows
import com.reprezen.rapidml.AuthenticationTypes
import com.reprezen.rapidml.Constraint
import com.reprezen.rapidml.Extensible
import com.reprezen.rapidml.HasSecurityValue
import com.reprezen.rapidml.HttpMessageParameterLocation
import com.reprezen.rapidml.LengthConstraint
import com.reprezen.rapidml.MessageParameter
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.RapidmlFactory
import com.reprezen.rapidml.RegExConstraint
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.SecurityScheme
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.SourceReference
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.TemplateParameter
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.ValueRangeConstraint
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.util.OAuth2Parameters
import io.swagger.models.Info
import io.swagger.models.Model
import io.swagger.models.Operation
import io.swagger.models.Path
import io.swagger.models.RefModel
import io.swagger.models.Response
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.Tag
import io.swagger.models.auth.ApiKeyAuthDefinition
import io.swagger.models.auth.BasicAuthDefinition
import io.swagger.models.auth.In
import io.swagger.models.auth.OAuth2Definition
import io.swagger.models.auth.SecuritySchemeDefinition
import io.swagger.models.parameters.AbstractParameter
import io.swagger.models.parameters.AbstractSerializableParameter
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.HeaderParameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.AbstractNumericProperty
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.BooleanProperty
import io.swagger.models.properties.DateProperty
import io.swagger.models.properties.DateTimeProperty
import io.swagger.models.properties.DecimalProperty
import io.swagger.models.properties.DoubleProperty
import io.swagger.models.properties.FloatProperty
import io.swagger.models.properties.IntegerProperty
import io.swagger.models.properties.LongProperty
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import io.swagger.models.properties.StringProperty
import io.swagger.util.Json
import io.swagger.util.Yaml
import java.io.IOException
import java.math.BigDecimal
import java.net.URI
import java.util.Collections
import java.util.HashMap
import java.util.List
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.DumperOptions.FlowStyle

class XGenerateSwagger extends ZenModelOutputItem {
	val public static String OUTPUT_FORMAT_PARAM = 'format'
	val public static String OPTION_RETAIN_EMPTY_PARAMATERS = 'retainEmptyParameters'
	extension ZenModelHelper = new ZenModelHelper
	extension JsonSchemaHelper = new JsonSchemaHelper
	extension ExtensionsHelper = new ExtensionsHelper

	val String defaultFormat;

	val JsonSchemaForSwaggerGenerator jsonSchemaGenerator

	new() {
		this(SwaggerOutputFormat.JSON);
	}

	new(SwaggerOutputFormat format) {
		this(format, new JsonSchemaForSwaggerGenerator())
	}

	new(SwaggerOutputFormat format, JsonSchemaForSwaggerGenerator jsonSchemaGenerator) {
		defaultFormat = format.toString;
		this.jsonSchemaGenerator = jsonSchemaGenerator
	}

	override init(IGenTemplateContext context) {
		super.init(context)
	}

	override generate(ZenModel model) {
		val templateParam = context.genTargetParameters
		val swagger = getSwagger(model)
		val mapper = Json.pretty()
		var content = mapper.writeValueAsString(swagger)

		// workaround to use JSON Schema generator for Swagger
		val tempMapper = new ObjectMapper()
		val jsonSchemasDefinitionsNode = jsonSchemaGenerator.generateDefinitionsNode(model, templateParam)
		val swaggerNode = (tempMapper.readTree(content) as ObjectNode).set("definitions", jsonSchemasDefinitionsNode)

		val format = if (templateParam?.get(OUTPUT_FORMAT_PARAM) !== null)
				templateParam.get(OUTPUT_FORMAT_PARAM)
			else
				defaultFormat;

		if (SwaggerOutputFormat.YAML.toString == format) {
			val ObjectMapper swaggerYamlMapper = Yaml::mapper();
			val foldMultiline = templateParam?.get(XSwaggerGenTemplate::FOLD_MULTILINE)
			if (foldMultiline instanceof Boolean && (foldMultiline as Boolean)) {
				val DumperOptions opts = new DumperOptions();
				opts.setDefaultFlowStyle(FlowStyle::BLOCK);
				val yaml = new org.yaml.snakeyaml.Yaml(opts);
				// Note that we are using SnakeYAML directly here. See https://github.com/ModelSolv/RepreZen/pull/1348
				return yaml.dump(new ObjectMapper().convertValue(swaggerNode, typeof(Object)));
			}
			return swaggerYamlMapper.writeValueAsString(swaggerNode)
		} else {
			return tempMapper.writerWithDefaultPrettyPrinter.writeValueAsString(swaggerNode)
		}
	}

	def Swagger getSwagger(ZenModel model) {
		val Swagger swagger = new Swagger

		// casting to Extensible, otherwise Xtend does not recognize inherited methods
		val groups = (model as Extensible).extensions.filter[it.name.startsWith("openAPI.tags.")]
		if (!groups.isEmpty) {
			groups.forEach [
				val tag = new Tag
				tag.name = it.name.substring(13) // 13 == "openAPI.tags.".length()
				val description = it.value
				tag.description = description
				swagger.addTag(tag)
			]
		}
		setVendorExtensions(model, swagger)

		if (model.resourceAPIs.empty) {
			addDefaultResourceAPI(model)
		}

		val resourceAPI = model.resourceAPIs.get(0)

		val uri = URI.create(resourceAPI.baseURI ?: 'http://localhost')
		swagger.basePath = if(uri.path !== null && !uri.path.empty) uri.path else '/'
		swagger.host = uri.host + if(uri.port >= 0) ':' + uri.port else ''
		if (uri.scheme !== null) {
			swagger.addScheme(Scheme.forValue(uri.scheme))
		}

		swagger.info = new Info()
		swagger.info.version = if(!Strings::isNullOrEmpty(resourceAPI.version)) resourceAPI.version else '1.0.0'
		swagger.info.title = resourceAPI.name
		swagger.info.description = getDocumentation(resourceAPI)
		// Is it a right mapping?
		// setVendorExtensions(resourceAPI, swagger.info)
		// global media types
		resourceAPI.definedMediaTypes.forEach [
			{
				swagger.addConsumes(it.name)
				swagger.addProduces(it.name)
			}
		]

		// resources 
		model.resourceAPIs.map[ownedResourceDefinitions].flatten.forEach [
			val tag = new Tag
			tag.name = it.name
			tag.description = getDocumentation(it)
			swagger.addTag(tag)
			// make uri absolute because it is required by Swagger UI
			val pathUri = '/' + if(it.URI !== null) it.URI.toString else ''
			swagger.path(pathUri, createSwaggerPath(it))
		]
		// ZEN-2962 - we should output paths: {} when there are no paths objects
		if (swagger.paths === null) {
			swagger.paths = Collections.emptyMap()
		}

		getAllUsedSecuritySchemes(model).forEach[swagger.addSecurityDefinition(it.name, createSecurityScheme(it))]

		return swagger
	}

	def getAllUsedSecuritySchemes(ZenModel model) {
		val result = Lists::newArrayList
		if (model.securitySchemesLibrary !== null) {
			result.addAll(model.securitySchemesLibrary.securitySchemes)
		}
		model.eAllContents.filter(typeof(HasSecurityValue)).toList.map[securedBy].flatten.forEach[result.add(it.scheme)]
		return result
	}

	def SecuritySchemeDefinition createSecurityScheme(SecurityScheme scheme) {
		switch (scheme.type) {
			case AuthenticationTypes::OAUTH2: {
				val result = new OAuth2Definition
				setVendorExtensions(scheme, result)
				result.flow = switch (scheme.flow) {
					case AuthenticationFlows::IMPLICIT: "implicit"
					case AuthenticationFlows::PASSWORD: "password"
					case AuthenticationFlows::APPLICATION: "application"
					case AuthenticationFlows::ACCESS_CODE: "accessCode"
				}

				val authorizationUrlSetting = scheme.settings.findFirst[it.name == OAuth2Parameters::AUTHORIZATION_URL]
				result.authorizationUrl = authorizationUrlSetting?.value

				// Use tokenUrl if it's defined, otherwise try using accessTokenUrl
				val tokenUrlSetting = scheme.settings.findFirst[it.name == OAuth2Parameters::TOKEN_URL]
				if (tokenUrlSetting !== null) {
					result.tokenUrl = tokenUrlSetting.value
				} else {
					val accessTokenUrlSetting = scheme.settings.findFirst[it.name == OAuth2Parameters::ACCESS_TOKEN_URL]
					result.tokenUrl = accessTokenUrlSetting?.value
				}
				scheme.scopes.forEach[result.addScope(it.name, getDocumentation(it))]
				result.type = "oauth2"
				return result;
			}
			case AuthenticationTypes::BASIC: {
				val result = new BasicAuthDefinition
				setVendorExtensions(scheme, result)
				result.type = "basic"
				return result;
			}
			case AuthenticationTypes::CUSTOM: {
				val result = new ApiKeyAuthDefinition
				setVendorExtensions(scheme, result)

				// TODO Swagger requires exactly one param for ApiKey auth
				if (!scheme.parameters.empty) {
					val param = scheme.parameters.get(0)
					result.name = param.name
					val in = if(param.type == HttpMessageParameterLocation::HEADER) In::HEADER else In::QUERY
					result.setIn(in)
				}
				result.type = "apiKey"
				return result;
			}
		}
	}

	def Path createSwaggerPath(ResourceDefinition res) {
		val Path path = new Path()
		// Xtend not recognizing supertype again:(
		setVendorExtensions(res as Extensible, path)
		if ((res as ServiceDataResource).URI !== null) {
			(res as ServiceDataResource).URI.uriParameters.forEach [
				// use matrix parameters as QueryParameter because Swagger doesnot support Matrix parameters
				val AbstractSerializableParameter<?> param = if(it instanceof TemplateParameter) new PathParameter else new QueryParameter
				param.description = getDocumentation(it)
				param.name = it.name
				param.type = it.sourceReference.type.JSONSchemaTypeName
				if (it.sourceReference.type.JSONSchemaTypeFormat !== null) {
					param.format = it.sourceReference.type.JSONSchemaTypeFormat
				}
				setVendorExtensions(it, param)
				switch (param) {
					PathParameter: {

						// path parameter should be always true according Swagger spec 
						param.required = true
						param.defaultValue = it.^default
					}
					QueryParameter: {
						param.required = it.required
						param.defaultValue = it.^default
					}
				}
				path.addParameter(param)
			]
		}
		res.methods.forEach[path.createSwaggerOperation(it)]
		return path
	}

	def createSwaggerOperation(Path path, Method method) {
		val Operation operation = new Operation()
		setVendorExtensions(method, operation)

		operation.operationId = method.id
		operation.description = getDocumentation(method)

		// casting to Extensible, otherwise Xtend does not recognize inherited methods
		val groups = (method as Extensible).extensions.filter[it.name == "openAPI.tags"]
		if (!groups.isEmpty) {
			groups.forEach [
				Splitter.on(",").split(it.value).forEach[tag|operation.addTag(tag.trim)]
			]
		} else {
			operation.addTag(method.containingResourceDefinition.name)
		}

		method.responses.forEach [
			operation.addResponse(if(it.statusCode > 0) it.statusCode.toString else '200', getResponse(it))
		]
		method.responses.map[mediaTypes].flatten.map[name].toSet.forEach[operation.safeAddProduces(it)]

		method.request.mediaTypes.map[name].toSet.forEach[operation.safeAddConsumes(it)]
		method.request.parameters.filter[it.httpLocation != HttpMessageParameterLocation::QUERY].forEach [
			operation.addParameter(messageHeaderParameter)
		]

		method.request.parameters.filter[it.httpLocation == HttpMessageParameterLocation::QUERY].forEach [
			operation.addParameter(messageQueryParameter)
		]
		if (!shouldRetainEmptyParameters) {
			if (operation.parameters.isEmpty) {
				operation.parameters = null
			}
		}

		val requestTypeName = getDefinitionName(method.request)
		if (requestTypeName !== null) {
			val param = new BodyParameter()
			operation.addParameter(param)
			param.name = requestTypeName
			param.description = "Request body" // TODO
			param.description = getMessageDocumentation(method.request)
			param.schema = getReferenceToType(requestTypeName)
			setVendorExtensions(method.request, param)
			param.required = true
		}

		switch (method.httpMethod.getName()) {
			case "GET": path.get = operation
			case "POST": path.post = operation
			case "PUT": path.put = operation
			case "DELETE": path.delete = operation
			case "OPTIONS": path.options = operation
			case "PATCH": path.patch = operation
		}

		method.securedBy.forEach[operation.addSecurity(it.scheme.name, it.scopes.map[it.name])]
	}

	/**
	 * @returns creates Swagger Response from ZEN TypedResponse 
	 */
	def getResponse(TypedResponse rapidResponse) {
		val swaggerResponse = new Response()
		// Casting because Xtend editor does not recognize Extensible as supertype
		setVendorExtensions(rapidResponse as Extensible, swaggerResponse)

		val example = rapidResponse.allExamples.head

		if (example !== null && example.body !== null) {
			val type = rapidResponse.mediaTypes.head
			if (type !== null) {
				swaggerResponse.example(rapidResponse.mediaTypes.head.name, example.body.renderExample(type.name))
			}
		}

		val typeName = getDefinitionName(rapidResponse)

		swaggerResponse.description = getMessageDocumentation(rapidResponse)

		if (typeName !== null) {
			val property = new RefProperty
			property.$ref = "#/definitions/" + typeName
			swaggerResponse.schema = property
		}
		rapidResponse.parameters.forEach [
			if (swaggerResponse.headers === null) {
				swaggerResponse.headers = new HashMap<String, Property>
			}
			if (it.arrayProperty) {
				val prop = new ArrayProperty

				prop.items = it.createSwaggerPropertyWithConstraints()
				prop.description = getDocumentation(it)
				swaggerResponse.headers.put(it.name, prop)
			} else {
				val prop = it.createSwaggerPropertyWithConstraints()
				prop.description = getDocumentation(it)
				swaggerResponse.headers.put(it.name, prop)
			}
		]

		return swaggerResponse
	}

	def private renderExample(String example, String type) {
		switch (type) {
			case "application/json":
				example.jsonExample
			default:
				example
		}
	}

	val static jsonMapper = new ObjectMapper()
	val static yamlMapper = new ObjectMapper(new YAMLFactory())

	def private getJsonExample(String example) {
		try {
			return jsonMapper.readTree(example)
		} catch (IOException exception) {
		}
		try {
			return yamlMapper.readTree(example.removeIndentation)
		} catch (Exception exception) {
		}
		return example
	}

	// We first remove any initial or final lines that contain only whitespace, then compute the indentation of the first
	// line among those that remain. If all lines begin with that exact indentation, we strip it from all lines to yield
	// the final example text. Otherwise, we just return the example text as-is, which will almost certainly fail YAML
	// parse and will therefore be copied as a string-valued swagger example
	def private removeIndentation(String text) {
		var lines = text.split("(?m)^")
		lines = lines.dropWhile[it.trim.isEmpty]
		lines = lines.reverse.dropWhile[it.trim.isEmpty].toList.reverse
		val indent = findIndent(lines.head)
		if (lines.forall[it.startsWith(indent)]) {
			return lines.map[it.substring(indent.length())].join("\n")
		} else {
			return text
		}
	}

	def private findIndent(String line) {
		if (line !== null) {
			for (i : 0 ..< line.length) {
				if (" \t".indexOf(line.charAt(i)) < 0) {
					return line.substring(0, i)
				}
			}
		}
		return line
	}

	def getMessageDocumentation(TypedMessage message) {
		var result = getDocumentation(message)
		if (result.nullOrEmpty) {
			val Structure dataType = if (message.actualType !== null) {
					message.actualType
				} else {
					if (message.resourceType instanceof ServiceDataResource) {
						(message.resourceType as ServiceDataResource).dataType
					}
				};
			if (dataType !== null) {
				result = getDocumentation(dataType)
			}
		}
		return result
	}

	def String getDefinitionName(TypedMessage message) {
		if (message === null) {
			return null;
		}
		var String typeName = if (message.actualType !== null) {
				jsonSchemaGenerator.getDefinitionName(message)
			} else if (message.resourceType !== null) {
				jsonSchemaGenerator.getDefinitionName(message.resourceType as ServiceDataResource)
			} else {
				null
			}
		return typeName
	}

	def Model getReferenceToType(String typeName) {
		val ref = new RefModel
		ref.$ref = "#/definitions/" + typeName
		ref
	}

	def Property createSwaggerPropertyWithConstraints(MessageParameter parameter) {
		val SourceReference sourceReference = parameter.sourceReference
		val prop = getSwaggerProperty(sourceReference.type.name)
		prop.setConstraints(parameter.messageParameterConstraints)
		prop
	}

	def Property getSwaggerProperty(String typeName) {
		switch (typeName) {
			case #["anyURI", "duration", "gMonth", "gMonthDay", "gDay", "gYearMonth", "gYear", "QName", "time",
				"string", "NCName"].contains(typeName):
				new StringProperty
			case "boolean":
				new BooleanProperty
			case "date":
				new DateProperty
			case "dateTime":
				new DateTimeProperty
			case "decimal":
				new DecimalProperty
			case "double":
				new DoubleProperty
			case "float":
				new FloatProperty
			case "integer":
				new IntegerProperty
			case "int":
				new IntegerProperty
			case "long":
				new LongProperty
			default: {
				val prop = new RefProperty
				prop.$ref = "#/definitions/" + typeName
				prop
			}
		}
	}

	def setConstraints(Property property, List<Constraint> constraints) {
		if (constraints === null || constraints.empty) {
			return
		}

		switch (property) {
			StringProperty: {
				constraints.filter(LengthConstraint).forEach [
					if (it.setMinLength) {
						property.minLength = it.minLength
					}
					if (it.setMaxLength) {
						property.maxLength = it.maxLength
					}
				]
				constraints.filter(RegExConstraint).forEach [
					property.pattern = it.pattern
				]
			}
			AbstractNumericProperty: {
				constraints.filter(ValueRangeConstraint).forEach [
					if (it.minValue !== null) {
						property.minimum = new BigDecimal(it.minValue)
						property.exclusiveMinimum = it.minValueExclusive
					}
					if (it.maxValue !== null) {
						property.maximum = new BigDecimal(it.maxValue)
						property.exclusiveMaximum = it.maxValueExclusive
					}
				]
			}
		}
	}

	def getMessageHeaderParameter(MessageParameter parameter) {
		val swaggerParameter = new HeaderParameter
		if (parameter.arrayProperty) {

			// TODO - Make sure this is the proper way to handle arrays in new swagger model. Formerly:
			// swaggerParameter.array = true
			// swaggerParameter.items = parameter.createSwaggerPropertyWithConstraints()
			val items = parameter.createSwaggerPropertyWithConstraints
			swaggerParameter.property = new ArrayProperty(items)
		} else {
			swaggerParameter.property = parameter.createSwaggerPropertyWithConstraints()
		}
		swaggerParameter.description = getDocumentation(parameter)
		swaggerParameter.name = parameter.name
		swaggerParameter.required = parameter.required
		setVendorExtensions(parameter, swaggerParameter)
		return swaggerParameter
	}

	def private setVendorExtensions(Extensible rapidElement, Swagger swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def private setVendorExtensions(Extensible rapidElement, Path swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def private setVendorExtensions(Extensible rapidElement, Operation swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def private setVendorExtensions(Extensible rapidElement, AbstractParameter swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def private setVendorExtensions(Extensible rapidElement, Response swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def private setVendorExtensions(Extensible rapidElement, SecuritySchemeDefinition swaggerObj) {
		val extensions = getRapidExtensions(rapidElement)
		extensions.forEach[swaggerObj.setVendorExtension(it.name, it.value)]
	}

	def getMessageQueryParameter(MessageParameter parameter) {
		val swaggerParameter = new QueryParameter
		if (parameter.arrayProperty) {

			// TOOD is this the right way to handle arrays in V2? Formerly was:
			// swaggerParameter.array = true
			// swaggerParameter.items = parameter.createSwaggerPropertyWithConstraints()
			val items = parameter.createSwaggerPropertyWithConstraints()
			swaggerParameter.property = new ArrayProperty(items);
		} else {
			swaggerParameter.property = parameter.createSwaggerPropertyWithConstraints()
		}
		swaggerParameter.description = getDocumentation(parameter)
		swaggerParameter.name = parameter.name
		swaggerParameter.required = parameter.required
		setVendorExtensions(parameter, swaggerParameter)
		return swaggerParameter
	}

	def List<Constraint> getMessageParameterConstraints(MessageParameter parameter) {
		val ref = parameter.sourceReference
		switch (ref) {
			PropertyReference: {
				var property = parameter.containingMessage.includedProperties.findFirst [
					it.baseProperty == ref.conceptualFeature
				]
				if (property === null) {
					return ref.conceptualFeature.allConstraints
				}
				return property.allConstraints
			}
		}
	}

	def addDefaultResourceAPI(ZenModel model) {
		val resourceAPI = RapidmlFactory.eINSTANCE.createResourceAPI
		resourceAPI.baseURI = 'http://localhost'
		resourceAPI.name = model.name
		model.resourceAPIs.add(resourceAPI)
	}

	private def boolean shouldRetainEmptyParameters() {
		val option = context?.genTargetParameters?.get(OPTION_RETAIN_EMPTY_PARAMATERS)
		return if (option instanceof Boolean) {
			option as Boolean
		} else if (option === null) {
			false
		} else
			throw new IllegalArgumentException(
				String.format("Only boolean values are allowed for '%s', current value is of type %s",
					OPTION_RETAIN_EMPTY_PARAMATERS, option.class.simpleName))
	}

	private static def safeAddConsumes(Operation op, String mediaType) {
		if(op.consumes === null || !op.consumes.contains(mediaType)) op.addConsumes(mediaType)
	}

	private static def safeAddProduces(Operation op, String mediaType) {
		if(op.produces === null || !op.produces.contains(mediaType)) op.addProduces(mediaType)
	}

}
