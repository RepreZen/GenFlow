/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.models.Swagger;

/**
 * Utility class to set RepreZenVendorExtension data on various model objects or
 * JSON tree nodes
 */
public class OpenApiMarkers {
	public static void markUnresolvableRef(Object item, String ref, String reason) {
		RepreZenVendorExtension.getOrCreate(item).setUnresolvableRef(true).setReason(reason).set(item);
	}

	public static void removeUnresolvableRef(Object item) {
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
		if (rzve != null) {
			rzve.setUnresolvableRef(null).setReason(null).set(item);
			RepreZenVendorExtension.removeIfEmpty(item);
		}
	}

	public static void markTypeName(Object item, String typeName) {
		RepreZenVendorExtension.getOrCreate(item).setTypeName(typeName).set(item);
	}

	public static void removeTypeName(Object item) {
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
		if (rzve != null) {
			rzve.setTypeName(null).set(item);
			RepreZenVendorExtension.removeIfEmpty(item);
		}
	}

	public static void markJsonPointer(Object item, String pointerString) {
		RepreZenVendorExtension.getOrCreate(item).setPointer(pointerString).set(item);
	}

	public static void removeJsonPointer(Object item) {
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
		if (rzve != null) {
			rzve.setPointer(null).set(item);
			RepreZenVendorExtension.removeIfEmpty(item);
		}
	}

	public static void markPosition(Object item, int position) {
		RepreZenVendorExtension.getOrCreate(item).setPosition(position).set(item);
	}

	public static void removePosition(Object item) {
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
		if (rzve != null) {
			rzve.setPosition(null).set(item);
			RepreZenVendorExtension.removeIfEmpty(item);
		}
	}

	public static void markFile(Object item, String fileUrl) {
		RepreZenVendorExtension.getOrCreate(item).setFileUrl(fileUrl).set(item);
	}

	public static void removeFile(Object item) {
		RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
		if (rzve != null) {
			rzve.setFileUrl(null).set(item);
			RepreZenVendorExtension.removeIfEmpty(item);
		}
	}

	public static void removeMarkers(Swagger model, final Options options) {
		if (!options.isRetainAllExtensionData()) {
			SwaggerWalker.walk(model, new SwaggerWalker.Callbacks() {

				@Override
				public void any(Object obj) {
					removeItemMarkers(obj, options);
				}
			});
		}
	}

	public static void removeMarkers(JsonNode node, Options options) {
		if (node.has(RepreZenVendorExtension.EXTENSION_NAME)) {
			removeItemMarkers(node, options);
		}
		for (Entry<String, JsonNode> child : iterable(node.fields())) {
			removeMarkers(child.getValue(), options);
		}
		for (JsonNode element : iterable(node.elements())) {
			removeMarkers(element, options);
		}
	}

	private static void removeItemMarkers(Object item, Options options) {
		if (!options.isRetainOrderingExtensionData()) {
			removePosition(item);
		}
		if (!options.isRetainPointerExtensionData()) {
			removeJsonPointer(item);
		}
		if (!options.isRetainFileExtensionData()) {
			removeFile(item);
		}
		if (!options.isRetainTypeNameExtensionData()) {
			removeTypeName(item);
		}
		if (!options.isRetainBadRefExtensionData()) {
			removeUnresolvableRef(item);
		}
	}

	private static <T> Iterable<T> iterable(final Iterator<T> iterator) {
		return new Iterable<T>() {
			@Override
			public Iterator<T> iterator() {
				return iterator;
			}
		};
	}
}
