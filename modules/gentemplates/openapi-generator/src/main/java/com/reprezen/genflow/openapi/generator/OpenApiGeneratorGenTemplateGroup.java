/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.openapitools.codegen.CodegenConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplateGroup;
import com.reprezen.genflow.openapi.generator.OpenApiGeneratorModulesInfo.Info;

public class OpenApiGeneratorGenTemplateGroup implements IGenTemplateGroup {

	public static Logger logger = LoggerFactory.getLogger(OpenApiGeneratorGenTemplateGroup.class);

	@Override
	public Iterable<IGenTemplate> getGenTemplates(ClassLoader classLoader) {
		OpenApiGeneratorModulesInfo modulesInfo = getModulesInfo();
		List<IGenTemplate> genTemplates = Lists.newArrayList();
		for (Class<? extends CodegenConfig> config : getCodegenConfigClasses(modulesInfo,
				CodegenConfig.class.getClassLoader())) {
			Info info = modulesInfo.getInfo(config);
			genTemplates.add(new BuiltinOpenApiGeneratorGenTemplate(config, info));
		}
		return genTemplates;
	}

	public Collection<Class<? extends CodegenConfig>> getCodegenConfigClasses(OpenApiGeneratorModulesInfo modulesInfo,
			ClassLoader classLoader) {
		Set<Class<? extends CodegenConfig>> classes = Sets.newHashSet();
		try {
			Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + CodegenConfig.class.getName());
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				for (Class<? extends CodegenConfig> candidate : getClassesFromServiceLoaderResource(url, classLoader)) {
					Info info = modulesInfo.getInfo(candidate);
					// we only provide built-in SCG modules, for which we must have moduleinfo
					// discovered during product
					// build
					if (info == null || !info.isSuppressed()) {
						classes.add(candidate);
					}
				}
			}
		} catch (IOException e) {
			logger.warn("Failed to locate service URLs for SCG CodegenConfig class", e);
		}
		return classes;
	}

	private Collection<Class<? extends CodegenConfig>> getClassesFromServiceLoaderResource(URL url,
			ClassLoader classLoader) {
		List<Class<? extends CodegenConfig>> classes = Lists.newArrayList();
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
						classes.add(validClass);
					}
				} catch (ClassNotFoundException e) {
					logger.warn(String.format("Failed to load SCG class %s", line), e);
				}
			}
		} catch (IOException e1) {
			logger.warn(String.format("Failed to read from service loader URL %s", url), e1);
		}
		return classes;
	}

	public static OpenApiGeneratorModulesInfo getModulesInfo() {
		OpenApiGeneratorModulesInfo modulesInfo = new OpenApiGeneratorModulesInfo();
		// resources are in directory whose path mimics this class' package
		URL infoUrl = OpenApiGeneratorGenTemplateGroup.class.getResource("");
		try {
			modulesInfo.load(infoUrl);
		} catch (IOException e) {
			// resource not found... no modules available
		}
		return modulesInfo;
	}
}
