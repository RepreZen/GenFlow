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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.genflow.api.GenerationException;

/**
 * Class to save and load trace objects to/from files, in the form of JSON
 * objects.
 * <p>
 * Though the relevant logic does not appear here (see
 * {@link GenTemplateTraceItem}), it is important to note that the output files
 * associated with trace items are not directly serialized. If the trace object
 * as a whole has a base directory at the time a trace item output file is set,
 * the output file path is relativized against that base directory, and it is
 * the resulting path that is serialized. Otherwise, the given file path is
 * directly serialized.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@reprezen.com>
 * 
 */
public class GenTemplateTraceSerializer {

	/**
	 * Compute the output file to contained the serialized structure.
	 * <p>
	 * The output file is in the same directory as the .gen file for the controlling
	 * GenTarget, with the same name but with the extension
	 * <code>.trace.json</code>.
	 * 
	 * @param genTargetFile
	 *            controlling GenTarget
	 * @return output file
	 */
	public static File getTraceFileForGenTarget(File genTargetFile) {
		return new File(genTargetFile.getParentFile(), genTargetFile.getName().replaceAll("gen$", "trace.json")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Serialize a trace file to its output file.
	 * 
	 * @param trace
	 *            the trace object
	 * @param traceFileLocation
	 *            the output file
	 * @throws GenerationException
	 */
	public static void save(GenTemplateTrace trace, File traceFileLocation) throws GenerationException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.writerWithDefaultPrettyPrinter().writeValue(traceFileLocation, trace);
		} catch (Exception e) {
			throw new GenerationException("Cannot save trace file.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Deserialize a trace object from a saved serialization.
	 * <p>
	 * If a base directory is provided, all serialized trace item output file paths
	 * will be resolved against that directory in the process of deserialization.
	 * 
	 * @param traceFile
	 *            the file containing the serialized data
	 * @param baseDirectory
	 *            base directory for the trace object, or null for none.
	 * @return the trace object
	 * @throws GenerationException
	 */
	public static GenTemplateTrace load(File traceFile, File baseDirectory) throws GenerationException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			GenTemplateTrace trace = mapper.readValue(traceFile, GenTemplateTrace.class);
			trace.setBaseDirectory(baseDirectory);
			return trace;
		} catch (Exception e) {
			throw new GenerationException("Cannot load trace file.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Deserialize a trace object, using the containing directory as the base
	 * directory
	 * 
	 * @param traceFile
	 *            file containing serialized trace data
	 * @return trace object
	 * @throws GenerationException
	 */
	public static GenTemplateTrace load(File traceFile) throws GenerationException {
		return load(traceFile, traceFile.getParentFile());
	}
}
