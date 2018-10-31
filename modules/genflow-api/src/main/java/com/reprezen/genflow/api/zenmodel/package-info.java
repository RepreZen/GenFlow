/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package contains specializations of codegen APIs in support of RAPID-ML
 * models (aka Zen Models), exposed using the
 * {@link com.modelsolv.reprezen.restapi.ZenModel} class.
 * <p>
 * Specializations include:
 * <dl>
 * <dt>{@link ZenModelSource}</dt>
 * <dd>Implementation of {@link com.reprezen.genflow.api.source.ISource} that
 * can load models from .zen files. Includes a safeguard for correct metamodel
 * version, and is able to extract embedded resources by type.</dd>
 * <dt>{@link ZenModelLocator}</dt>
 * <dd>Implementation of {@link com.reprezen.genflow.api.source.ILocator} that
 * can locate and dereference arbitrary resources within a ZenModel</dd>
 * <dt>{@link ZenModelOutputItem}</dt>
 * <dd>Implementation of {@link com.reprezen.genflow.api.outputitem.IOutputItem}
 * with ZenModel bound to both the primary and item types. Offers a simplified
 * generate API, and a convenience class for configuring a ZenModel source</dd>
 * <dt>{@link ZenModelExtractOutputItem}</dt>
 * <dd>Implementation of {@link com.reprezen.genflow.api.outputitem.IOutputItem}
 * with ZenModel bound to the primary type, and the item type constrained to be
 * an extension of {@link org.eclipse.emf.ecore.EObject}.</dd>
 * <dt>{@link ZenModelGenTemplate}</dt>
 * <dd>Extension of
 * {@link com.reprezen.genflow.api.template.AbstractGenTemplate} with primary
 * source type of ZenModel</dd>
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.zenmodel;