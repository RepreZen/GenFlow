/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.util;

import com.reprezen.genflow.api.template.GenTemplate;

public interface GenTemplateRegistryAdapter {
	public GenTemplate<?> getGenTemplate(String genTemplateId);
}
