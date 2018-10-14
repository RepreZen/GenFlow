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
 * support GenTemplates with a OpenAPI model as a primary source. Any such
 * GenTemplate must be prepared to process either a
 * {@link io.swagger.models.Swagger} or an
 * {@link com.reprezen.kaizen.oasparser.model3.OpenApi3} object as its model.
 * The
 * <p>
 * Classes include:
 * <dl>
 * <dt>{@link OpenApiSource}</dt>
 * <dd>An {@link com.reprezen.genflow.api.source.ISource} implementation that
 * can load OpenAPI v2 or v3 files and provides methods to disambiguate</dd>
 * <dt>{@link OpenApiLocator}</dt>
 * <dd>A locator that can be used to locate and dereference structures within a
 * OpenAPI document.</dd>
 * <dt>{@link OpenApiGenTemplate}</dt>
 * <dd>A class that extends
 * {@link com.reprezen.genflow.api.template.GenTemplate}, using
 * {@link OpenApiSource} as the primary source.</dd>
 * <dt>{@link OpenApiOutputItem}</dt>
 * <dd>An implementation of
 * {@link com.reprezen.genflow.api.outputitem.IOutputItem} that uses
 * {@link com.reprezen.kaizen.oasparser.model3.OpenApi3} as both its primary
 * type and its input type.
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.openapi;