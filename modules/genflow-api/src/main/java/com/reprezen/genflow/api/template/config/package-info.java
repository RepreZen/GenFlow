/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package contains POJOs that house the items that are required to
 * configure GenTemplate classes derived from
 * {@link com.modelsolv.reprezen.generators.api.trace.GenTemplate}.
 * <p>
 * The config file for a given GenTemplate class should be available as resource
 * named either "config.json" or "<Name>-config.json", where <Name> is the
 * simple class name of the class. The latter takes precendence if both
 * resources are available. In both cases, the resource is accessed relative to
 * the defining class; e.g. the config data for a GenTemplate class named
 * x.y.ZGenTemplate should have a full resource path of either "x/y/config.json"
 * or "/x/y/ZGenTemplate-config.json"
 * <p>
 * This scheme supports the simple case of a GenTemplate class that is the only
 * such class in its package, with the config file saved in "config.json". When
 * more than one GenTemplate class appears in a single package, the other option
 * must be used.
 * <p>
 * Classes in this package include:
 * <dt>
 * <dt>{@link GenTemplateConfig}</dt>
 * <dd>The main POJO containing all the config data</dd>
 * <dt>{@link PrimarySourceConfig}, @{link PrerequisiteConfig},
 * {@link NamedSourceConfig}, {@link OutputItemConfig},
 * {@link StaticResourceConfing}, {@ParameterConfig}</dt>
 * <dd>POJOs representing config data used by
 * {@link com.modelsolv.reprezen.generators.api.trace.GenTemplate}</dd>
 * <dt>{@link GenTargetConfigUtil}</dt>
 * <dd>Class that handles loading of config data</dd>
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.template.config;