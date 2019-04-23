/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.swagger.models.ComposedModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

/**
 * This class locates all collections in a Swagger object and replaces nulls
 * with empty collections.
 * <p>
 * This significantly simplifies downstream code since many null checks are
 * eliminated. Note that vendor extensions are never null, so we don't do
 * anything with them.
 */
public class SwaggerFiller {

	public static void fill(Swagger swagger) {

		SwaggerWalker.walk(swagger, new SwaggerWalker.Callbacks() {

			@Override
			public void swagger(Swagger swagger) {
				swagger.setSchemes(fillList(swagger.getSchemes()));
				swagger.setConsumes(fillList(swagger.getConsumes()));
				swagger.setProduces(fillList(swagger.getProduces()));
				swagger.setPaths(fillMap(swagger.getPaths()));
				swagger.setDefinitions(fillMap(swagger.getDefinitions()));
				swagger.setParameters(fillMap(swagger.getParameters()));
				swagger.setResponses(fillMap(swagger.getResponses()));
				swagger.setSecurityDefinitions(fillMap(swagger.getSecurityDefinitions()));
				swagger.setSecurity(fillList(swagger.getSecurity()));
				swagger.setTags(fillList(swagger.getTags()));
			}

			@Override
			public void path(String name, Path path) {
				fillList(path.getOperations());
				path.setParameters(fillList(path.getParameters()));
			}

			@Override
			public void model(Model model) {
				model.setProperties(fillMap(model.getProperties()));
				if (model instanceof ModelImpl) {
				}
			}

			@Override
			public void objectModel(ModelImpl objectModel) {
				// can't use fillMap for this because of bogus set method implemetnation in
				// ModelImpl
				if (objectModel.getProperties() == null) {
					objectModel.addProperty("x", new StringProperty()); // faults in an empty map first
					objectModel.getProperties().clear();
				}
				objectModel.setRequired(fillList(objectModel.getRequired()));
			}

			@Override
			public void allofModel(ComposedModel composedModel) {
				composedModel.setAllOf(fillList(composedModel.getAllOf()));
			}

			@Override
			public void bodyParameter(BodyParameter bodyParameter) {
				bodyParameter.setExamples(fillMap(bodyParameter.getExamples()));
			}

			@Override
			public void response(String status, Response response) {
				if (!(response instanceof RefResponse)) {
					response.setHeaders(fillMap(response.getHeaders()));
				}
			}

			@Override
			public void operation(HttpMethod httpMethod, Operation operation) {
				operation.setTags(fillList(operation.getTags()));
				operation.setParameters(fillList(operation.getParameters()));
				operation.setResponses(fillMap(operation.getResponses()));
				operation.setSchemes(fillList(operation.getSchemes()));
			}

			@Override
			public void property(String name, Property property) {
				if (property instanceof ObjectProperty) {
					ObjectProperty op = (ObjectProperty) property;
					op.setProperties(fillMap(op.getProperties()));
					op.setRequiredProperties(fillList(op.getRequiredProperties()));
				}
			}
		});

	}

	private static <T> List<T> fillList(List<T> items) {
		if (items == null) {
			items = Lists.newArrayList();
		}
		return items;
	}

	private static <K, V> Map<K, V> fillMap(Map<K, V> map) {
		if (map == null) {
			map = Maps.newHashMap();
		}
		return map;
	}
}
