/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl;

import com.modelsolv.reprezen.core.xml.XmlFormatter;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.rapidml.ZenModel;

public abstract class WadlGenTemplateBase extends ZenModelGenTemplate {

	@Override
	protected StaticGenerator<ZenModel> getStaticGenerator() {
		return new Generator(this, context);
	}

	public static class Generator extends GenTemplate.StaticGenerator<ZenModel> {
		public Generator(GenTemplate<ZenModel> genTemplate, GenTemplateContext context) {
			super(genTemplate, context);
		}

		private final XmlFormatter formatter = new XmlFormatter();

		@Override
		protected String postProcessContent(String content) throws Exception {
			return formatter.format(content);
		}
	}
}
