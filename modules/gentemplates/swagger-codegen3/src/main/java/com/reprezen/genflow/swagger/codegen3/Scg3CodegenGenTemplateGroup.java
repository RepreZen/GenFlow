/*******************************************************************************
 * Copyright © 2013, 2019 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen3;

import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.common.codegen.CodegenGenTemplateGroup;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.v3.CodegenConfig;

public class Scg3CodegenGenTemplateGroup extends CodegenGenTemplateGroup<CodegenConfig> {
	private static Logger logger = LoggerFactory.getLogger(Scg3CodegenGenTemplateGroup.class);

	public Scg3CodegenGenTemplateGroup() {
		super(Scg3ModuleWrapper.getDummyInstance());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public GenModuleWrapper<CodegenConfig> wrap(CodegenConfig config) {
		return new Scg3ModuleWrapper(config);
	}

	@Override
	public IGenTemplate createGenTemplate(GenModuleWrapper<CodegenConfig> wrapper, Info info) {
		return new Scg3CodegenGenTemplate(wrapper, info);
	}

	public static void main(String[] args) {
		// only used during development for testing outside of product
		Iterable<IGenTemplate> genTemplatesIter = new Scg3CodegenGenTemplateGroup()
				.getGenTemplates(CodegenGenTemplateGroup.class.getClassLoader());
		IGenTemplate[] genTemplates = Iterables.toArray(genTemplatesIter, IGenTemplate.class);
		Arrays.sort(genTemplates, new Comparator<IGenTemplate>() {
			@Override
			public int compare(IGenTemplate o1, IGenTemplate o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		System.out.println(genTemplates.length + " GenTemplates discovered:");
		for (IGenTemplate genTemplate : genTemplates) {
			System.out.println(genTemplate.getName());
		}
	}

}
