/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel;

import org.eclipse.emf.ecore.EObject;

import com.modelsolv.reprezen.restapi.ZenModel;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.genflow.api.util.TypeUtils;

public abstract class ZenModelExtractOutputItem<ItemType extends EObject> extends
        AbstractOutputItem<ZenModel, ItemType> {

    @Override
    public Class<?> getPrimaryType() throws GenerationException {
        return ZenModel.class;
    }

    @Override
    public Class<?> getItemType() throws GenerationException {
        return TypeUtils.getTypeParamClass(getClass(), ZenModelExtractOutputItem.class, 0);
    }

}
