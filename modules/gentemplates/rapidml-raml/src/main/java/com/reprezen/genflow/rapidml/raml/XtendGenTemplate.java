/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;

public class XtendGenTemplate extends ZenModelGenTemplate {

	@Override
	public boolean isSuppressed() {
		// Suppressed per ZEN-3889
		return true;
	}

	@Override
	public String getName() {
		return "RAML";
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.generators.xtend.template.raml");
		defineZenModelSource();
		define(outputItem().named("RAML").using(MainTemplate.class).writing("${resourceAPI.name}.raml"));
		define(GenTemplateProperty.reprezenProvider());
	}
}
