/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.xtend;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;

/**
 * @author Konstantin Zaitsev
 * @date May 30, 2015
 */
public class XJsonSchemaGenTemplate extends ZenModelGenTemplate {

    @Override
    public String getName() {
        return "JSON Schema"; //$NON-NLS-1$
    }

    @Override
    public void configure() throws GenerationException {
        alsoKnownAs("com.modelsolv.reprezen.gentemplates.jsonschema.xtend.XJsonSchemaGenTemplate");
        defineZenModelSource();
        defineParameterizedOutputItem(outputItem().using(XGenerateJsonSchema.class), "JSON", "${zenModel.name}.json");
        define(GenTemplateProperty.reprezenProvider());
    }
}