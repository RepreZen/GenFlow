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
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Generator service implementation. Acts as an observer of the generation
 * process for components that need to listen to generation-related changes.
 * 
 * @author jimleroyer
 */
public class GeneratorServiceImpl implements GeneratorService {

	/**
	 * Protect concurrent listeners addition and cleaning with a copy on write
	 * array.
	 */
	private Collection<GeneratorListener> listeners = new CopyOnWriteArrayList<>();

	@Override
	public void addListener(GeneratorListener listener) {
		listeners.add(listener);
	}

	@Override
	public void notifyListeners(Collection<GeneratedArtifact> artifacts) {
		for (GeneratorListener listener : listeners) {
			try {
				listener.artifactsGenerated(artifacts);
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void removeAllListeners() {
		listeners.clear();
	}

	@Override
	public void removeListener(GeneratorListener listener) {
		listeners.remove(listener);
	}
}
