/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.csharp;

import org.apache.commons.lang3.StringUtils;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.rapidml.csharp.Config.Framework;

public class CSharpGenTemplate extends ZenModelGenTemplate {

	@Override
	public String getName() {
		return "C# ASP.NET Core Server (experimental)"; //$NON-NLS-1$
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.csharp.CSharpGenTemplate");
		defineZenModelSource();
		define(parameter() //
				.named("rootNamespace") //
				.withDescription("Leave null to use model name"));
		define(parameter(). //
				named("modelsFolder") //
				.withDescription("Folder for generated model files") //
				.withDefault("Models"));
		define(parameter() //
				.named("controllersFolder") //
				.withDescription("Folder for generated controller files") //
				.withDefault("Controllers"));
		define(parameter() //
				.named("generateModelPOCOs") //
				.withDescription("Whether to generate Plain-Old-CLI-Objects for model interfaces") //
				.withDefault(true));
		define(parameter() //
				.named("generateJSONSerialization") //
				.withDescription("Whether to enable JSON serialization for model objects") //
				.withDefault(true));
		define(parameter() //
				.named("generateDelegateController") //
				.withDescription("Whether to generate a controller class based on delegates for method implementation") //
				.withDefault(true));
		define(parameter() //
				.named("framework") //
				.withDescription("Target programming framework for generated code", //
						"One of: " + StringUtils.join(Framework.allFullNames(), ", ")) //
				.withDefault(Framework.ASP_DOTNET_CORE_2_0_MVC.getFullName()));
		define(dynamicGenerator()//
				.named("C#") //
				.using(CSharpGenerator.class));
		define(GenTemplateProperty.reprezenProvider());
	}
}
