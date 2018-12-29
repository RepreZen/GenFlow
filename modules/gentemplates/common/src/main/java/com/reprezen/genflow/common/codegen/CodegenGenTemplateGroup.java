/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.codegen;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplateGroup;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

public abstract class CodegenGenTemplateGroup<Config> implements IGenTemplateGroup {

	private GenModuleWrapper<Config> dummyWrapper;

	public abstract Logger getLogger();

	public abstract GenModuleWrapper<Config> wrap(Config config);

	public abstract IGenTemplate createGenTemplate(GenModuleWrapper<Config> wrapper, Info info);

	public CodegenGenTemplateGroup(GenModuleWrapper<Config> dummyWrapper) {
		this.dummyWrapper = dummyWrapper;
	}

	@Override
	public Iterable<IGenTemplate> getGenTemplates(ClassLoader classLoader) {
		GenModulesInfo modulesInfo = getModulesInfo();
		List<IGenTemplate> genTemplates = Lists.newArrayList();
		for (Config config : getConfigs(modulesInfo, classLoader)) {
			GenModuleWrapper<Config> wrapper = wrap(config);
			Info info = modulesInfo.getInfo(wrapper, true);
			if ((info.isVetted() || !info.isBuiltin()) && !info.isSuppressed()) {
				genTemplates.add(createGenTemplate(wrapper, info));
			}
		}
		return genTemplates;
	}

	public Collection<Config> getConfigs(GenModulesInfo modulesInfo, ClassLoader classLoader) {
		Set<Config> configs = Sets.newHashSet();
		try {
			Enumeration<URL> urls = classLoader.getResources("META-INF/services/" + dummyWrapper.getClassName());
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				for (Class<? extends Config> candidate : getConfigsFromServiceLoaderResource(url, classLoader)) {
					try {
						configs.add(candidate.newInstance());
					} catch (InstantiationException | IllegalAccessException e) {
						getLogger().warn(String.format("Failed to load codegen module class %s", candidate.getName()),
								e);
					}
				}
			}
		} catch (IOException e) {
			getLogger().warn("Failed to locate service URLs for codegen module class", e);
		}
		return configs;
	}

	private Collection<Class<? extends Config>> getConfigsFromServiceLoaderResource(URL url, ClassLoader classLoader) {
		Set<Class<? extends Config>> configs = new HashSet<>();
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
					if (dummyWrapper.canWrap(c)) {
						@SuppressWarnings("unchecked")
						Class<? extends Config> validClass = (Class<? extends Config>) c;
						configs.add(validClass);
					}
				} catch (ClassNotFoundException e) {
					getLogger().warn(String.format("Failed to load codegen module class %s", line), e);
				}
			}
		} catch (IOException e1) {
			getLogger().warn(String.format("Failed to read from service loader URL %s", url), e1);
		}
		return configs;
	}

	public GenModulesInfo getModulesInfo() {
		// assumption: modulesInfo files appear in jar file in same package as
		// superclass
		URL infoUrl = getClass().getResource("");
		String libVersion = dummyWrapper.getLibraryVersion();
		try {
			return GenModulesInfo.load(libVersion, infoUrl, dummyWrapper);
		} catch (IOException | URISyntaxException e) {
			// resource not found... no modules available
			return new GenModulesInfo(libVersion);
		}
	}

}
