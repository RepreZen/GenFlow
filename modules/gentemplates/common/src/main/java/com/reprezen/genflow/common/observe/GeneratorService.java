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
 * Acts as an observer of the generation process for components that need to
 * listen to generation-related changes.
 * 
 * @author jimleroyer
 */
public interface GeneratorService {

	/**
	 * Adds a listener to this generator service.
	 * 
	 * @param listener Listener to add
	 */
	public void addListener(GeneratorListener listener);

	/**
	 * Notify the listeners of the generator changes involving the generated
	 * artifacts parameter.
	 * 
	 * @param artifacts Generated artifacts to notify of
	 */
	public void notifyListeners(Collection<GeneratedArtifact> artifacts);

	/**
	 * Removes all listeners attached to this generator service.
	 */
	public void removeAllListeners();

	/**
	 * Removes the listener parameter attached to this generator service.
	 * 
	 * @param listener Listener to remove
	 */
	public void removeListener(GeneratorListener listener);

}
