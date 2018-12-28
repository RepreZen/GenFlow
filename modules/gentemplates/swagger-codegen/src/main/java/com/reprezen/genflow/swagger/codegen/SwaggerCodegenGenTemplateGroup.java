/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplateGroup;
import com.reprezen.genflow.swagger.codegen.GenModulesInfo.Info;
import com.reprezen.genflow.swagger.codegen.ModuleWrapper.ScgModuleWrapper;

import io.swagger.codegen.CodegenConfig;

public class SwaggerCodegenGenTemplateGroup implements IGenTemplateGroup {

	public static Logger logger = LoggerFactory.getLogger(SwaggerCodegenGenTemplateGroup.class);

	@Override
	public Iterable<IGenTemplate> getGenTemplates(ClassLoader classLoader) {
		GenModulesInfo modulesInfo = getModulesInfo();
		List<IGenTemplate> genTemplates = Lists.newArrayList();
		for (CodegenConfig config : getCodegenConfigs(modulesInfo, CodegenConfig.class.getClassLoader())) {
			Info info = modulesInfo.getInfo(new ScgModuleWrapper(config), true);
			if ((info.isVetted() || !info.isBuiltin()) && !info.isSuppressed()) {
				BuiltinSwaggerCodegenGenTemplate builtinSwaggerCodegenGenTemplate = new BuiltinSwaggerCodegenGenTemplate(
						config.getClass(), info);
				genTemplates.add(builtinSwaggerCodegenGenTemplate);
			}
		}
		return genTemplates;
	}

	public Collection<CodegenConfig> getCodegenConfigs(GenModulesInfo modulesInfo, ClassLoader classLoader) {
		Set<CodegenConfig> configs = Sets.newHashSet();
		try {
			Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + CodegenConfig.class.getName());
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				for (CodegenConfig candidate : getConfigsFromServiceLoaderResource(url, classLoader)) {
					configs.add(candidate);
				}
			}
		} catch (IOException e) {
			logger.warn("Failed to locate service URLs for SCG CodegenConfig class", e);
		}
		return configs;
	}

	private Collection<CodegenConfig> getConfigsFromServiceLoaderResource(URL url, ClassLoader classLoader) {
		List<CodegenConfig> configs = Lists.newArrayList();
		try (InputStream in = url.openStream()) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("#") || line.isEmpty()) {
					continue;
				}
				try {
					Class<?> c = classLoader.loadClass(line);
					if (CodegenConfig.class.isAssignableFrom(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends CodegenConfig> validClass = (Class<? extends CodegenConfig>) c;
						configs.add(validClass.newInstance());
					}
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
					logger.warn(String.format("Failed to load SCG class %s", line), e);
				}
			}
		} catch (IOException e1) {
			logger.warn(String.format("Failed to read from service loader URL %s", url), e1);
		}
		return configs;
	}

	public static GenModulesInfo getModulesInfo() {
		URL infoUrl = SwaggerCodegenGenTemplateGroup.class.getResource("");
		String scgVersion = CodegenConfig.class.getPackage().getImplementationVersion();
		try {
			return GenModulesInfo.load(scgVersion, infoUrl, ScgModuleWrapper.getDummyInstance());
		} catch (IOException | URISyntaxException e) {
			// resource not found... no modules available
			return new GenModulesInfo(scgVersion);
		}
	}

	public static void main(String[] args) {
		// only used during development for testing outside of product
		IGenTemplate[] genTemplates = Iterables.toArray(new SwaggerCodegenGenTemplateGroup()
				.getGenTemplates(SwaggerCodegenGenTemplateGroup.class.getClassLoader()), IGenTemplate.class);
		System.out.println(genTemplates.length + " GenTemplates discovered:");
		for (IGenTemplate genTemplate : genTemplates) {
			System.out.println(genTemplate.getName());
		}
	}
}
