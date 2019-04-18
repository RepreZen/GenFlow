/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.reprezen.genflow.api.GenerationException;

/**
 * Registry to discover generation templates. Can be used only in non-Eclipse
 * environment.
 * 
 * @author Konstantin Zaitsev
 * @date May 18, 2015
 */
public final class GenTemplateRegistry {

	private static Logger logger = LoggerFactory.getLogger(GenTemplateRegistry.class);

	/** Singleton reference. */
	private static GenTemplateRegistry defaultInstance = null;
	private ClassLoader classLoader;

	private Map<String, GenTemplateInfo> registry = null;

	public GenTemplateRegistry() {
		this(GenTemplateRegistry.class.getClassLoader());
	}

	public GenTemplateRegistry(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	private void register(IGenTemplate template) {
		GenTemplateInfo info = new GenTemplateInfo(template);
		if (register(info.getId(), info)) {
			info.getAkaIds().forEach(id -> register(id, info));
		}
	}

	private boolean register(String id, GenTemplateInfo info) {
		if (!registry.containsKey(id)) {
			registry.put(id, info);
		} else {
			logger.warn(String.format("Id '%s' for GenTemplate %s already in use; ignoring", id, info.getName()));
			return false;
		}
		return true;
	}

	private static GenTemplateRegistry getDefaultInstance() {
		if (defaultInstance == null) {
			synchronized (GenTemplateRegistry.class) {
				if (defaultInstance == null) {
					defaultInstance = new GenTemplateRegistry();
				}
			}
		}
		return defaultInstance;
	}

	/**
	 * @param id id of the gen template
	 * @return the gen template by ID
	 * @throws GenerationException
	 */

	public GenTemplateInfo getGenTemplate(String id) {
		assert id != null;
		scan(false);
		if (registry.containsKey(id)) {
			return registry.get(id);
		}
		throw new RuntimeException("Template " + id + " not found");
	}

	public static GenTemplateInfo getDefaultGenTemplate(String id) {
		return getDefaultInstance().getGenTemplate(id);
	}

	private void scan(boolean force) {
		if (registry == null || force) {
			registry = new HashMap<>();
			Iterator<IGenTemplate> genTemplates = ServiceLoader.load(IGenTemplate.class).iterator();
			while (genTemplates.hasNext()) {
				try {
					IGenTemplate template = genTemplates.next();
					register(template);
				} catch (Throwable e) {
					logger.warn("Could not retrieve gentemplate; skipping", e);
				}
			}
			Iterator<IGenTemplateGroup> groups = ServiceLoader.load(IGenTemplateGroup.class).iterator();
			while (groups.hasNext()) {
				try {
					IGenTemplateGroup templateGroup = groups.next();
					Iterator<IGenTemplate> groupIterator = templateGroup.getGenTemplates(classLoader).iterator();
					while (groupIterator.hasNext()) {
						try {
							IGenTemplate template = groupIterator.next();
							register(template);
						} catch (Throwable e) {
							logger.warn("Could not retrieve gentemplate from group; skipping", e);
						}
					}
				} catch (Exception e) {
					logger.warn("Could not retrieve gentemplate group); skipping", e);
				}
			}
		}
	}

	public List<GenTemplateInfo> getGenTemplates(boolean force) {
		scan(force);
		return registry.values().stream().distinct().collect(Collectors.toList());

	}

	public static List<GenTemplateInfo> getDefaultGenTemplates(boolean force) {
		return getDefaultInstance().getGenTemplates(force);
	}
}
