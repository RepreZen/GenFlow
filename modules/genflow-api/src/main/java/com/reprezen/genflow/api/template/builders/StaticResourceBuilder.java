/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.builders;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;

public class StaticResourceBuilder extends BuilderBase<StaticResourceBuilder> {

	private String resourcePath;
	private File output;

	public StaticResourceBuilder copying(String resourcePath) {
		this.resourcePath = resourcePath;
		return this;
	}

	public StaticResourceBuilder to(File output) {
		this.output = output;
		return this;
	}

	public StaticResourceBuilder to(String output) {
		return to(new File(output));
	}

	public StaticResourceSpec build() throws GenerationException {
		return new StaticResourceSpec(resourcePath, output, descriptionLines);
	}

	public static class StaticResourceSpec extends SpecBase {
		private final String resourcePath;
		private final File output;

		public StaticResourceSpec(String resourcePath, File output, String[] descriptionLines)
				throws GenerationException {
			super(descriptionLines);
			this.resourcePath = resourcePath;
			this.output = output;
			validate();
		}

		public String getResourcePath() {
			return resourcePath;
		}

		public File getOutput() {
			return output;
		}

		private void validate() throws GenerationException {
			// TODO validate valid resource path or "." using regex
			if (resourcePath == null || resourcePath.isEmpty()) {
				throw new GenerationException("Invalid resource path: \"" + resourcePath + "\"");
			}
			if (output == null) {
				throw new GenerationException("Output file/directory must be specified");
			}
		}

		@Override
		public String toString() {
			return String.format("%s[resourcePath: %s, output: %s, description: %s]", BuilderUtil.simpleName(this),
					resourcePath, output, description);
		}
	}
}
