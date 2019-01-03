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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.reprezen.genflow.api.GenerationException;

public class TraceTest extends Assert {

	private File tempDir = null;
	private File baseDir = null;
	private File genDir = null;
	private File dummyFile = null;
	private String dummyOutput;
	private GenTemplateTraceBuilder traceBuilder = null;

	@Before
	public void setup() throws IOException {
		tempDir = Files.createTempDir();
		System.out.println(tempDir);
		baseDir = new File(tempDir, "Dummy");
		genDir = new File(tempDir, "Dummy/generated");
		baseDir.mkdirs();
		genDir.mkdirs();
		dummyFile = new File(genDir, "dummy.txt");
		dummyOutput = "Dummy output";
		Files.write(dummyOutput, dummyFile, Charsets.UTF_8);
		traceBuilder = new GenTemplateTraceBuilder("bogusGenTemplateId").withBaseDirectory(baseDir);
	}

	@After
	public void teardown() throws IOException {
		FileUtils.deleteDirectory(tempDir);
	}

	@Test
	public void traceOutputFileIsRelativeGivenRelative()
			throws JsonParseException, JsonMappingException, GenerationException, IOException {
		traceOutputFileTest(dummyFile);
	}

	@Test
	public void traceOutputFileIsRelativeGivenAbsolute()
			throws JsonParseException, JsonMappingException, GenerationException, IOException {
		traceOutputFileTest(dummyFile.getAbsoluteFile());
	}

	private void traceOutputFileTest(File outputFile)
			throws GenerationException, JsonParseException, JsonMappingException, IOException {
		traceBuilder.newItem("file").withOutputFile(outputFile);
		GenTemplateTrace trace = traceBuilder.build();
		GenTemplateTraceItem traceItem = trace.getTraceItems().get(0);
		String relativePath = traceItem.getOutputRelativePath();
		assertTrue("Output path in trace item does not name expected file",
				new File(baseDir, relativePath).equals(dummyFile));

		assertTrue("Trace item output file is not expected file",
				traceItem.getOutputFile().getCanonicalPath().equals(dummyFile.getCanonicalPath()));

		File traceFile = new File(baseDir, "Dummy.trace.json");
		GenTemplateTraceSerializer.save(trace, traceFile);
		GenTemplateTrace loadedTrace = GenTemplateTraceSerializer.load(traceFile);
		GenTemplateTraceItem loadedTraceItem = loadedTrace.getTraceItems().get(0);
		String loadedRelativePath = loadedTraceItem.getOutputRelativePath();

		assertFalse("Output path in serialized trace item is absolute", new File(loadedRelativePath).isAbsolute());
		assertTrue("Output path in serialized trace item does not name expected file",
				new File(baseDir, loadedRelativePath).equals(dummyFile));

		assertTrue("Loaded trace item output file is not expected file",
				loadedTraceItem.getOutputFile().getCanonicalPath().equals(dummyFile.getCanonicalPath()));
		assertTrue("Unexpected data in loaded trace item output file",
				dummyOutput.equals(Files.toString(dummyFile, Charsets.UTF_8)));
	}
}
