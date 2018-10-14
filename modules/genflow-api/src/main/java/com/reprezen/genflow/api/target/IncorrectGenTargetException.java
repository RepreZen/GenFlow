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
 * @date May 28, 2015
 */
public class IncorrectGenTargetException extends GenerationException {

	/** Serial version UID. */
	private static final long serialVersionUID = -4058428648466826743L;

	public IncorrectGenTargetException(String message) {
		super(message);
	}

	public IncorrectGenTargetException(String message, Throwable cause) {
		super(message, cause);
	}

}
