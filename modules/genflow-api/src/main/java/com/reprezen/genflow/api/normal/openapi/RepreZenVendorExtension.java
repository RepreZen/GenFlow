/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.swagger.models.ExternalDocs;
import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.models.auth.SecuritySchemeDefinition;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.Property;

/**
 * Vendor extensions providing various bits of trace information left behind by
 * the normalizer
 */
@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE, isGetterVisibility = Visibility.NONE)
@JsonInclude(Include.NON_EMPTY)
public class RepreZenVendorExtension {
	public final static String EXTENSION_NAME = "x-reprezen-normalization";
	private final static ObjectMapper mapper = new ObjectMapper();

	private Boolean unresolvableRef = null;
	private String typeName = null;
	private String pointer = null;
	private Integer position = null;
	private String fileUrl = null;
	private String reason = null;

	public Integer getPosition() {
		return position;
	}

	public RepreZenVendorExtension setPosition(Integer position) {
		this.position = position;
		return this;
	}

	public String getPointer() {
		return pointer;
	}

	public RepreZenVendorExtension setPointer(String pointer) {
		this.pointer = pointer;
		return this;
	}

	public RepreZenVendorExtension setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
		return this;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public final RepreZenVendorExtension setUnresolvableRef(Boolean unresolvableRef) {
		this.unresolvableRef = unresolvableRef;
		return this;
	}

	public final Boolean isUnresolvableRef() {
		return this.unresolvableRef;
	}

	public RepreZenVendorExtension setReason(String reason) {
		this.reason = reason;
		return this;
	}

	public String getReason() {
		return reason != null ? reason : "";
	}

	public String getTypeName() {
		return typeName;
	}

	public RepreZenVendorExtension setTypeName(String typeName) {
		this.typeName = typeName;
		return this;
	}

	public boolean isEmpty() {
		return unresolvableRef == null && reason == null && pointer == null && position == null && fileUrl == null
				&& typeName == null;
	}

	/**
	 * Return the RZVE object for the given model component object (or JsonNode).
	 * <p>
	 * If the object does not yet have a vendor extension and the
	 * <code>create</code> parameter is true, then one is created and installed into
	 * the object as a side-effect, with one exception: If the item is a model
	 * component and its existing RZVE object is a JsonNode, that node is replaced
	 * with an equivalent RZVE object, even if create if false.
	 * <p>
	 * Note that when the incoming item is a JsonNode, the <code>create</code>
	 * parameter gets a bit tricky, because the newly created RZVE object cannot be
	 * incorporated into the incoming object as a side-effect (since it is not,
	 * itself a JsonNode object). In this case, a separate operation is required by
	 * the caller to install the returned vendor extension into the object after
	 * adjusting its properties. See the {@link #set(Object)) method below.
	 */
	public static RepreZenVendorExtension get(Object item, boolean create) {
		if (item instanceof ObjectNode) {
			return getFromJsonNode((ObjectNode) item);
		} else {
			Map<String, Object> veMap = getVendorExtensionMap(item);
			return veMap != null ? getExtension(veMap, create) : new RepreZenVendorExtension();
		}
	}

	public static RepreZenVendorExtension get(Object item) {
		return get(item, false);
	}

	public static RepreZenVendorExtension getOrCreate(Object item) {
		return get(item, true);
	}

	public static void removeIfEmpty(Object item) {
		if (item instanceof ObjectNode) {
			removeFromJsonNodeIfEmpty((ObjectNode) item);

		} else {
			RepreZenVendorExtension rzve = RepreZenVendorExtension.get(item);
			if (rzve != null && rzve.isEmpty()) {
				Map<String, Object> veMap = getVendorExtensionMap(item);
				if (veMap != null) {
					veMap.remove(EXTENSION_NAME);
				}
			}
		}
	}

