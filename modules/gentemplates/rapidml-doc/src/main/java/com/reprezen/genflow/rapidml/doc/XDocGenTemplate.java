/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.doc;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.rapidml.doc.xtend.XGenerateDoc;

/**
 * @author Konstantin Zaitsev
 * @date May 28, 2015
 */
public class XDocGenTemplate extends ZenModelGenTemplate {

	public XDocGenTemplate() {
	}

	@Override
	public String getName() {
		return "Documentation"; //$NON-NLS-1$
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.doc.XDocGenTemplate");
		defineZenModelSource();
		defineParameterizedOutputItem(outputItem().using(XGenerateDoc.class), "HTML", "${zenModel.name}_doc.html");
		define(staticResource().copying("js").to("."));
		define(staticResource().copying("images").to("images"));
		define(GenTemplateProperty.reprezenProvider());
	}

}
