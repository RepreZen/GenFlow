/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template.config;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;

public class GenTargetConfigUtil {

	private final static ObjectMapper objectMapper = new ObjectMapper();

	public static GenTemplateConfig loadConfig(Class<? extends GenTemplate<?>> genTemplateClass)
			throws GenerationException {
		String specificName = genTemplateClass.getSimpleName() + "-config.json";
		InputStream resource = genTemplateClass.getResourceAsStream(specificName);
		Throwable failureCause = null;
		if (resource == null) {
			resource = genTemplateClass.getResourceAsStream("config.json");
		}
		if (resource != null) {
			try {
				GenTemplateConfig config = objectMapper.readValue(resource, GenTemplateConfig.class);
				config.complete();
				return config;
			} catch (IOException e) {
				failureCause = e;
			}
		}
		throw new GenerationException("Failed to load config file for GenTemplate", failureCause);
	}
}
