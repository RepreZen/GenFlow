/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.test.common;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader;

public abstract class GeneratorTestFixture extends TestWatcher {

	private final TemporaryFolder scratchDirRule = new TemporaryFolder();

	private final String extension;
	protected File generatedFile;
	private File scratchDir;

	private ZenModelLoader loader;

	public GeneratorTestFixture(String extension) {
		this.extension = extension;
		this.loader = new ZenModelLoader();
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return scratchDirRule.apply(super.apply(base, description), description);
	}

	@Override
	protected void starting(Description description) {
		scratchDir = scratchDirRule.getRoot();

		// end of the workaround
		URI modelURI = getRestFileURI(description);
		try {
			ZenModel zenModel = loader.loadModel(modelURI);

			Monitor progressMonitor = null;
			Map<String, String> generated = doGenerate(zenModel, scratchDir, progressMonitor);
			assertTrue(generated.size() >= 1);
			generatedFile = prepareFile(scratchDir, generated, extension);
		} catch (IOException | GenerationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	protected abstract Map<String, String> doGenerate(ZenModel zenModel, File dir, Monitor progressMonitor)
			throws IOException, GenerationException;

	@Override
	protected void finished(Description description) {
		// dispose
		super.finished(description);
	}

	protected File getTemporaryDir() {
		return scratchDir;
	}

	protected static InputStream getSpecFile(String specFileName) {
		try {
			return Resources.getResource("spec/" + specFileName).openStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private URI getRestFileURI(Description description) {
		String sampleRestFilePath = getSampleRestName(description);
		String path = getClass().getClassLoader() //
				.getResource("models/dsl/" + sampleRestFilePath).toExternalForm();

		return URI.createURI(path);
	}

	protected abstract String getSampleRestName(Description description);

	protected File prepareFile(File scratchDir, Map<String, String> generated, String extension) throws IOException {
		Entry<String, String> entry = generated.entrySet().stream() //
				.filter(e -> e.getKey().endsWith("." + extension)) //
				.findFirst() //
				.orElseThrow(RuntimeException::new);

		String filePath = entry.getKey();
		File file = new File(filePath);
		if (!file.isAbsolute()) {
			file = new File(scratchDir, filePath);
		}
		Files.write(file.toPath(), entry.getValue().getBytes());
		return file;
	}

	@Target({ ElementType.TYPE })
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SampleRestFile {
		String value();
	}

}
