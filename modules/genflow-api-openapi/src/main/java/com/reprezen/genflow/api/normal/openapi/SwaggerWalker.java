package com.reprezen.genflow.api.normal.openapi;

import java.util.Map.Entry;

import io.swagger.models.ArrayModel;
import io.swagger.models.ComposedModel;
import io.swagger.models.Contact;
import io.swagger.models.ExternalDocs;
import io.swagger.models.HttpMethod;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.SecurityRequirement;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.Xml;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.SerializableParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;

/**
 * Walk a Swagger model and invoke callbacks for each object encountered.
 * <p>
 * In general, anything that can have a vendor extension according to the
 * swagger specification gets a callback, except in the few cases (like the
 * "paths" object) where the swagger-models code has no provision for vendor
 * extensions.
 * <p>
 * Note that multiple callbacks can be called for the same object. E.g. both
 * #parameter and #bodyParamter are called for a body parameter. And #any is
 * called for every object.
 * 
 * @author Andy Lowry
 *
 */
public class SwaggerWalker {

	private Swagger swagger;
	private Callbacks callbacks;

	public SwaggerWalker(Swagger swagger, Callbacks callbacks) {
		this.swagger = swagger;
		this.callbacks = callbacks;
	}

	public static void walk(Swagger swagger, Callbacks callbacks) {
		new SwaggerWalker(swagger, callbacks).walk();
	}

	public void walk() {
		callbacks.swagger(swagger);
		Info info = swagger.getInfo();
		if (info != null) {
			callbacks.info(info);
			callbacks.any(info);
			walkInfo(info);
		}

		if (swagger.getPaths() != null) {
			for (Entry<String, Path> path : swagger.getPaths().entrySet()) {
				callbacks.path(path.getKey(), path.getValue());
				callbacks.any(path.getValue());
				walkPath(path.getValue());
			}
		}

		if (swagger.getDefinitions() != null) {
			for (Entry<String, Model> def : swagger.getDefinitions().entrySet()) {
				callbacks.definition(def.getKey(), def.getValue());
				callbacks.any(def.getValue());
				walkModel(def.getValue());
			}
		}

		if (swagger.getResponses() != null) {
			for (Entry<String, Response> response : swagger.getResponses().entrySet()) {
				callbacks.topLevelResponse(response.getKey(), response.getValue());
				callbacks.any(response.getValue());
				walkResponse(response.getValue());
			}
		}

		if (swagger.getParameters() != null) {
			for (Entry<String, Parameter> param : swagger.getParameters().entrySet()) {
				callbacks.topLevelParamter(param.getKey(), param.getValue());
				callbacks.any(param.getValue());
				walkParameter(param.getValue());
			}
		}

		if (swagger.getSecurityDefinitions() != null) {
			for (Entry<String, SecuritySchemeDefinition> entry : swagger.getSecurityDefinitions().entrySet()) {
				callbacks.securityScheme(entry.getKey(), entry.getValue());
				callbacks.any(entry.getValue());
			}
		}

		if (swagger.getSecurity() != null) {
			for (SecurityRequirement requirement : swagger.getSecurity()) {
				callbacks.securityRequirement(requirement);
				callbacks.any(requirement);
			}
		}

		if (swagger.getTags() != null) {
			for (Tag tag : swagger.getTags()) {
				callbacks.tag(tag);
				callbacks.any(tag);
			}
		}

		if (swagger.getExternalDocs() != null) {
			callbacks.externalDocs(swagger.getExternalDocs());
			callbacks.any(swagger.getExternalDocs());
		}
	}

	private void walkInfo(Info info) {
		// info object itself has already been visited
		Contact contact = info.getContact();
		if (contact != null) {
			callbacks.contact(contact);
			callbacks.any(contact);
		}
		License license = info.getLicense();
		if (license != null) {
			callbacks.license(license);
			callbacks.any(license);
		}
	}

	private void walkPath(Path path) {
		// path object has already been visited
		if (path.getOperationMap() != null) {
			for (Entry<HttpMethod, Operation> op : path.getOperationMap().entrySet()) {
				callbacks.operation(op.getKey(), op.getValue());
				callbacks.any(op.getValue());
				walkOperation(op.getValue());
			}
		}
		if (path.getParameters() != null) {
			for (Parameter param : path.getParameters()) {
				callbacks.pathParameter(param);
				callbacks.parameter(param);
				callbacks.any(param);
				walkParameter(param);
			}
		}
	}

	private void walkOperation(Operation op) {
		// operation has already been visted
		if (op.getParameters() != null) {
			for (Parameter param : op.getParameters()) {
				callbacks.parameter(param);
				callbacks.any(param);
				walkParameter(param);
			}
		}
		if (op.getResponses() != null) {
			for (Entry<String, Response> response : op.getResponses().entrySet()) {
				callbacks.response(response.getKey(), response.getValue());
				callbacks.any(response.getValue());
				walkResponse(response.getValue());
			}
		}
		ExternalDocs externalDocs = op.getExternalDocs();
		if (externalDocs != null) {
			callbacks.externalDocs(externalDocs);
			callbacks.any(externalDocs);
		}
	}

	private void walkModel(Model model) {
		// model has already been visited
		if (model instanceof ModelImpl) {
			callbacks.objectModel((ModelImpl) model);
			walkObjectModel((ModelImpl) model);
		} else if (model instanceof ArrayModel) {
			callbacks.arrayModel((ArrayModel) model);
			walkArrayModel((ArrayModel) model);
		} else if (model instanceof ComposedModel) {
			callbacks.allofModel((ComposedModel) model);
			walkAllofModel((ComposedModel) model);
		}
		ExternalDocs externalDocs = model.getExternalDocs();
		if (externalDocs != null) {
			callbacks.externalDocs(externalDocs);
			callbacks.any(externalDocs);
		}
	}

