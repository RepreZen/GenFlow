/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.loadability;

import java.io.File;

public abstract class AbstractLoadabilityTester implements LoadabilityTester {

	public Loadability getLoadability(File file) {
		return getLoadability(file, getDefaultDiligence());
	}

	@Override
	public Loadability getLoadability(File file, int diligence) {
		if (!file.exists()) {
			return Loadability.notLoadable("File does not exist: " + file);
		} else if (file.isDirectory()) {
			return Loadability.notLoadable("File is a directory: " + file);
		}
		Loadability loadability = _getLoadability(file, FILENAME_DILIGENCE);
		if (loadability.isLoadable() && diligence > FILENAME_DILIGENCE) {
			loadability = _getLoadability(file, diligence);
		}
		return loadability;
	}

	protected abstract Loadability _getLoadability(File file, int diligence);

	@Override
	public boolean canLoad(File file) {
		return canLoad(file, getDefaultDiligence());
	}

	@Override
	public boolean canLoad(File file, int diligence) {
		return getLoadability(file, diligence).isLoadable();
	}

	@Override
	public int getDefaultDiligence() {
		return LoadabilityTester.FILENAME_DILIGENCE;
	}
}
