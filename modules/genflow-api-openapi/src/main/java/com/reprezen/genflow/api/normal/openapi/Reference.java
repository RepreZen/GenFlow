/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Charsets;

public class Reference {

	public static final String UNRESOLVABLE_NAME = "_UNRESOLVABLE";

	private final String refString;
	private String fragment = null;
	private URL url;
	private String urlString; // for use in hashcode and equals, since they are expensive for URL
	private boolean isValid = true;
	private String invalidReason = null;
	private boolean isObjectRef = false;
	private ObjectType objectType = null;
	private String objectName = null;
	private Integer modelVersion;

	public Reference(String refString, Integer modelVersion) {
		this(refString, null, modelVersion);
	}

	public Reference(URL url, Integer modelVersion) {
		this(url.toString(), null, modelVersion);
	}

	public Reference(ObjectType section, String objectName, Reference context, Integer modelVersion) {
		this("#" + section.getPath(modelVersion) + "/" + encodeForJsonPointer(objectName), context, modelVersion);
	}

	public Reference(String refString, Reference context, Integer modelVersion) {
		this.refString = refString;
		this.modelVersion = modelVersion;
		String[] refParts = refString.split("#", 2);
		if (refParts.length > 1) {
			this.fragment = maybeDecodeFragment(refParts[1]);
			if (!fragment.startsWith("/")) {
				throw new IllegalArgumentException("JSON Pointer must start with '/'");
			}
		}
		try {
			if (context == null) {
				this.url = new URL(refParts[0]).toURI().normalize().toURL();
			} else if (context.isValid()) {
				this.url = new URL(context.getUrl(), refParts[0]);
			} else {
				this.isValid = false;
				this.invalidReason = "Invalid resolution context";
			}
			this.urlString = url != null ? url.toString() : "";
		} catch (MalformedURLException | URISyntaxException e) {
			this.isValid = false;
			this.invalidReason = e.getMessage();
			this.urlString = refParts[0];
		}
		setObjectRefInfo();
	}

	private void setObjectRefInfo() {
		if (fragment != null) {
			ObjectType.find(fragment, modelVersion).ifPresent(type -> {
				this.objectType = type;
				this.isObjectRef = true;
				this.objectName = decodeFromJsonPointer(type.getObjectName(fragment, modelVersion));
			});
		}
	}

	private static String decodeFromJsonPointer(String x) {
		return x.replaceAll("~1", "/").replaceAll("~0", "~");
	}

	private static String encodeForJsonPointer(String x) {
		return x.replaceAll("~", "~0").replaceAll("/", "~1");
	}

	public static Reference fromNode(JsonNode refNode, Reference base, Integer modelVersion) {
		Optional<String> refString = Util.getRefString(refNode);
		return refString.isPresent() ? new Reference(refString.get(), base, modelVersion) : null;
	}

	public String getRefString() {
		return refString;
	}

	public URL getUrl() {
		return url;
	}

	public String getFragment() {
		return fragment;
	}

	public String getCanonicalRefString() {
		if (fragment == null) {
			return getCanonicalFileRefString();
		} else {
			return getCanonicalFileRefString() + "#" + fragment;
		}
	}

	public String getCanonicalFileRefString() {
		return urlString;
	}

	public Integer getModelVersion() {
		return modelVersion;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setInvalidReason(String invalidReason) {
		this.invalidReason = invalidReason;
	}

	public boolean isModelObjectRef() {
		return isObjectRef;
	}

	public boolean isDefinitionRef() {
		return objectType == ObjectType.DEFINITION;
	}

	public boolean isParameterRef() {
		return objectType == ObjectType.PARAMETER;
	}

	public boolean isResponseRef() {
		return objectType == ObjectType.RESPONSE;
	}

	public boolean isPathRef() {
		return objectType == ObjectType.PATH;
	}

	public ObjectType getSection() {
		return objectType;
	}

	public String getSectionPath() {
		return objectType != null ? objectType.getPathString(modelVersion) : null;
	}

	public String getObjectName() {
		return objectName;
	}

	public JsonNode getRefNode() {
		ObjectNode refNode = JsonNodeFactory.instance.objectNode();
		refNode.put("$ref", refString);
		return refNode;
	}

	public Reference getBadRef() {
		return new Reference(getBadRefString(), this, null);
	}

	public JsonNode getBadRefNode() {
		ObjectNode refNode = JsonNodeFactory.instance.objectNode();
		refNode.put("$ref", getBadRefString());
		return refNode;
	}

	private String getBadRefString() {
		return "#/" + UNRESOLVABLE_NAME + "/" + refString;
	}

	private String maybeDecodeFragment(String fragment) {
		try {
			return URLDecoder.decode(fragment, Charsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			return fragment;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fragment == null) ? 0 : fragment.hashCode());
		result = prime * result + ((urlString == null) ? 0 : urlString.hashCode());
		return result;
	}

	public String getInvalidReason() {
		return invalidReason;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Reference other = (Reference) obj;
		if (fragment == null) {
			if (other.fragment != null)
				return false;
		} else if (!fragment.equals(other.fragment))
			return false;
		if (urlString == null) {
			if (other.urlString != null)
				return false;
		} else if (!urlString.equals(other.urlString))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("Ref[url=%s,frag=%s]", url.toString(), fragment);
	}
}