/*******************************************************************************
 * Copyright © 2013, 2017 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package contains specializations of various CodeGen API classes to
 * support GenTemplates with a OpenAPI v3 model as a primary source. The
 * {@link com.reprezen.kaizen.oasparser.model3.OpenApi3} class is used to
 * reprezent OpenAPI v3 models.
 * <p>
 * Classes include:
 * <dl>
 * <dt>{@link OpenApi3Source}</dt>
 * <dd>An {@link com.modelsolv.reprezen.generators.api.source.ISource}
 * implementation that can load OpenAPI v3 files.</dd>
 * <dt>{@link OpenApi3Locator}</dt>
 * <dd>A locator that can be used to locate and dereference structures within a
 * OpenAPI v33 definition.</dd>
 * <dt>{@link OpenApi3GenTemplate}</dt>
 * <dd>A class that extends
 * {@link com.modelsolv.reprezen.generators.api.template.GenTemplate}, using
 * {@link OpenApi3Source} as the primary source.</dd>
 * <dt>{@link OpenApi3OutputItem}</dt>
 * <dd>An implementation of
 * {@link com.modelsolv.reprezen.generators.api.outputitem.IOutputItem} that
 * uses {@link com.reprezen.kaizen.oasparser.model3.OpenApi3} as both its
 * primary type and its input type.
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.openapi3;