/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.nodejs;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;

public class NodejsGenTemplate extends ZenModelGenTemplate {

	@Override
	public String getName() {
		return "Node.js Server"; //$NON-NLS-1$
	}

	@Override
	public void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.nodejs.NodejsGenTemplate");
		defineZenModelSource();
		define(parameter().named("daoAdapter") //
				.withDescription("[ENV] Name of Built-In DAO Adapter Class, or require path for custom adapter", //
						"Currently available built-in adapters:", //
						"  ES (ElasticSearch)", //
						"", //
						"Note: this and other options marked with '[ENV]' will have environment variables interpolated", //
						"at run time. To use this feature, include env vars in the form '${VAR_NAME}'") //
				.required().withDefault("ES"));
		define(parameter().named("daoAdapterOptions") //
				.withDescription("[ENV] Options object used to instantiate DAO adapter")); //
		define(parameter().named("additionalMetadata") //
				.withDescription(
						"[ENV] List of additional metadata.json files to be loaded, with paths relative to the project root", //
						"(e.g. an overlay with DAO-specific metadata, or metadata for another model)"));
		define(parameter().named("servicePort") //
				.withDescription("[ENV] Port on which service should listen for requests"));
		define(parameter().named("baseURL") //
				.withDescription("[ENV] Base URL for the application, used both for resource routing and in", //
						"construction of reference link URIs. In the former case, only the path component is used.") //
				.required().withDefault("https://example.com/"));
		define(parameter().named("middlewareModules") //
				.withDescription(
						"[ENV] List of middlewares to incorporate into the Express app object, just prior to routing.", //
						"Each value should take the form of a require path of the middleware module, relative to the project root.", //
						"Each module should export its middleware function."));
		define(parameter().named("setupModules") //
				.withDescription("[ENV] List of modules to invoke after setup, just prior to service start.", //
						"Each value should take the form of a require path of the setup module, relative to the project root.", //
						"Each module should export a function that will be invoked with three arguments:", //
						"  - the metadata object defining model types and their relationships", //
						"  - the data access object providing read access to the data store", //
						"  - the Express app object"));
		define(parameter().named("genMetadataSkeleton") //
				.withDescription("Whether to generate a skeleton for a metadata file for this model",
						"You can edit this and add it to the additionalMetadata list as needed, e.g. for your DAO adapter",
						"You should probably rename this file before editing, so it won't be overwritten through regeneration")
				.withDefault(true));
		define(parameter().named("structMetadataSkeleton") //
				.withDescription("Properties (possibly nested) to place in skeleton metadata for each Structure"));
		define(parameter().named("primFieldMetadataSkeleton") //
				.withDescription(
						"Properties (possibly nested) to place in skeleton metadata for each primitive model property"));
		define(parameter().named("refFieldMetadataSkeleton") //
				.withDescription(
						"Properties (possibly nested) to place in skeleton metadata for each reference model property"));
		define(parameter().named("omitFieldsWithoutSkeletons") //
				.withDescription(
						"Do not create skeleton properties for which the corresponding skeleton metadata parameter", //
						"(primFieldMetadtaSkeleton or refFieldMetadataSkeleton) is null")
				.withDefault(true));
		define(parameter().named("linksPropertyName") //
				.withDescription("Property name for links added to objects via realization enrichment") //
				.withDefault("_links"));
		define(dynamicGenerator().using(NodejsGenerator.class));
		define(staticResource().copying("fixed").to("."));
		define(GenTemplateProperty.reprezenProvider());
	}
}
