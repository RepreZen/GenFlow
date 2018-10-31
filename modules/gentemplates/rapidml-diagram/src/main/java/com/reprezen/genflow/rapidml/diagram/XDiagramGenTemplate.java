/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.rapidml.diagram.xtend.XGenerateD3JS;
import com.reprezen.genflow.rapidml.diagram.xtend.XGenerateJSON;

/**
 * @author Konstantin Zaitsev
 * @date May 28, 2015
 */
public class XDiagramGenTemplate extends ZenModelGenTemplate {
	public static final String JSON_OUTPUT_ITEM_NAME = "JSON";

	@Override
	public String getName() {
		return "Diagram"; //$NON-NLS-1$
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.d3js.XDiagramGenTemplate");
		defineZenModelSource();
		defineParameterizedOutputItem(outputItem().using(XGenerateJSON.class), JSON_OUTPUT_ITEM_NAME,
				"data/${zenModel.name}.js");
		defineParameterizedOutputItem(outputItem().using(XGenerateD3JS.class), "HTML", "${zenModel.name}_diagram.html");
		define(staticResource().copying("assets").to("."));
		define(GenTemplateProperty.reprezenProvider());
	}
}
