/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import static com.reprezen.genflow.api.trace.GenTemplateTrace.SOURCE_DATA_ROLE;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Strings;
import com.reprezen.genflow.api.target.GenTarget;

/**
 * This class is a builder with a fluent API for building
 * {@link GenTemplateTrace} objects.
 * 
 * @author Konstantin Zaitsev
 * @date Jun 10, 2015
 */
public class GenTemplateTraceBuilder {

	/** Trace object under construction. */
	private final GenTemplateTrace trace = new GenTemplateTrace();

	public GenTemplateTraceBuilder(String genTemplateId) {
		trace.setGenTemplateId(genTemplateId);
	}

	/**
	 * Latest output file for reuse.
	 * <p>
	 * This becomes the default for subsequent trace items.
	 */
	private File currentOutputFile;

	/** Counter for automatic ID creation. */
	private final AtomicLong counter = new AtomicLong();

	/**
	 * Creates a new builder for a GenTemplateTrace object.
	 * 
	 * @return the trace builder
	 * 
	 * @see #build()
	 */
	public static GenTemplateTraceBuilder newBuilder(String genTemplateId) {
		return new GenTemplateTraceBuilder(genTemplateId);
	}

	/**
	 * Set the GenTemplate ID for the current trace item.
	 * 
	 * @param genTemplateId
	 *            required ID of GenTemplate
	 * @return the trace builder
	 */
	public GenTemplateTraceBuilder withTemplateId(String genTemplateId) {
		trace.setGenTemplateId(genTemplateId);
		return this;
	}

	/**
	 * Set the base directory for this trace object to that of a given
	 * {@link GenTarget} object.
	 * 
	 * @param target
	 *            the GenTarget object
	 * @return the trace builder
	 */
	public GenTemplateTraceBuilder forGenTarget(GenTarget target) {
		return withBaseDirectory(target.getBaseDir());
	}

	/**
	 * Set the base directory for this trace object.
	 * 
	 * @param baseDirectory
	 *            the base directory
	 * @return the trace builder
	 */
	public GenTemplateTraceBuilder withBaseDirectory(File baseDirectory) {
		trace.setBaseDirectory(baseDirectory);
		return this;
	}

	/**
	 * Start building a new trace item for this trace.
	 * 
	 * @param type
	 *            required trace item type
	 * @return the trace builder
	 */
	public GenTemplateTraceItemBuilder newItem(String type) {
		return new GenTemplateTraceItemBuilder().ofType(type);
	}

	/**
	 * Utility method to add a trace item for a static resource.
	 * <p>
	 * This can be used when a resource is copied verbatim to an output file, e.g.
	 * images, javascript or CSS files referenced by generated HTML.
	 * <p>
	 * The new item will have:
	 * <ul>
	 * <li>type: staticResource
	 * <li>outputFile: output file containing a copy of the resource
	 * <li>a single source item with
	 * <ul>
	 * <li>role: sourceData ({@link GenTemplateTrace.SOURCE_DATA_ROLE})
	 * <li>sourceName: _resource ({@link GenTemplateTrace.RESOURCE_SOURCE_NAME})
	 * <li>locator: full path of copied resource
	 * </ul>
	 * </ul>
	 * 
	 * @param resourcePath
	 *            full resource path
	 * @param outputFile
	 *            output file containing a copy of the resource
	 * @return The trace builder.
	 */
	@SuppressWarnings("nls")
	public GenTemplateTraceBuilder addStaticResource(String resourcePath, File outputFile) {
		newItem("staticResource").withOutputFile(outputFile).withResourceSourceItem(SOURCE_DATA_ROLE, resourcePath);
		return this;
	}

	/**
	 * Utility method to add a trace item for a one element of a tree of resources
	 * copied to an output directory.
	 * <p>
	 * The full resource path is constructed by prepending the
	 * <code>resourceRoot</code> path of
	 * <code>outputFile<code> relative to <code>outputRoot</code>.
	 * 
	 * @param resourceRoot
	 *            path designating the root of the copied resource tree
	 * @param outputFile
	 *            path to the file to which the resource was copied
	 * @param outputRoot
	 *            path of the output directory to which the tree was copied
	 * @return The trace builder.
	 * @see #addStaticResource(String, File)
	 */
	public GenTemplateTraceBuilder addStaticResource(String resourceRoot, File outputFile, File outputRoot) {
		String sourceTail = outputRoot.toURI().relativize(outputFile.toURI()).getPath();
		return addStaticResource(resourceRoot + "/" + sourceTail, outputFile);
	}

	/**
	 * Utility method to add a simple item for a file generated from the primary
	 * source.
	 * <p>
	 * The new item will have:
	 * <ul>
	 * <li>type: file
	 * <li>outputFile: the supplied output file
	 * <li>A single source item with:
	 * <ul>
	 * <li>role: sourceData ({@link GenTemplateTrace.SOURCE_DATA_ROLE})
	 * <li>sourceName: _primary {@link GenTemplateTrace.PRIMARY_SOURCE_NAME})
	 * <li>locator: the supplied locator
	 * </ul>
	 * </ul>
	 * 
	 * @param outputFile
	 *            The generated output file.
	 * @param locator
	 *            Optional locator identifying a structure contained within the
	 *            primary source.
	 * @return the trace builder
	 */
	@SuppressWarnings("nls")
	public GenTemplateTraceBuilder addPrimaryItem(File outputFile, String locator) {
		newItem("file").withOutputFile(outputFile).withPrimarySourceItem(locator);
		return this;
	}

