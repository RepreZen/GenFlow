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
import org.eclipse.xtext.linking.lazy.LazyURIEncoder;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.source.AbstractLocator;
import com.reprezen.restapi.ZenModel;

public class ZenModelLocator extends AbstractLocator<ZenModel> {

    private ZenModel model;
    private LazyURIEncoder uriEncoder = new LazyURIEncoder();

    public ZenModelLocator(ZenModel model) {
        this.model = model;
    }

    @Override
    public <T> String locate(T item) throws GenerationException {
        if (EObject.class.isAssignableFrom(item.getClass())) {
            EObject eObject = (EObject) item;
            StringBuilder result = new StringBuilder();
            uriEncoder.appendShortFragment(eObject, result);
            return result.toString();
        } else {
            throw new GenerationException("Cannot create ZenModel locator for item of type " + item.getClass());

        }
    }

    @Override
    public Object dereference(String locator) {
        return uriEncoder.resolveShortFragment(model.eResource(), locator);
    }
    
    public EObject dereferenceEObject(String locator) {
        return uriEncoder.resolveShortFragment(model.eResource(), locator);
    }
}
