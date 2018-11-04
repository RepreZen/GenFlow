/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jaxrs;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.rapidml.jaxb.JaxbGenTemplate;

public class JaxRsGenTemplate extends ZenModelGenTemplate {

	public JaxRsGenTemplate() {
	}

	public static final String JAX_RS_GENERATOR_NAME = "JAX-RS";
	public static final String JAX_RS_GENERATOR_ID = JaxRsGenTemplate.class.getName();
	public static final String JAXB_DEPENDENCY = "xsdToJavaGenerator";

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.jaxrs");
		define(prerequisite().named(JAXB_DEPENDENCY).on(JaxbGenTemplate.class));
		defineZenModelSource();
		define(outputItem().named("JAXRS").using(XGenerateJaxRsResource.class).writing(
				"${com.reprezen.genflow.rapidml.jaxrs.XGenerateJaxRsResource.getFilePath(serviceDataResource)}"));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return JAX_RS_GENERATOR_NAME; // $NON-NLS-1$
	}

	@Override
	public String getId() {
		return JAX_RS_GENERATOR_ID;
	}
}