	private static Map<String, Object> getVendorExtensionMap(Object item) {
		// note that the following places where the spec allows vendor extensions do not
		// support them in swagger-models
		// and are therefore not implemented here:
		// * info.contact
		// * info.license
		// * paths (the overall object, not individual paths)
		// * responses (the overall object, not individual responses)
		// * xml (in models properties)
		// * security requirement
		// * oauth2 security scope
		// Also note that the following VE locations are all covered by Property:
		// * items object in array definitions
		// * header in response headers

		if (item instanceof Swagger) {
			return ((Swagger) item).getVendorExtensions();
		} else if (item instanceof Info) {
			return ((Info) item).getVendorExtensions();
		} else if (item instanceof Parameter) {
			return ((Parameter) item).getVendorExtensions();
		} else if (item instanceof Response) {
			return ((Response) item).getVendorExtensions();
		} else if (item instanceof Path) {
			return ((Path) item).getVendorExtensions();
		} else if (item instanceof Operation) {
			return ((Operation) item).getVendorExtensions();
		} else if (item instanceof Model) {
			return ((Model) item).getVendorExtensions();
		} else if (item instanceof Property) {
			return ((Property) item).getVendorExtensions();
		} else if (item instanceof ExternalDocs) {
			return ((ExternalDocs) item).getVendorExtensions();
		} else if (item instanceof Tag) {
			return ((Tag) item).getVendorExtensions();
		} else if (item instanceof SecuritySchemeDefinition) {
			return ((SecuritySchemeDefinition) item).getVendorExtensions();
		} else {
			return null;
		}
	}

	/**
	 * N.B. This must be called after manipulating a RZVE attached to a JsonNode,
	 * since the value returned by getOrAdd() in this case does not retain any
	 * association with the JsonNode itself. This is because that object is not
	 * itself a JsonNode and so cannot be part of a JsonNode structure. For all
	 * model component objects, getOrAdd() returns a RZVE object that is already
	 * attached to the underlying object.
	 */
	public void set(Object item) {
		if (item instanceof ObjectNode) {
			((ObjectNode) item).replace(EXTENSION_NAME, mapper.convertValue(this, JsonNode.class));
		}
	}

	private static RepreZenVendorExtension getFromJsonNode(ObjectNode node) {
		JsonNode rzve = node.get(EXTENSION_NAME);
		if (rzve != null) {
			return mapper.convertValue(rzve, RepreZenVendorExtension.class);
		} else {
			return new RepreZenVendorExtension();
		}
	}

	private static void removeFromJsonNodeIfEmpty(ObjectNode node) {
		JsonNode rzveNode = node.get(EXTENSION_NAME);
		if (rzveNode != null) {
			RepreZenVendorExtension rzve = mapper.convertValue(rzveNode, RepreZenVendorExtension.class);
			if (rzve.isEmpty()) {
				node.remove(EXTENSION_NAME);
			}
		}
	}

	private static RepreZenVendorExtension getExtension(Map<String, Object> veMap, boolean create) {
		if (veMap == null) {
			return null;
		}
		Object veObj = veMap.get(EXTENSION_NAME);
		if (veObj == null) {
			if (create) {
				veObj = new RepreZenVendorExtension();
				veMap.put(EXTENSION_NAME, veObj);
			}
			return (RepreZenVendorExtension) veObj;
		} else if (veObj instanceof RepreZenVendorExtension) {
			return (RepreZenVendorExtension) veObj;
		} else if (veObj instanceof Map) {
			// this is an RZVE object in JsonNode form, which happens because Swagger
			// Parser, not knowing about our
			// class, cannot possibly convert the value on our behalf. So we need to convert
			// it.
			RepreZenVendorExtension ve = mapper.convertValue(veObj, RepreZenVendorExtension.class);
			veMap.put(EXTENSION_NAME, ve);
			return ve;
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return String.format("RZVE: typeName=%s, unresolvableRef=%s, pointer=%s, position=%s, reason=%s]", typeName,
				unresolvableRef, pointer, position, reason);
	}
}
