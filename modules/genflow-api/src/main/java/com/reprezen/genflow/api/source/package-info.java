/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * This package provides support for GenTemplate sources. The basic role of a
 * source is to load a source value from a source file. Thus, different sources
 * value types - or different storage representations for the same type -
 * require different source implementations.
 * <p>
 * Sources may also support sub-value extraction, where by substructures
 * appearing as embedded values in source values can be supplied, based on
 * various extraction APIs. The only supported extraction API at present is
 * {@link ISource#extractByType(Object, Class)}, which extracts values of the
 * supplied type from the supplied source value. Other extraction methods may be
 * added in the future.
 * <p>
 * The related concept of <em>locators</em> is also supported in this package. A
 * locator is an object that can be used to create references to substructures
 * within a source value or - given such a reference - to retrieve the
 * referenced item from the source value.
 * <p>
 * Classes in this package include:
 * <dl>
 * <dt>{@link ISource}</dt>
 * <dd>Interface for a GenTemplate source. This is a generic type parameterized
 * by the source value type.</dd>
 * <dt>{@link AbstractSource}</dt>
 * <dd>Provides default implementations for some methods of {@link ISource}. An
 * implementation of {@link ISource#extractByType(Object, Class)} is provided
 * that handles the easy case in which the supplied type is the value type
 * itself, in which case the "extracted" value is the entire source value. For
 * non-source types, method
 * {@link AbstractSource#extractByNonSourceType(Object, Class)} is provided,
 * which can be overridden by subclasses that support extraction by types other
 * than the source type. The default implementation here throws a
 * {@link com.reprezen.genflow.api.GenerationException}. This is done using a
 * the method {@link AbstractSource#cantExtractException(Class)}, which is also
 * introduced by this class and can be used by subclasses.</dd>
 * <dt>{@link ILocator}</dt>
 * <dd>Defines an interface for creating and dereferencing locator
 * references.</dd>
 * <dt>{@link AbstractLocator}</dt>
 * <dd>Defines a default implementation of
 * {@link ILocator#dereference(String, Class)}.</dd>
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.source;