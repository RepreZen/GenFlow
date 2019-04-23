/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetNamedSource;
import com.reprezen.genflow.api.target.GenTargetParameter;
import com.reprezen.genflow.api.target.GenTargetPrerequisite;
import com.reprezen.genflow.api.target.GenTargetPrimarySource;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.trace.GenTemplateTraces;
import com.reprezen.genflow.common.codegen.GenModulesInfo;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.CodegenConfig;
import io.swagger.models.Swagger;

public class SwaggerCodegenModulesTest extends Assert {

	private static Logger logger = Logger.getLogger(SwaggerCodegenModulesTest.class.getSimpleName());
	private static final List<CodegenConfig> unsuppressedScgClasses = Lists.newArrayList();
	private static final List<CodegenConfig> suppressedScgClasses = Lists.newArrayList();
	private static final GenModulesInfo modulesInfo = new ScgCodegenGenTemplateGroup().getModulesInfo();

	@BeforeClass
	public static void setup() throws InstantiationException, IllegalAccessException {
		System.out.println(modulesInfo);

		for (CodegenConfig scgClass : new ScgCodegenGenTemplateGroup().getConfigs(modulesInfo,
				SwaggerCodegenModulesTest.class.getClassLoader())) {

			ScgModuleWrapper wrapper = new ScgModuleWrapper(scgClass);
			Info info = modulesInfo.getInfo(wrapper);

			if (info != null && info.isSuppressed()) {
				suppressedScgClasses.add(scgClass);
			} else {
				unsuppressedScgClasses.add(scgClass);
			}
		}
	}

	@Test
	public void SuppressedClassCheck() {
		assertEquals("Not all suppressed SCG classes were discovered", suppressedScgClasses.size(),
				expectedSuppressionsCount());
	}

	private int expectedSuppressionsCount() {
		int count = 0;
		for (String className : modulesInfo.getClassNames()) {
			if (modulesInfo.getInfo(className).isSuppressed()) {
				count += 1;
			}
		}
		return count;
	}

	@Test
	public void UnsupressedLanguagesSucceed() throws Exception {
		for (CodegenConfig scgClass : unsuppressedScgClasses) {
			logger.info("SCG Test: " + scgClass.getClass().getSimpleName());
			assertNull("SCG target language '" + scgClass.getName() + " failed", tryCodegen(scgClass));
		}
	}

	@Test
	public void SuppressedLanguagesThrowExceptions() throws Exception {
		for (CodegenConfig scgClass : suppressedScgClasses) {
			assertNotNull("Suppressed SCG target language '" + scgClass.getName()
					+ "' ran successfully - perhaps it should no longer be suppressed", tryCodegen(scgClass));
		}
	}

	private Throwable tryCodegen(CodegenConfig scgClass) throws Exception {
		try {
			ScgModuleWrapper wrapper = new ScgModuleWrapper(scgClass);

			ScgCodegenGenTemplate genTemplate = new ScgCodegenGenTemplate(wrapper, modulesInfo.getInfo(wrapper));
			GenTarget target = createGenTarget("Test.yaml", genTemplate);
			GenTemplate<Swagger>.Generator generator = genTemplate.getGenerator();
			generator.attachLogger(logger);
			generator.generate(target, new GenTemplateTraces());
			return null;
		} catch (Throwable t) {
			t.printStackTrace();
			return t;
		}
	}

	private GenTarget createGenTarget(String swaggerFileName, GenTemplate<?> genTemplate)
			throws GenerationException, URISyntaxException {
		GenTarget target = new GenTarget();

		URL swaggerFileUrl = Resources.getResource(swaggerFileName);
		File swaggerFile = Paths.get(swaggerFileUrl.toURI()).toFile();

		target.setBaseDir(swaggerFile.getParentFile());
		GenTargetPrimarySource priSrc = new GenTargetPrimarySource();
		priSrc.setPath(swaggerFile);
		target.setPrimarySource(priSrc);
		target.setRelativeOutputDir(new File("generated", genTemplate.getId()));
		target.setGenTemplateId(genTemplate.getId());
		target.setParameters(Lists.<GenTargetParameter>newArrayList());
		target.setPrerequisiteList(Lists.<GenTargetPrerequisite>newArrayList());
		target.SetNamedSourceList(Lists.<GenTargetNamedSource>newArrayList());
		return target;
	}
}
