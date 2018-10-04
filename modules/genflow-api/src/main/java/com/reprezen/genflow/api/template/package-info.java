/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package defines interfaces and classes for GenTemplates - the heart of
 * the CodeGen framework.
 * <p>
 * A GenTemplate is invoked by a GenTarget, which provides bindings for items
 * required by but not specified by the GenTemplate itself - items such as
 * source files, an output directory, string parameters, etc. Prerequisite
 * GenTargets are another of these bound items supplied by the controlling
 * GenTarget. In some cases, the GenTemplate can specify constraints on the
 * bound items. For example, for a prerequisite, the GenTemplate identifies the
 * GenTemplate that must be executed by the prerequisite GenTarget. Any attempt
 * to execute this GenTemplate with a GenTarget that does not supply a
 * prerequisite GenTarget satisfying this constraint - or in general any attempt
 * to execute a GenTarget that does not satisfactorily meet all the binding
 * requirements of its GenTemplate - will fail with a
 * {@link com.modelsolv.reprezen.generators.api.GenerationException}.
 * <p>
 * While the above considerations apply to most actual GenTemplates, the binding
 * mechanism is not visible at the most basic levels - the {@link IGenTemplate}
 * interface and the {@link AbstractGenTemplate} class. While it is still true
 * that GenTemplates built to these lower-level types must be executed by
 * GenTargets, the binding machinery is not available until one advances to
 * {@code SimpleGenTemplates}, which exposes bindings for primary source and
 * prerequisites. Further binding capabilities arise with {@link GenTemplate}.
 * <p>
 * Binding requirements are created in a GenTemplate implementation by
 * overriding {@link GenTemplate#configure} and including in-line definitions
 * that invoke builders, as explained in the <code><a href=
 * "builders/package-summary.html">com.modelsolv.reprezen.generators.api.template.builders</a></code>
 * package summary. It is also possible to place this information in a separate
 * configuration file. If {@link SimpleGentemplate#configure} is not overridden,
 * then its default implementation will defer to
 * {@link GenTemplate#configureByConfigFile()}. See its documentation and that
 * of {@link GenTemplate#configureByConfigFile()} for details.
 * <p>
 * A GenTemplate's binding requirements are exposed through its
 * {@link IGenTemplate#getDependencies()} method. This information is used to
 * ensure that when a collection of GenTargets are selected for execution, all
 * necessary prerequisite GenTargets are also scheduled for execution, and the
 * GenTargets are executed in the correct order.
 * <p>
 * A GenTemplate must include a resource named
 * <code>META-INF/services/com.modelsolv.reprezen.generators.api.template.IGenTemplate</code>
 * in order to be available for use. The file should include the fully-qualified
 * type name of the implementing class.
 * <p>
 * Classes in this package include:
 * <dl>
 * <dt>{@link IGenTemplate}</dt>
 * <dd>Describes the low-level interface by which GenTemplates are executed and
 * wired for execution.</dd>
 * <dt>{@link IGenTemplateGruop}</dt>
 * <dd>Provides a simple means of advertising multiple GenTemplates in one go.
 * Rather than creating a <code>META-INF/services/.../IGenTemplate</code>
 * resource for each GenTemplate individually, a resource named
 * <code>META-INF/services/.../IGenTemplateGroup</code> can be created, with the
 * fully qualified name of a class implementing {@link IGenTemplateGroup} as its
 * content. That implementing class can then provide multiple GenTemplate
 * exemplars for discovery.</dd>
 * <dt>{@link AbstractGenTemplate}</dt>
 * <dd>Fills in default implementations for most {@link IGenTemplate}
 * methods.</dd>
 * <dt>{@link GenTemplate}</dt>
 * <dd>This is the recommended base class for all GenTemplate development.
 * Introduces the notions of a <em>primary source</em> and is a generic type
 * with a type parameter specifying the type of that source. Also provides for
 * the configuration of prerequisite GenTargets and exposes them via
 * {@link IGenTemplate#getDependencies()}. Introduces the notion of multiple
 * <em>output items</em>, each generating content that to be saved in its own
 * output file. Each configured output item is an implementation of
 * {@link IOutputItem}.
 * <p>
 * This class also supports extracting embedded values from the primary source,
 * and sending each to an appropriately typed output item. This means that a
 * single output item can actually produce multiple output files from a single
 * primary source.
 * <p>
 * Finally, {@link GenTemplate} supports configuration of multiple named sources
 * in addition to the primary source, named parameters, and static resources.
 * The latter allows Java resources accompanying the GenTemplate implementation
 * to be copied out as part of the generated output. The prototypical example of
 * this is to provide web assets needed by a generated HTML file.</dd>
 * <dt>{@link GenTemplateDependency}</dt>
 * <dd>This class summarizes a binding requirement by a GenTemplate, supplying a
 * name, a type, a string of type-dependent additional info, and a flag
 * indicating whether the binding is actually required or merely optional.
 * Examples of additional info strings are a GenTemplate Id to constrain the
 * GenTargets that can be bound to a prerequisite, and a source value type (as a
 * class name) for a source binding.</dd>
 * <dt>{@link GenTemplateDependencies}</dt>
 * <dd>A container for dependencies. This class also provides static methods for
 * constructing dependencies, including a method for each type of configuration
 * builder spec that can give rise to a dependency.</dd>
 * <dt>{@link HeadlessGenTemplateRegistry}</dt>
 * <dd>This class uses Java's {@link java.util.ServiceLoader} facility to
 * discover available GenTemplate implementations, using the
 * <code>META-INF/services/...</code> resources mentioned earlier.
 * </dl>
 * 
 * @author Andy Lowry
 */
package com.reprezen.genflow.api.template;