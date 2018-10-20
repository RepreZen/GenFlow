/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.openapi3

import com.reprezen.genflow.api.GenerationException
import com.reprezen.genflow.api.openapi3.OpenApi3OutputItem
import com.reprezen.genflow.common.DiagramGenerator
import com.reprezen.kaizen.oasparser.model3.OpenApi3

class XGenerateOpenApi3Diagram extends OpenApi3OutputItem {
	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	override generate(OpenApi3 swagger) {
		if (swagger.getPaths().isEmpty()) {
			'''
				<font face="verdana" color="#5C5858"><b>No diagram to display</b>
				<br/>
				The API model does not define any paths.
			'''
		} else {
			val json = new OpenApi3DiagramData(swagger).generateDiagramData();
			val name = swagger.info?.title ?: "Unnamed"
			val generator = new DiagramGenerator<OpenApi3>() {
				override generate(OpenApi3 primarySource, OpenApi3 inputItem) throws GenerationException {
					// won't get called this way					
				}
			};
			generator.init(context)
			generator.generate(json, name)
		}
	}
}
