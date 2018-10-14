/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
/**
 * Utility classes used by the CodeGen framework.
 * <p>
 * Classes include:
 * <dl>
 * <dt>{@link Eval}</dt>
 * <dd>Includes methods for using MVEL2 to expand templates and evaluate
 * expressions, and for creating contexts for such uses.</dd>
 * <dt>{@link FileUtils}</dt>
 * <dd>Includes methods for copying Java resources to external files. Supports
 * resources in Jar files or in the file system, depending on the code source
 * from which a class provided by the caller was loaded.</dd>
 * <dt>{@link GeneratorLauncher}
 * <dt>
 * <dd>Provides support for launching GenTargets, either from code or from the
 * command line (this class includes the necesary <code>static main</code>
 * method).</dd>
 * <dt>{@link TypeUtils}</dt>
 * <dd>Provides support for determining the actual types of generic type
 * arguments in limited circumstances, but sufficient for the needs of the
 * CodeGen API</dd>
 * </dl>
 * 
 * @author Andy Lowry
 *
 */
package com.reprezen.genflow.api.util;