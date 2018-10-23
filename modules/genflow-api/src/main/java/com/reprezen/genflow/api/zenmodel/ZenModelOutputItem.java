/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel;

import java.io.File;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.AbstractOutputItem;
import com.reprezen.restapi.ZenModel;

public abstract class ZenModelOutputItem extends AbstractOutputItem<ZenModel, ZenModel> {

    @Override
    public String generate(ZenModel model, ZenModel item) throws GenerationException {
        assert (model == item);
        return generate(model);
    }

    public abstract String generate(ZenModel model) throws GenerationException;

    @Override
    public final File getOutputFile(ZenModel model, ZenModel inputItem) {
        return getOutputFile(model);
    }

    public File getOutputFile(ZenModel model) {
        return null;
    }

    @Override
    public Class<?> getPrimaryType() throws GenerationException {
        return ZenModel.class;
    }

    @Override
    public Class<?> getItemType() throws GenerationException {
        return ZenModel.class;
    }

}
