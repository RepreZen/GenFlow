/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package contains specializations of various CodeGen API classes to
 * support GenTemplates with a Swagger model as a primary source. The
 * {@link io.swagger.models.Swagger} class is used to reprezent Swagger models.
 * <p>
 * Classes include:
 * <dl>
 * <dt>{@link SwaggerSource}</dt>
 * <dd>An {@link com.reprezen.genflow.api.source.ISource} implementation that
 * can load Swagger files.</dd>
 * <dt>{@link SwaggerLocator}</dt>
 * <dd>A locator that can be used to locate and dereference structures within a
 * Swagger definition.</dd>
 * <dt>{@link SwaggerGenTemplate}</dt>
 * <dd>A class that extends
 * {@link com.reprezen.genflow.api.template.GenTemplate}, using
 * {@link SwaggerSource} as the primary source.</dd>
 * <dt>{@link SwaggerOutputItem}</dt>
 * <dd>An implementation of
 * {@link com.reprezen.genflow.api.outputitem.IOutputItem} that uses
 * {@link io.swagger.models.Swagger} as both its primary type and its input
 * type.
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.swagger;