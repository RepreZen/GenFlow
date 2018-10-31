/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.rapidml.wadl.xtend.XGenerateWadl;

/**
 * @author Konstantin Zaitsev
 * @date Jun 29, 2015
 */
public class WadlGenTemplate extends WadlGenTemplateBase {

	@Override
	public String getName() {
		return "WADL"; //$NON-NLS-1$
	}

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.wadl.WadlGenTemplate");
		define(primarySource() //
				.ofType(ZenModelSource.class) //
				.withDescription("RAPID-ML .rapid file containing model from which to generated WADL") //
				.required());
		define(prerequisite() //
				.named("xsdGenerator").on("com.reprezen.genflow.rapidml.xsd.XMLSchemaGenTemplate") //
				.withDescription("GenTarget of XML Schema Generator target") //
				.required());
		define(outputItem().named("WADL") //
				.using(XGenerateWadl.class) //
				.writing("${resourceAPI.name}.wadl"));
		define(GenTemplateProperty.reprezenProvider());
	}
}
