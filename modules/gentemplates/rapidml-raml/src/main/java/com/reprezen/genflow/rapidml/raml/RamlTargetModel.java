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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.reprezen.rapidml.ResourceAPI;
import com.reprezen.rapidml.ResourceDefinition;
import com.reprezen.rapidml.ZenModel;

public class RamlTargetModel {
    // See http://raml.org/spec.html
    private ZenModel rapidModel;
    private ResourceAPI rapidResourceAPI;
    private Logger logger;

    public RamlTargetModel(ZenModel rapidModel, ResourceAPI rapidResourceAPI) {
        this.rapidModel = rapidModel;
        this.rapidResourceAPI = rapidResourceAPI;
    }

    // API Title: Required
    public String getTitle() {
        if (rapidModel.getNamespace() == null) {
            return rapidModel.getName() + " " + rapidResourceAPI.getName();
        }
        return rapidModel.getNamespace() + " " + rapidModel.getName() + " " + rapidResourceAPI.getName();
    }

    // API Version: Optional
    public String getVersion() {
        return "1.0.0";
    }

    // Base URI: Optional during development; Required after implementation.
    // Parameters in baseURI are supported in RAML but not in RAPID?
    public String getBaseUri() {
        return rapidResourceAPI.getBaseURI();
    }

    public List<RamlResource> getRamlResources() {
        // Create list of resources. Do not do nesting yet.
        List<RamlResource> ramlResources = new ArrayList<RamlResource>();
        for (Iterator<ResourceDefinition> it = rapidResourceAPI.getOwnedResourceDefinitions().iterator(); it
                .hasNext();) {
            ResourceDefinition rapidResource = it.next();
            RamlResource ramlResource = new RamlResource(rapidResource);
            ramlResource.attachLogger(logger);
            ramlResources.add(ramlResource);
        }
        // Sort the resources by URI
        Collections.sort(ramlResources);
        return ramlResources;
    }

    public void attachLogger(Logger logger) {
        this.logger = logger;
    }
}
