/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.EcoreUtil2;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.reprezen.core.RapidFileExtensions;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.loadability.AbstractLoadabilityTester;
import com.reprezen.genflow.api.loadability.LoadabilityTester;
import com.reprezen.genflow.api.source.AbstractSource;
import com.reprezen.genflow.api.source.ILocator;
import com.reprezen.genflow.api.zenmodel.util.CommonServices;
import com.reprezen.rapidml.DataModel;
import com.reprezen.rapidml.RapidmlPackage;
import com.reprezen.rapidml.SingleValueType;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader;

public class ZenModelSource extends AbstractSource<ZenModel> {

	private final ZenModelLoader loader = new ZenModelLoader();

	public ZenModelSource() {
		super();
	}

	public ZenModelSource(File inputFile) {
		super(inputFile);
	}

	@Override
	public String getLabel() {
		return "RAPID model";
	}

	@Override
	public ZenModel load(File inFile) throws GenerationException {
		checkMetaModelVersion(getMetaModelVersion());
		ZenModel model = loader.loadAndValidateModel(inFile);
		new ZenModelNormalizer().normalize(model);
		return model;
	}

	@Override
	public Iterable<Object> extractByNonSourceType(ZenModel model, Class<?> itemClass) throws GenerationException {
		if (EObject.class.isAssignableFrom(itemClass)) {
			@SuppressWarnings("unchecked")
			Class<? extends EObject> eObjectClass = (Class<? extends EObject>) itemClass;
			return getExtractedItems(model, eObjectClass);
		} else {
			throw cantExtractException(itemClass);
		}
	}

	@Override
	public Class<?> getValueType() throws GenerationException {
		return ZenModel.class;
	}

	private <T extends EObject> List<Object> getExtractedItems(ZenModel model, Class<T> itemClass) {

		List<Object> items = Lists.newArrayList();
		items.addAll(EcoreUtil2.getAllContentsOfType(model, itemClass));

		// in addition add used datamodels from another imported zenmodels
		if (itemClass.isAssignableFrom(DataModel.class)) {
			for (SingleValueType singleValueType : CommonServices.getUsedSimpleTypes(model)) {
				DataModel dataModel = CommonServices.getContainerOfType(singleValueType, DataModel.class);
				if (!items.contains(dataModel)) {
					@SuppressWarnings("unchecked")
					T tDataModel = (T) dataModel;
					items.add(tDataModel);
				}
			}

		}
		return items;
	}

	@Override
	public ILocator<ZenModel> getLocator(ZenModel model) {
		return new ZenModelLocator(model);
	}

	private String getCurrentMetaModelVersion() {
		String nsURI = RapidmlPackage.eNS_URI;
		if (!Strings.isNullOrEmpty(nsURI)) {
			int idx = nsURI.lastIndexOf('/');
			if (idx > 0) {
				return nsURI.substring(idx + 1);
			}
		}
		return "1.0";
	}

	public void checkMetaModelVersion(String metamodelVersion) throws GenerationException {
		if (!metamodelVersion.equals(getCurrentMetaModelVersion())) {
			throw new GenerationException(String.format(
					"ZenModelSource implementation needs to be updated to support current metamodel verison %s; currently supports %s",
					getCurrentMetaModelVersion(), metamodelVersion));
		}
	}

	/**
	 * @return meta model version that supported by this generation template.
	 */
	String getMetaModelVersion() {
		return "1.0";
	}

	@Override
	public LoadabilityTester getLoadabilityTester() {
		return ZenModelLoadabilityTester.getInstance();
	}

	public static LoadabilityTester loadabilityTester() {
		return ZenModelLoadabilityTester.getInstance();
	}

	public static boolean canLoad(File file) {
		return ZenModelLoadabilityTester.getInstance().canLoad(file);
	}

	public static boolean canLoad(File file, int diligence) {
		return ZenModelLoadabilityTester.getInstance().canLoad(file, diligence);
	}

	public static class ZenModelLoadabilityTester extends AbstractLoadabilityTester {

		private static ZenModelLoadabilityTester instance = new ZenModelLoadabilityTester();

		private ZenModelLoadabilityTester() {
		}

		public static ZenModelLoadabilityTester getInstance() {
			return instance;
		}

		@Override
		public Loadability _getLoadability(File file, int diligence) {
			if (diligence <= LoadabilityTester.FILENAME_DILIGENCE) {
				if (RapidFileExtensions.includes(FilenameUtils.getExtension(file.getName()))) {
					return Loadability.loadable();
				} else {
					return Loadability.notLoadable("File is not named with a valid RAPID-ML extension: " + file);
				}
			} else {
				try {
					new ZenModelSource().load(file);
					return Loadability.loadable();
				} catch (GenerationException e) {
					return Loadability.notLoadable(
							"File [" + file + "] does not contain a valid RAPID-ML model: " + e.getMessage());
				}
			}
		}

		@Override
		public int getDefaultDiligence() {
			return LoadabilityTester.FILENAME_DILIGENCE;
		}
	}
}
