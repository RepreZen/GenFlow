/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.services;

import static java.lang.String.valueOf;

import java.util.Random;

/**
 * Java utilities for the templates around the Documentation generation. <br>
 * This class was moved from the gentemplates.doc project because of ZEN-458
 * exceptions in java services in built product
 * 
 * @author jimleroyer
 * @since 2013/11/12
 */
public class DocServices {

	private Random random;

	public DocServices() {
		random = new Random();
		random.setSeed(System.currentTimeMillis());
	}

	/**
	 * Generates a random Integer ID.
	 * 
	 * @return Random Integer ID.
	 */
	public String randomId() {
		return valueOf(random.nextInt());
	}

}
