/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.target;

import com.reprezen.genflow.api.GenerationException;

/**
 * @author Konstantin Zaitsev
 */
public class CircularDependenciesException extends GenerationException {
	/** Serial version UID. */
	private static final long serialVersionUID = 6396559776451316310L;
	private String genTarget;
	private String dependency;

	/**
	 * @param message
	 */
	public CircularDependenciesException(String genTarget, String dependency) {
		super(String.format("Generation target has circular dependencies:\n%s -> %s", genTarget, dependency), null);
		this.genTarget = genTarget;
		this.dependency = dependency;
	}

	/**
	 * @return the genTarget
	 */
	public String getGenTarget() {
		return genTarget;
	}

	/**
	 * @return the dependency
	 */
	public String getDependency() {
		return dependency;
	}
}