	/**
	 * Finalize and return built trace object.
	 * 
	 * @return trace object
	 */
	public GenTemplateTrace build() {
		return trace;
	}

	/**
	 * Builder to create and fill a {@link GenTemplateTraceItem}.
	 * 
	 * @author Konstantin Zaitsev
	 * @date Jun 11, 2015
	 */
	public class GenTemplateTraceItemBuilder {

		/** The trace item under construction. */
		private final GenTemplateTraceItem item;

		/**
		 * Creates and initializes a new trace item.
		 * <p>
		 * The new trace item is immediately added to the trace and is pre-filled with
		 * current defaults:
		 * <ul>
		 * <li>trace: the containing trace object
		 * <li>id: the next available automatic id
		 * <li>outputFile: the current output file (whatever was set in the most recent
		 * call of {@link #withOutputFile(File)})
		 * </ul>
		 * 
		 * @return The new trace item
		 */
		private GenTemplateTraceItemBuilder() {
			item = new GenTemplateTraceItem();
			item.setTrace(trace);
			item.setId(String.valueOf(counter.incrementAndGet()));
			if (currentOutputFile != null) {
				item.setOutputFile(currentOutputFile);
			}
			trace.getTraceItems().add(item);
		}

		/**
		 * Sets the type of this trace item.
		 * 
		 * @param type
		 *            trace item type
		 * @return the trace item builder
		 * @see GenTemplateTraceItem#type
		 */
		public GenTemplateTraceItemBuilder ofType(String type) {
			item.setType(type);
			return this;
		}

		/**
		 * Sets the output file path of the trace item.
		 * <p>
		 * The given output file will become the default for subsequent trace items
		 * added to this trace using the builder.
		 * 
		 * @param outputFile
		 *            the output file
		 * 
		 * @return the trace item builder.
		 * @see GenTemplateTraceItem#outputFile
		 */
		public GenTemplateTraceItemBuilder withOutputFile(File outputFile) {
			item.setOutputFile(outputFile);
			currentOutputFile = outputFile;
			return this;
		}

		/**
		 * Sets optional locator for trace item's output item.
		 * 
		 * @param locator
		 *            the locator
		 * @returns the trace item builder
		 * @see GenTemplateTraceItem#locator
		 */
		public GenTemplateTraceItemBuilder withLocator(String locator) {
			item.setLocator(locator);
			return this;
		}

		/**
		 * Adds a string property to the trace item.
		 * 
		 * @param name
		 *            the property name
		 * @param value
		 *            the property value
		 * @return the trace item builder
		 */
		public GenTemplateTraceItemBuilder withProperty(String name, String value) {
			item.properties.put(name, value);
			return this;

		}

		/**
		 * Adds a source item to this trace item.
		 * 
		 * @param role
		 *            source role
		 * @param sourceName
		 *            source name
		 * @param locator
		 *            optional locator
		 * @return the trace item builder
		 * @see GenTemplateTraceSourceItem
		 * @see GenTemplateTraceItem#sourceItems
		 */
		public GenTemplateTraceItemBuilder withSourceItem(String role, String sourceName, String locator) {
			GenTemplateTraceSourceItem source = new GenTemplateTraceSourceItem();
			source.setRole(role);
			source.setSourceModel(sourceName);
			source.setLocator(Strings.nullToEmpty(locator));

			item.getSources().add(source);
			return this;
		}

		/**
		 * Add a source item without a locator to this trace item.
		 * 
		 * @see #withSourceItem(String, String, String)
		 */
		public GenTemplateTraceItemBuilder withSourceItem(String role, String sourceModel) {
			return withSourceItem(role, sourceModel, null);
		}

		/**
		 * Add a resource source item to this trace item.
		 * <p>
		 * The source name will be set to {@link GenTemplateTrace.RESOURCE_SOURCE_NAME})
		 * 
		 * @param role
		 * @param locator
		 *            full resource path
		 * @return the trace item builder
		 */
		public GenTemplateTraceItemBuilder withResourceSourceItem(String role, String locator) {
			return withSourceItem(role, GenTemplateTrace.RESOURCE_SOURCE_NAME, locator);
		}

		/**
		 * Add a primary source item to this trace item.
		 * <p>
		 * The role will be set to {@link GenTemplateTrace.SOURCE_DATA_ROLE}, and the
		 * source name will be set to {@link GenTemplateTrace.SOURCE_DATA_ROLE}.
		 * 
		 * @param locator
		 *            the locator
		 * @return the trace item builder
		 * @see #withSourceItem(String, String, String)
		 */
		public GenTemplateTraceItemBuilder withPrimarySourceItem(String locator) {
			return withSourceItem(GenTemplateTrace.SOURCE_DATA_ROLE, GenTemplateTrace.PRIMARY_SOURCE_NAME, locator); // $NON-NLS-1$
		}

		/**
		 * Copies a property from the trace of a dependency to this trace item, and adds
		 * the dependency as a source for this trace item.
		 * 
		 * @param traceItem
		 *            dependency trace item
		 * @param genTemplateName
		 *            template name of dependency
		 * @param property
		 *            name of property to copy
		 * @return the trace item builder
		 */
		public GenTemplateTraceItemBuilder withPropertyFromTraceItem(GenTemplateTraceItem traceItem,
				String genTemplateName, String property) {
			withProperty(property, traceItem.properties.get(property));
			withSourceItem(traceItem.type, genTemplateName, traceItem.locator);
			return this;
		}

		/**
		 * Finalizes and returns the newly built trace item.
		 * 
		 * @return the newly built trace item
		 */
		public GenTemplateTraceItem build() {
			return item;
		}

	}
}
