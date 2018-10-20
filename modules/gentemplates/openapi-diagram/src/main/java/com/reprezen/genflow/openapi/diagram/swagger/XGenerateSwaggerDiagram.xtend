/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import com.reprezen.genflow.api.GenerationException
import com.reprezen.genflow.api.swagger.SwaggerOutputItem
import com.reprezen.genflow.common.DiagramGenerator
import io.swagger.models.Swagger

class XGenerateSwaggerDiagram extends SwaggerOutputItem {
	public static val URL_PREFIX_PARAM = 'urlPrefix'
	public static val ANCHOR_NAME_PARAM = 'anchorName'
	public static val PREVIEW_PARAM = 'preview'

	override generate(Swagger swagger) {
		if (swagger.getPaths().isEmpty()) {
			'''
				<font face="verdana" color="#5C5858"><b>No diagram to display</b>
				<br/>
				The API model does not define any paths.
			'''
		} else {
			val json = new SwaggerDiagramData(swagger).generateDiagramData();
			val name = swagger.info?.title ?: "Unnamed"
			val generator = new DiagramGenerator<Swagger>() {
				override generate(Swagger primarySource, Swagger inputItem) throws GenerationException {
					// won't get called this way
				}
			}
			generator.init(context)
			generator.generate(json, name)
		}
	}
}
