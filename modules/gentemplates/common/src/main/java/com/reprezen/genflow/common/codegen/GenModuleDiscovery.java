/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.codegen;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ServiceLoader;

import com.reprezen.genflow.common.codegen.GenModulesInfo.Info;

import io.swagger.codegen.CodegenConfig;

/**
 * This class is a main program that collects information about all the
 * available swagger-codegen language modules and updates the modulesInfo
 * moduleParams CSV files in the project resources directory.
 * <p>
 * What's important about this is that the update will indicate where metadata
 * obtained from the scan differs from what was contained in the CSV files prior
 * to this execution.
 * <p>
 * When preparing to use a new release of swagger-codegen project, these changes
 * should be scrutinized, and name overrides added to the CSV file as needed.
 * Checking in that updated CSV file will ensure that the name overrides will be
 * incorporated into subsequent builds.
 * 
 * @author Andy Lowry
 * 
 */
public abstract class GenModuleDiscovery {

	protected abstract String getLibraryVersion();

	protected abstract ModuleWrapper getDummyWrapper();

	protected abstract ModuleWrapper wrap(Object obj);

	private ModuleWrapper dummyWrapper = getDummyWrapper();

	public void runDiscovery(String[] args) throws URISyntaxException, MalformedURLException, IOException {

		File baseDir = new File(args[0]);
		String libVersion = getLibraryVersion();
		GenModulesInfo baseInfo = GenModulesInfo.load(libVersion, baseDir.toURI().toURL(), dummyWrapper);
		if (baseInfo == null || !baseInfo.getLibVersion().equals(libVersion)) {
			GenModulesInfo myInfo = new GenModulesInfo(libVersion);
			if (baseInfo != null) {
				copyInfo(baseInfo, myInfo);
			}
			doDiscovery(myInfo);
			myInfo.purgeNonBuiltin();
			myInfo.save(baseDir);
			if (baseInfo != null) {
				System.out.printf("Modules info for SCG version %s created based on existing info for version %s",
						libVersion, baseInfo.getLibVersion());
			} else {
				System.out.printf("Modules infor for SCG version %s created from scratch", libVersion);
			}
		} else {
			System.out.printf("Modules info for SCG version %s already present; left unchanged", libVersion);
		}

	}

	private void copyInfo(GenModulesInfo from, GenModulesInfo to) {
		for (String cls : from.getClassNames()) {
			to.addOrUpdateInfo(cls, from.getInfo(cls));
		}
		to.resetStatus();
	}

	private void doDiscovery(GenModulesInfo modulesInfo) {
		modulesInfo.resetStatus();
		for (CodegenConfig codegen : ServiceLoader.load(CodegenConfig.class)) {
			Info info = new Info(wrap(codegen));
			modulesInfo.addOrUpdateInfo(codegen.getClass().getName(), info);
		}
	}
}
