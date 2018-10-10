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
import java.util.Map;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * Headless registry to discover generation templates. Can be used only in
 * non-Eclipse environment.
 * 
 * @author Konstantin Zaitsev
 * @date May 18, 2015
 */
public final class HeadlessGenTemplateRegistry {

	private static Logger logger = LoggerFactory.getLogger(HeadlessGenTemplateRegistry.class);

	/** Singleton reference. */
	private static HeadlessGenTemplateRegistry instance;

	private Map<String, IGenTemplate> registry;

	private HeadlessGenTemplateRegistry() {
		ClassLoader classLoader = HeadlessGenTemplateRegistry.class.getClassLoader();
		if (registry == null) {
			registry = new HashMap<String, IGenTemplate>();
			Iterator<IGenTemplate> genTemplates = ServiceLoader.load(IGenTemplate.class).iterator();
			while (genTemplates.hasNext()) {
				try {
					IGenTemplate template = genTemplates.next();
					registry.put(template.getId(), template);
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
							registry.put(template.getId(), template);
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

	private static HeadlessGenTemplateRegistry getInstance() {
		if (instance == null) {
			synchronized (HeadlessGenTemplateRegistry.class) {
				if (instance == null) {
					instance = new HeadlessGenTemplateRegistry();
				}
			}
		}
		return instance;
	}

	/**
	 * @param id id of the gen template
	 * @return the gen template by ID
	 */
	public static IGenTemplate getGenTemplate(String id) {
		assert id != null;
		if (getInstance().registry.containsKey(id)) {
			return getInstance().registry.get(id);
		}
		throw new RuntimeException("Template " + id + " not found"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static ImmutableList<IGenTemplate> getGenTemplates() {
		return ImmutableList.copyOf(getInstance().registry.values());
	}
}
