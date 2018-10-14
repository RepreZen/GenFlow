/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Class representing a source item attached to a @{link
 * {@link GenTemplateTraceItem}.
 * <p>
 * A source item represents one of the inputs to a GenTemplate, or a portion of
 * an input, specifically tied to the creation of the output item described the
 * the containing trace item.
 * 
 * @author Konstantin Zaitsev
 * @date Jun 10, 2015
 */
public final class GenTemplateTraceSourceItem {
	// protected fields for better JavaDoc generation

	/**
	 * Role of this source model in contributing to the output item.
	 * <p>
	 * There is no set of predefined roles, though there is a role,
	 * {@link GenTemplateTrace.SOURCE_DATA_ROLE}, that can serve as a fall-back if
	 * no more specific role is specified. Developers should choose roles that are
	 * meaningful in the context of the generated output.
	 */
	protected String role;

	/**
	 * GenTarget dependency name.
	 * <p>
	 * This is generally the name by which the GenTemplate accesses the souce file
	 * containing the source item. There are also two special values that do not
	 * correspond to such names:
	 * <dl>
	 * <dt>{@link GenTemplateTrace.PRIMARY_SOURCE_NAME}</dt>
	 * <dd>Indicates that this source item came from the GenTemplate's primary
	 * source</dd>
	 * <dt>{@link GenTemplateTrace.RESOURCE_SOURCE_NAME}</dt>
	 * <dd>Indicates that the input was a Java resource packaged with the
	 * GenTemplate itself</dd>
	 * </dl>
	 */
	protected String sourceName;

	/**
	 * ID or path to identify this source item within the overall source.
	 * <p>
	 * Examples of strings that could serve as locators are XML element id values,
	 * XPath locators, JSONPath locators, fully qualified structure names, line
	 * number ranges, etc. The form of a locator is intimately tied to the format of
	 * the associated source.
	 * <p>
	 * In the special case of a resource-based source item, the locator should be
	 * the full resource path of the contributing resource (treating the entire
	 * resource collection in this case as the overall source, and the identified
	 * resource as the source item within that source).
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected String locator;

	/** @see #role */
	public String getRole() {
		return role;
	}

	/** @see #role */
	public void setRole(String role) {
		this.role = role;
	}

	/** @see #sourceName */
	public String getSourceModel() {
		return sourceName;
	}

	/** @see #sourceName */
	public void setSourceModel(String sourceModel) {
		this.sourceName = sourceModel;
	}

	/** @see #locator */
	public String getLocator() {
		return locator;
	}

	/** @see #locator */
	public void setLocator(String locator) {
		this.locator = locator;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "GenTemplateTraceSourceItem [role=" + role + ", sourceModel=" + sourceName + ", locator=" + locator
				+ "]";
	}
}
