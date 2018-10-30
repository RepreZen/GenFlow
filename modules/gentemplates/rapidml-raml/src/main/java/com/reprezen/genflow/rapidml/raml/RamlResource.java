/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.reprezen.rapidml.Method;
import com.reprezen.rapidml.ResourceDefinition;
import com.reprezen.rapidml.URIParameter;

class RamlResource extends RamlObject implements Comparable<RamlResource> {

    private final ResourceDefinition rapidResource;
    private Logger logger;

    public RamlResource(ResourceDefinition rapidResource) {
        this.rapidResource = rapidResource;
    }

    @Override
    public int compareTo(RamlResource other) {
        return this.getURI().compareTo(other.getURI());
    }

    public String getURI() {
        if (rapidResource.getURI() == null) {
            return UNDEFINED + "_" + hashCode();
        }
        return rapidResource.getURI().toString();
    }

    public String getDisplayName() {
        if (rapidResource.getName() == null) {
            return UNDEFINED;
        }
        return rapidResource.getName();
    }

    public String getDescription() {
        return getDocumentation(rapidResource);
    }

    public List<RamlParameter> getUriParameters() {
        List<RamlParameter> ramlUriParameters = new ArrayList<RamlParameter>();
        if (rapidResource.getURI() == null) {
            return ramlUriParameters;
        }
        for (URIParameter rapidUriParameter : rapidResource.getURI().getUriParameters()) {
            RamlParameter ramlUriParameter = new RamlParameter(rapidUriParameter);
            ramlUriParameters.add(ramlUriParameter);
        }
        return ramlUriParameters;
    }

    public List<RamlMethod> getMethods() {
        List<RamlMethod> ramlMethods = new ArrayList<RamlMethod>();
        for (Method rapidMethod : rapidResource.getMethods()) {
            RamlMethod ramlMethod = new RamlMethod(rapidMethod);
            ramlMethod.attachLogger(logger);
            ramlMethods.add(ramlMethod);
        }
        return ramlMethods;
    }

    public void attachLogger(Logger logger) {
        this.logger = logger;
    }

}
