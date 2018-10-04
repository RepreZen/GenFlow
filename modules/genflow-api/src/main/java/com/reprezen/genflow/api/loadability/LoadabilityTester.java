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

public interface LoadabilityTester {

	public final static int DEFAULT_DILIGENCE = 0;
	public final static int FILENAME_DILIGENCE = 1; // must not require file to exist but can reject if it exists and is
													// not correct variety (e.g. directory)
	public final static int MAGIC_NUMBER_DILIGENCE = 3;
	public final static int PARTIAL_LOAD_DILIGENCE = 7;
	public final static int LOAD_DILIGENCE = 10;

	Loadability getLoadability(File file, int diligence);

	boolean canLoad(File file);

	boolean canLoad(File file, int diligence);

	int getDefaultDiligence();

	public final static class Loadability {
		private final boolean loadable;
		private final String explanation;

		public Loadability(boolean loadable, String explanation) {
			this.loadable = loadable;
			this.explanation = explanation;
		}

		public static Loadability loadable() {
			return new Loadability(true, null);
		}

		public static Loadability notLoadable(String explanation) {
			return new Loadability(false, explanation);
		}

		public boolean isLoadable() {
			return this.loadable;
		}

		public String getExplanation() {
			return this.explanation;
		}
	}

}
