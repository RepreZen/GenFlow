/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.modelsolv.reprezen.restapi.ZenModel;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.builders.OutputItemBuilder;

public abstract class ZenModelGenTemplate extends GenTemplate<ZenModel> {

    public ZenModelGenTemplate() {
        super();
    }

    public void defineZenModelSource() throws GenerationException {
        defineZenModelSource(true);
    }

    public void defineZenModelSource(boolean primary) throws GenerationException {
        if (primary) {
            define(primarySource().ofType(ZenModelSource.class));
        } else {
            define(namedSource().named("zenModel").ofType(ZenModelSource.class));
        }
    }

    @Override
    public Class<?> getPrimaryType() throws GenerationException {
        return ZenModel.class;
    }
    
    protected void defineParameterizedOutputItem(OutputItemBuilder builder, String name, String fileNameTemplate)
            throws GenerationException {
        String paramName = getParamNameFor(name);
        define(builder.named(name).writing(String.format("${%s}", paramName)));
        if (parameterSpecs.containsKey(GenTemplate.OUTPUT_FILES_PARAM)) {
            Object outputNamesObject = parameterSpecs.get(GenTemplate.OUTPUT_FILES_PARAM).getDefaultValue();
            if (!(outputNamesObject instanceof Map)) {
                throw new GenerationException(GenTemplate.OUTPUT_FILES_PARAM + " should be an object");
            }
            ((Map) outputNamesObject).put(paramName, fileNameTemplate);
        } else {
            HashMap<Object, Object> outputNamesObject = Maps.newHashMap();
            outputNamesObject.put(paramName, fileNameTemplate);
            define(parameter().named(GenTemplate.OUTPUT_FILES_PARAM).withDefault(outputNamesObject)
                    .withDescription(String.format("Defines the file for the '%s' output item", name)));
        }
    }

}
