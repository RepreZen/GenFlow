/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

/**
 * An interface to facilitate GenTemplate registration. This interface is for
 * registration purposes only, it's not intended to be used to create
 * categories. Therefore, it's anemic - does not have a name or description, in
 * only provides a method to get a collection of GenTemplates. IGenTemplateGroup
 * is registered the same way as IGenTemplates - via java.util.ServiceLoader
 * 
 * @author Tatiana Fesenko
 * 
 */
public interface IGenTemplateGroup {
	Iterable<IGenTemplate> getGenTemplates(ClassLoader classLoader);
}
