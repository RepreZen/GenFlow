/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package and its subpackages define the RepreZen CodeGen framework.
 * <p>
 * The overall conceptual framework embodied in these packages is as follows:
 * <ul>
 * <li><strong>GenTemplates</strong> turn one or more source files into output files.
 * <li><strong>GenTargets</strong> provide run-time values - such as string values for named parameters, file paths for source files, etc. - to GenTemplates.
 * They are the vehicle used to execute GenTemplates.
 * <li><strong>Dependencies</strong> define the values that must be supplied by a GenTarget when executing a particular GenTemplate
 * <li><strong>Trace</strong> information constitutes "bread crumbs" left behind after a GenTemplate executes.
 * These can be used by other GenTemplates that depend on this GenTemplate.
 * Such dependencies result in "chained" execution scenarios, wherein executing a single GenTarget can cause the execution of one or more other GenTargets as a side-effect, in order to generate their trace information.
 * <li><strong>Sources</strong> provide input values to GenTemplates.
 * <li><strong>Output Items<strong> generate output content from input content.
 * A single GenTemplate can be configured to incorporate mutiple output items.
 * <li>The framework includes specializations for many of the above for specific source types, including {@link com.modelsolv.reprezen.restapi.ZenModel} and {@link io.swagger.models.Swagger}.
 * </ul>
 *
 * @author Andy
 *
 */
package com.reprezen.genflow.api;