	private void walkObjectModel(ModelImpl model) {
		// model has already been visited both as model and as object model
		if (model.getProperties() != null) {
			for (Entry<String, Property> prop : model.getProperties().entrySet()) {
				callbacks.property(prop.getKey(), prop.getValue());
				callbacks.any(prop.getValue());
				walkProperty(prop.getValue());
			}
		}
		Property additionalProperties = model.getAdditionalProperties();
		if (additionalProperties != null) {
			callbacks.property(null, additionalProperties);
			walkProperty(additionalProperties);
		}
		Xml xml = model.getXml();
		if (xml != null) {
			callbacks.xml(xml);
			callbacks.any(xml);
		}
	}

	private void walkArrayModel(ArrayModel model) {
		// model has already been visited both as model and as array model
		Property items = model.getItems();
		if (items != null) {
			callbacks.arrayItems(items);
			callbacks.any(items);
			walkProperty(items);
		}
	}

	private void walkAllofModel(ComposedModel model) {
		// model has already been visited both as model and as array model
		if (model.getAllOf() != null) {
			for (Model component : model.getAllOf()) {
				callbacks.model(model);
				callbacks.allofComponent(component);
				callbacks.any(component);
				walkModel(component);
			}
		}
	}

	private void walkParameter(Parameter parameter) {
		// parameter has already been visited
		if (parameter instanceof BodyParameter) {
			callbacks.bodyParameter((BodyParameter) parameter);
			walkBodyParameter((BodyParameter) parameter);
		} else if (parameter instanceof SerializableParameter) {
			walkSerializableParameter((SerializableParameter) parameter);
		}
	}

	private void walkBodyParameter(BodyParameter parameter) {
		// parameter has already been visited as a parameter and as a body parameter
		Model schema = parameter.getSchema();
		if (schema != null) {
			callbacks.model(schema);
			callbacks.any(schema);
			walkModel(schema);
		}
	}

	private void walkSerializableParameter(SerializableParameter parameter) {
		// parameter has already been visited as a parameter
		Property items = parameter.getItems();
		if (items != null) {
			callbacks.property(null, items);
			callbacks.any(items);
			walkProperty(items);
		}
	}

	private void walkResponse(Response response) {
		// response has already been visited
		Model schema = response.getResponseSchema();
		if (schema != null) {
			callbacks.model(schema);
			callbacks.any(schema);
			walkModel(schema);
		}
		if (response.getHeaders() != null) {
			for (Entry<String, Property> header : response.getHeaders().entrySet()) {
				callbacks.header(header.getKey(), header.getValue());
				callbacks.property(null, header.getValue());
				callbacks.any(header.getValue());
				walkProperty(header.getValue());
			}
		}
	}

	private void walkProperty(Property prop) {
		// property has already been visited
		Xml xml = prop.getXml();
		if (xml != null) {
			callbacks.xml(xml);
			callbacks.any(xml);
		}
		if (prop instanceof ArrayProperty) {
			callbacks.arrayProperty((ArrayProperty) prop);
			walkArrayProperty((ArrayProperty) prop);
		} else if (prop instanceof MapProperty) {
			walkMapProperty((MapProperty) prop);
		}
	}

	private void walkArrayProperty(ArrayProperty prop) {
		// property has already been visited as a property and as an array property
		Property items = prop.getItems();
		if (items != null) {
			callbacks.property(null, items);
			callbacks.any(items);
			walkProperty(items);
		}
	}

	private void walkMapProperty(MapProperty prop) {
		// property has already been visited
		Property additionalProperties = prop.getAdditionalProperties();
		if (additionalProperties != null) {
			callbacks.property(null, additionalProperties);
			walkProperty(additionalProperties);
		}
	}

	public static class Callbacks {
		public void swagger(Swagger swagger) {
		}

		public void info(Info info) {
		}

		public void contact(Contact contact) {
		}

		public void license(License license) {
		}

		public void path(String name, Path path) {
		}

		public void operation(HttpMethod httpMethod, Operation operation) {
		}

		public void parameter(Parameter parameter) {
		}

		public void bodyParameter(BodyParameter bodyParameter) {
		}

		public void pathParameter(Parameter parameter) {
		}

		public void response(String status, Response response) {
		}

		public void header(String name, Property header) {
		}

		public void topLevelParamter(String name, Parameter parameter) {
		}

		public void topLevelResponse(String name, Response response) {
		}

		public void definition(String name, Model definition) {
		}

		public void model(Model model) {
		}

		public void objectModel(ModelImpl objectModel) {
		}

		public void property(String name, Property property) {
		}

		public void arrayProperty(ArrayProperty property) {
		}

		public void arrayModel(ArrayModel arrayModel) {
		}

		public void allofModel(ComposedModel composedModel) {
		}

		public void allofComponent(Model component) {
		}

		public void xml(Xml xml) {
		}

		public void arrayItems(Property items) {
		}

		public void externalDocs(ExternalDocs externalDocs) {
		}

		public void tag(Tag tag) {
		}

		public void securityScheme(String name, SecuritySchemeDefinition securityScheme) {
		}

		public void securityRequirement(SecurityRequirement securityRequirement) {
		}

		public void any(Object obj) {
		}

	}
}
