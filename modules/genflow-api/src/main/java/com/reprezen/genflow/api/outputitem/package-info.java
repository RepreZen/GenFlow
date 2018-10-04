/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package provides support for Output Items, which are used by
 * GenTemplates that extend
 * {@link com.modelsolv.reprezen.generators.api.template.GenTemplate} to create
 * genereated output files.
 * <p>
 * An output item is responsible for producing the content of an output file,
 * given an item of source data, plus a set of named string-valued parameters
 * appearing in the controlling GenTarget. The input item is supplied as a pair
 * of values: the overall source value loaded by the GenTemplate's primary
 * source, and an item value obtained from that source value by an extraction
 * method of {@link com.modelsolv.reprezen.generators.api.source.ISource}.
 * <p>
 * Classes in this package include:
 * <dl>
 * <dt>{@link IOutputItem}</dt>
 * <dd>Defines the interface for an output item. This is a generic interface
 * with type parameters for the source and item value types. An output item
 * intended to work on the source value as whole will specify the source value
 * type for both types.
 * <p>
 * An output item must be initialized via {@link outputitem.IOutputItem#init()}
 * before being used to generate output. This is used to provide both a
 * {@link com.modelsolv.reprezen.generators.api.trace.GenTemplateTraceBuilder}
 * and a {@link com.modelsolv.reprezen.generators.api.trace.GenTemplateTraces}
 * object for use during generation. The former allows the output item to create
 * trace information as it executes, while the latter provides access to trace
 * information from prerequisite GenTargets.
 * <dt>{@link AbstractOutputItem}</dt>
 * <dd>Provides default implementations for some methods</dd>
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.outputitem;