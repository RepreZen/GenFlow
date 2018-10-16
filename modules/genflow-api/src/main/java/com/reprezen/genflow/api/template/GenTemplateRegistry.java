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
import java.util.ServiceConfigurationError;
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
	private static GenTemplateRegistry instance;

	private Map<String, IGenTemplate> registry;

	private GenTemplateRegistry() {
		ClassLoader classLoader = GenTemplateRegistry.class.getClassLoader();
		if (registry == null) {
			registry = new HashMap<String, IGenTemplate>();
			Iterator<IGenTemplate> genTemplates = ServiceLoader.load(IGenTemplate.class).iterator();
			while (genTemplates.hasNext()) {
				try {
					IGenTemplate template = genTemplates.next();
					register(template);
				} catch (ServiceConfigurationError e) {
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
						} catch (Exception e) {
							logger.warn("Could not retrieve gentemplate from group; skipping", e);
						}
					}
				} catch (Exception e) {
					logger.warn("Could not retrieve gentemplate group); skipping", e);
				}

			}
		}
	}

	private void register(IGenTemplate template) {
		register(template.getId(), template);
		if (template instanceof AbstractGenTemplate) {
			try {
				((AbstractGenTemplate) template).getAlsoKnownAsIds().stream().forEach((aka -> register(aka, template)));
			} catch (GenerationException e) {
				logger.warn(String.format("Failed to register also-known-as IDs for gentemplate " + template.getId()));
			}
		}
	}

	private void register(String id, IGenTemplate template) {
		if (!registry.containsKey(id)) {
			registry.put(id, template);
		} else {
			logger.warn(String.format("Id '%s' for GenTemplate %s already in use; ignoring", id,
					template.getClass().getName()));
		}
	}

	private static GenTemplateRegistry getInstance() {
		if (instance == null) {
			synchronized (GenTemplateRegistry.class) {
				if (instance == null) {
					instance = new GenTemplateRegistry();
				}
			}
		}
		return instance;
	}

	/**
	 * @param id id of the gen template
	 * @return the gen template by ID
	 * @throws GenerationException
	 */
	public static IGenTemplate getGenTemplate(String id) {
		assert id != null;
		if (getInstance().registry.containsKey(id)) {
			try {
				return getInstance().registry.get(id).newInstance();
			} catch (GenerationException e) {
			}
		}
		throw new RuntimeException("Template " + id + " not found");
	}

	public static List<IGenTemplate> getGenTemplates() {
		return getInstance().registry.values().stream().distinct().collect(Collectors.toList());
	}
}