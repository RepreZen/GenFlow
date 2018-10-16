/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.generator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.openapitools.codegen.CliOption;
import org.openapitools.codegen.CodegenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.openapi.generator.OpenApiGeneratorModulesInfo.Info;
import com.reprezen.genflow.openapi.generator.OpenApiGeneratorModulesInfo.Parameter;

/**
 * This class is a main program that collects information about all the
 * available openapi-generator language modules and updates the modulesInfo
 * moduleParams CSV files in the project resources directory.
 * <p>
 * What's important about this is that the update will indicate where metadata
 * obtained from the scan differs from what was contained in the CSV files prior
 * to this execution.
 * <p>
 * When preparing to use a new release of OpenAPI Generator project, these
 * changes should be scrutinized, and name overrides added to the CSV file as
 * needed. Checking in that updated CSV file will ensure that the name overrides
 * will be incorporated into subsequent builds.
 * 
 * @author Andy Lowry
 * 
 */
public class OpenApiGeneratorDiscovery {
	private static Logger logger = LoggerFactory.getLogger(OpenApiGeneratorDiscovery.class);

	/**
	 * Read module info from the specified directory, scan for new generator
	 * modules, and save the updated module info.
	 * 
	 * @param args [0] - Path to directory holding module info files
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		OpenApiGeneratorModulesInfo modulesInfo = new OpenApiGeneratorModulesInfo();
		File resourceDir = new File(args[0]);
		URL resourceUrl = resourceDir.toURI().toURL();
		modulesInfo.load(resourceUrl);
		modulesInfo.resetStatus();
		for (CodegenConfig codegen : ServiceLoader.load(CodegenConfig.class)) {
			Info info = new Info();
			try {
				info.setType(codegen.getTag());
			} catch (Throwable e) {
				// internal config problem in codegen module... ignore
			}
			info.setReportedName(codegen.getName());
			info.setDerivedDisplayName(BuiltinOpenApiGeneratorGenTemplate.getDerivedName(codegen));
			info.setParameters(getParameters(codegen));
			modulesInfo.addOrUpdateInfo(codegen.getClass(), info);
		}
		modulesInfo.save(resourceDir);
	}

	private static List<Parameter> getParameters(CodegenConfig codegen) {
		List<Parameter> params = Lists.newArrayList();
		Set<String> paramNames = Sets.newHashSet();
		for (CliOption option : codegen.cliOptions()) {
			if (paramNames.contains(option.getOpt())) {
				logger.warn("Duplicate parameter '{}' ignored for SCG module {}", option.getOpt(),
						codegen.getClass().getName());
			} else {
				Parameter param = new Parameter();
				param.setName(option.getOpt());
				param.setDescription(option.getDescription());
				param.setRequired(false);
				params.add(param);
				paramNames.add(option.getOpt());
			}
		}
		return params.size() > 0 ? params : null;
	}
}