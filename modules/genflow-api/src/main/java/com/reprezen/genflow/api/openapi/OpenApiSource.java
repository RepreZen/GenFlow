/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.openapi;

import java.io.File;
import java.nio.file.Files;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.loadability.LoadabilityTester;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.openapi3.OpenApi3Source.OpenApi3LoadabilityTester;
import com.reprezen.genflow.api.source.AbstractSource;
import com.reprezen.genflow.api.source.ILocator;
import com.reprezen.genflow.api.swagger.SwaggerSource.SwaggerLoadabilityTester;

public class OpenApiSource extends AbstractSource<OpenApiDocument> {

	private static OpenApiLoadabilityTester loadabilityTester = OpenApiLoadabilityTester.getInstance();

	@Override
	public OpenApiDocument load(File inFile) throws GenerationException {
		try {
			String document = new String(Files.readAllBytes(inFile.toPath()));
			OpenApiType modelType = getModelType(inFile, document);
			if (modelType == null) {
				throw new GenerationException("Failed to determine OpenAPI version of document file " + inFile);
			}
			switch (modelType) { // null throws NPE if type cannot be determined
			case SWAGGERv2:
				return new SwaggerDocument(document, inFile, getNormalizerOptions());
			case OPENAPIv3:
				return new OpenApi3Document(document, inFile, getNormalizerOptions());
			default: // should not be possible
				return null;
			}
		} catch (Exception e) {
			throw new GenerationException("Failed to load normalized model", e);
		}
	}

	private OpenApiType getModelType(File file, String document) {
		return loadabilityTester.getLoadableType(file, LoadabilityTester.PARTIAL_LOAD_DILIGENCE, document);
	}

	protected Option[] getNormalizerOptions() throws GenerationException {
		return Option.CODEGEN_DEFAULT_OPTIONS;
	}

	@Override
	public ILocator<OpenApiDocument> getLocator(OpenApiDocument model) {
		return new OpenApiLocator(model);
	}

	@Override
	public String getLabel() {
		return "OpenAPI";
	}

	@Override
	public LoadabilityTester getLoadabilityTester() {
		return loadabilityTester;
	}

	public static LoadabilityTester loadabilityTester() {
		return loadabilityTester;
	}

	public static boolean canLoad(File file) {
		return loadabilityTester.canLoad(file);
	}

	public enum OpenApiType {
		SWAGGERv2(SwaggerLoadabilityTester.getInstance()), //
		OPENAPIv3(OpenApi3LoadabilityTester.getInstance());

		final private OpenApiLoadabilityTester loadabilityTester;

		OpenApiType(OpenApiLoadabilityTester loadabilityTester) {
			this.loadabilityTester = loadabilityTester;
		}

		public OpenApiLoadabilityTester getLoadabilityTester() {
			return loadabilityTester;
		}
	}
}