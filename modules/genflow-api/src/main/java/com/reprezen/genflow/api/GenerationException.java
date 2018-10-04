/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api;

public class GenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public GenerationException(String message, Throwable cause) {
		super(message, cause);
	}

	public GenerationException(String message) {
		super(message);
	}
}
