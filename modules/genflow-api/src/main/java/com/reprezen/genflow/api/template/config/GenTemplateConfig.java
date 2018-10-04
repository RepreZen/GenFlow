/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.config;

import java.util.Collections;
import java.util.List;

public class GenTemplateConfig {

	private List<PrerequisiteConfig> prerequisites;
	private PrimarySourceConfig primarySource;
	private List<NamedSourceConfig> namedSources;
	private List<OutputItemConfig> outputItems;
	private List<StaticResourceConfig> staticResources;
	private List<ParameterConfig> parameters;

	public List<PrerequisiteConfig> getPrerequisites() {
		return prerequisites;
	}

	public void setPrerequisites(List<PrerequisiteConfig> prerequisites) {
		this.prerequisites = prerequisites;
	}

	public PrimarySourceConfig getPrimarySource() {
		return primarySource;
	}

	public void setPrimarySource(PrimarySourceConfig primarySource) {
		this.primarySource = primarySource;
	}

	public List<NamedSourceConfig> getNamedSources() {
		return namedSources;
	}

	public void setNamedSources(List<NamedSourceConfig> namedSources) {
		this.namedSources = namedSources;
	}

	public List<OutputItemConfig> getOutputItems() {
		return outputItems;
	}

	public void setOutputItems(List<OutputItemConfig> outputItems) {
		this.outputItems = outputItems;
	}

	public List<StaticResourceConfig> getStaticResources() {
		return staticResources;
	}

	public void setStaticResources(List<StaticResourceConfig> staticResources) {
		this.staticResources = staticResources != null ? staticResources
				: Collections.<StaticResourceConfig>emptyList();
	}

	public List<ParameterConfig> getParameters() {
		return parameters;
	}

	public void setParameters(List<ParameterConfig> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Replace null collection values with empty collections.
	 * <p>
	 * This makes consuming code far simpler and less error-prone. Jackson leaves
	 * collection values null if the corresponding properities were missing in the
	 * json. Call this after deserialization to fix things up.
	 */
	public void complete() {
		if (prerequisites == null) {
			prerequisites = Collections.emptyList();
		}
		if (namedSources == null) {
			namedSources = Collections.emptyList();
		}
		if (outputItems == null) {
			outputItems = Collections.emptyList();
		}
		if (staticResources == null) {
			staticResources = Collections.emptyList();
		}
		if (parameters == null) {
			parameters = Collections.emptyList();
		}
	}

}