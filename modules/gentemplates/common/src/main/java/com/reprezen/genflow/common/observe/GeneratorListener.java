/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.observe;

import java.util.Collection;

/**
 * Implement this interface to listen to the RepreZen generator.
 * 
 * @author jimleroyer
 */
public interface GeneratorListener {

	/**
	 * This method is triggered when artifacts were generated from the listened
	 * generator.
	 * 
	 * @param artifacts Generated artifacts.
	 */
	public void artifactsGenerated(Collection<GeneratedArtifact> artifacts);

}
