/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * Builders for all the configuration items available to GenTemplates.
 * <p>
 * A builder class named <code>XxxBuilder</code> creates instances of
 * <code>XxxSpec</code>, which is a member of the builder class. Each spec class
 * includes local validation that throws
 * {@link com.modelsolv.reprezen.generators.api.GenerationException} if it is
 * incomplete or otherwise invalid.
 * <p>
 * The {@link com.modelsolv.reprezen.generators.api.template.GenTemplate} class
 * makes use of builders. It declares a <code>define(Xxx)</code> method and an
 * <code>xxx</code> method for each builder type. The <code>xxx</code> method
 * instantiates a builder of the correct type, and the <code>define</code>
 * method finalizes the builder and adds the resulting spec to an internal
 * collection. See
 * {@link com.modelsolv.reprezen.generators.api.template.GenTemplate#configure}
 * for more details.
 * <p>
 * A typical use of a builder therefore looks like this, appearing in an
 * override of
 * {@link com.modelsolv.reprezen.generators.api.template.GenTemplate#configure}
 * within a concrete GenTemplate implementation:
 * 
 * <pre>
 * define(xxx().named("...").with(...)...);
 * </pre>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.template.builders;