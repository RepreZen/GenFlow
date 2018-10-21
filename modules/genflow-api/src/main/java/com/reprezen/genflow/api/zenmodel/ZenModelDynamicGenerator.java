/*******************************************************************************
 * Copyright © 2013, 2018 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel;

import com.modelsolv.reprezen.restapi.ZenModel;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.AbstractDynamicGenerator;

public abstract class ZenModelDynamicGenerator extends AbstractDynamicGenerator<ZenModel> {

    @Override
    public Class<?> getPrimaryType() throws GenerationException {
        return ZenModel.class;
    }

}
