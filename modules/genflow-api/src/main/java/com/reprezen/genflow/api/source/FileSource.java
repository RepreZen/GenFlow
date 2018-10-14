/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.source;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.loadability.AbstractLoadabilityTester;

/**
 * Generic Source implementation that can be used to provide access to files for
 * which value-loading source implementations are unavailable.
 * <p>
 * The "value" of a FileSource is simply a File object referencing whatever file
 * is bound to the source. The file is not opened or processed in any way; it is
 * exactly as provided by the GenTarget during binding.
 * 
 * @author Andy
 * 
 */
public class FileSource extends AbstractSource<File> {

	public FileSource() {
		super();
	}

	public FileSource(File inputFile) {
		super(inputFile);
	}

	@Override
	public String getLabel() {
		return "File";
	}

	@Override
	public File load(File inFile) throws GenerationException {
		return inputFile;
	}

	@Override
	public ILocator<File> getLocator(File soureValue) {
		return null;
	}

	public static class FileLoadabilityTester extends AbstractLoadabilityTester {

		@Override
		public Loadability _getLoadability(File file, int diligence) {
			return Loadability.loadable();
		}
	}
}
