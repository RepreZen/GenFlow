/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author Konstantin Zaitsev
 * @date Sep 25, 2014
 */
public class D3JSPlugin extends Plugin {

	/** The plug-in ID */
	public static final String PLUGIN_ID = "com.modelsolv.reprezen.gentemplates.d3js"; //$NON-NLS-1$

	/** The shared instance */
	private static D3JSPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static D3JSPlugin getDefault() {
		return plugin;
	}
}
