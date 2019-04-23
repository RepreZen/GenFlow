package com.reprezen.genflow.swagger.codegen;

import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.common.codegen.CodegenGenTemplateGroup;
import com.reprezen.genflow.common.codegen.GenModuleWrapper;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.CodegenConfig;

public class ScgCodegenGenTemplateGroup extends CodegenGenTemplateGroup<CodegenConfig> {
	private static Logger logger = LoggerFactory.getLogger(ScgCodegenGenTemplateGroup.class);

	public ScgCodegenGenTemplateGroup() {
		super(ScgModuleWrapper.getDummyInstance());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}

	@Override
	public GenModuleWrapper<CodegenConfig> wrap(CodegenConfig config) {
		return new ScgModuleWrapper(config);
	}

	@Override
	public IGenTemplate createGenTemplate(GenModuleWrapper<CodegenConfig> wrapper, Info info) {
		return new ScgCodegenGenTemplate(wrapper, info);
	}

	public static void main(String[] args) {
		// only used during development for testing outside of product
		Iterable<IGenTemplate> genTemplatesIter = new ScgCodegenGenTemplateGroup()
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
