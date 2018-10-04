/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * One item of trace information produced by a GenTemplate.
 * 
 * @author Konstantin Zaitsev
 * @date Jun 10, 2015
 */
public final class GenTemplateTraceItem {

	// protected fields for better JavaDoc generation

	/** Trace object to whom this item belongs. */
	protected GenTemplateTrace trace;

	/**
	 * ID of trace item.
	 * <p>
	 * This value must be unique among all trace items in a trace.
	 */
	protected String id;

	/**
	 * The type of this trace item.
	 * <p>
	 * There is no predefined set of trace types; each GenTemplate should choose
	 * trace types that are meaningful in the context of the contents of the
	 * generated output file.
	 */
	protected String type;

	/**
	 * Output file path, as it will appear in the serialized form of this trace
	 * item.
	 * <p>
	 * This field is updated as a side-effect of calling
	 * {@link #setOutputFile(File)}, making use of the containing trace object's
	 * base directory.
	 */

	@JsonProperty
	private String outputRelativePath;

	/**
	 * Optional string identifying a structure contained within the overall value
	 * represented by the output file data.
	 * 
	 * @see com.modelsolv.reprezen.generators.api.source.ILocator
	 * 
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected String locator;

	/**
	 * Name-value pairs describing the generated element.
	 * <p>
	 * These properties may include properties copied or aggregated from dependent
	 * GenTemplates.
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected Map<String, String> properties = new HashMap<>();

	/**
	 * Source items that contributed the output item that this trace item describes.
	 */
	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	protected List<GenTemplateTraceSourceItem> sourceItems = new ArrayList<>();

	/** @see GenTemplateTraceItem#trace */
	public GenTemplateTrace getTrace() {
		return trace;
	}

	/** @see GenTemplateTraceItem#trace */
	public void setTrace(GenTemplateTrace trace) {
		this.trace = trace;
	}

	/** @see #id */
	public String getId() {
		return id;
	}

	/** @see #id */
	public void setId(String id) {
		this.id = id;
	}

	/** @see #type */
	public String getType() {
		return type;
	}

	/** @see #type */
	public void setType(String type) {
		this.type = type;
	}

	public String getOutputRelativePath() {
		return outputRelativePath;
	}

	/**
	 * Get the output file for this trace item.
	 * <p>
	 * The output file is constructed from the current {@link #outputRelativePath}.
	 * If the containing trace has a base directory set, it is used to resolve the
	 * relative path into a File object with the resolved path. Otherwise, a File
	 * object with the unresolved {@link #outputRelativePath} is returned.
	 * 
	 * @see #outputRelativePath
	 */
	@JsonIgnore
	public File getOutputFile() {
		try {
			File baseDirectory = trace != null ? trace.getBaseDirectory() : null;

			if (baseDirectory == null) {
				return new File(outputRelativePath);
			} else {
				Path basePath = Paths.get(trace.getBaseDirectory().getCanonicalPath());
				return basePath.resolve(outputRelativePath).toFile().getCanonicalFile();
			}
		} catch (IOException e) {
			// this should never happen, but we don't want to swallow it. So we wrap it as
			// an unchecked exception to
			// avoid forcing lots of code to deal with this
			throw new RuntimeException("Failed to resolve trace item output file with relative path "
					+ outputRelativePath + " and base " + trace.getBaseDirectory().toString(), e);
		}
	}

	/**
	 * Set the output file for this trace item.
	 * <p>
	 * The path of the given File is stored in {@link #outputRelativePath}. If the
	 * containing trace object has a base directory set, the relative path
	 * corresponding to that base directory is computed and saved; otherwise the
	 * path from the supplied File object is saved as-is.
	 * 
	 * @see #outputRelativePath
	 */
	@JsonIgnore
	public void setOutputFile(File outputFile) {
		File base = trace != null ? trace.getBaseDirectory() : null;
		if (base == null) {
			this.outputRelativePath = outputFile.getPath();
		} else {
			try {
				Path outputPath = Paths.get(outputFile.getCanonicalPath());
				Path basePath = Paths.get(base.getCanonicalPath());
				this.outputRelativePath = basePath.relativize(outputPath).toString();
			} catch (IOException e) {
				// this should never happen, but we don't want to swallow it. So we wrap it as
				// an unchecked exception to
				// avoid forcing lots of code to deal with this
				throw new RuntimeException("Failed to relativize trace item output file with path "
						+ outputFile.getPath() + " against base path " + base.toString(), e);
			}
		}
	}

	/** @see #locator */
	public String getLocator() {
		return locator;
	}

	/** @see #locator */
	public void setLocator(String locator) {
		this.locator = locator;
	}

	/** @see #properties */
	public Map<String, String> getProperties() {
		return properties;
	}

	/** @see #properties */
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	/** @see #sourceItems */
	public List<GenTemplateTraceSourceItem> getSources() {
		return sourceItems;
	}

	/** @see #sourceItems */
	public void setSourceItems(List<GenTemplateTraceSourceItem> sources) {
		this.sourceItems = sources;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		File outputFile = null;
		outputFile = getOutputFile();
		return "GenTemplateTraceItem [id=" + id + ", type=" + type + ", outputFile=" + outputFile
				+ ", outputRelativePath=" + outputRelativePath + ", locator=" + locator + ", properties=" + properties
				+ ", sourceItems=" + sourceItems + "]";
	}
}