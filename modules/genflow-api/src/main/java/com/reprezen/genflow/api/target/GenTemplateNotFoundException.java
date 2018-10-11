/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.target;

import java.net.URL;
import java.util.Set;

/**
 * @author Konstantin Zaitsev
 * @date May 28, 2015
 */
public class GenTemplateNotFoundException extends RuntimeException {

	/** Serial version UID */
	private static final long serialVersionUID = 4189308431257088101L;
	private Set<URL> searchPath;
	private String templateName;

	public GenTemplateNotFoundException(String templateName, Set<URL> searchPath) {
		super();
		this.templateName = templateName;
		this.searchPath = searchPath;
	}

	/**
	 * @return the templateName
	 */
	public String getTemplateName() {
		return templateName;
	}

	/**
	 * @return the searchPath
	 */
	public Set<URL> getSearchPath() {
		return searchPath;
	}

	@Override
	public String getMessage() {
		return "Unable to locate the GenTemplate '" + templateName + "'"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
