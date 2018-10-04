/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * Trace facilities for CodeGen.
 * <p>
 * Trace is the means by which a GenTemplate leaves a trail of evidence for the
 * output it has generated. For each piece of output, trace items can describe
 * the contributing sources and the role played by each. In both cases (outputs
 * and sources), the trace items can incorporate locators (see
 * {@link com.modelsolv.reprezen.generators.api.source.ILocator}) to identify
 * logical portions of actual source or output files. We call such portions
 * "output items" and "source items."
 * <p>
 * Classes include:
 * <dl>
 * <dt>{@link GenTemplateTrace}</dt>
 * <dd>The collection of trace information created by a GenTemplate
 * execution.</dd>
 * <dt>{@link GenTemplateTraceItem}</dt>
 * <dd>Trace information relating to a single output item, possibly with
 * multiple associated source items.</dd>
 * <dt>{@link TenTemplateTraceSourceItem}</dt>
 * <dd>Describes a single source item attached to a trace item, and its role in
 * the creatino of the associated output item.</dd>
 * <dt>{@link GenTemlpateTraceBuilder}</dt>
 * <dd>Offers a fluent API for constructing trace items and adding them to a
 * trace</dd>
 * <dt>{@link GenTargetTraces}</dt>
 * <dd>Manages a set of named traces. Traces are generally named for the
 * GenTargets whose executions produced them.</dd>
 * <dt>{@link GenTemplateTraceSerializer}</dt>
 * <dd>Supports transferring traces to and from external files, using JSON
 * structures</dd>
 * <dt>{@link GenTemplateTraceUtils}</dt>
 * <dd>Provides methods to search traces for items and source items meeting
 * specific criteria</dd>
 * </dl>
 * 
 * @author Andy
 *
 */
package com.reprezen.genflow.api.trace;