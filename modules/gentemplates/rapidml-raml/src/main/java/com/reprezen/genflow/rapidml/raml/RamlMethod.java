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
import com.reprezen.rapidml.TypedResponse;

public class RamlMethod extends RamlObject {

    private final Method rapidMethod;
    private Logger logger;

    public RamlMethod(Method rapidMethod) {
        this.rapidMethod = rapidMethod;
    }

    public String getVerb() {
        return rapidMethod.getHttpMethod().getName().toLowerCase();
    }

    public String getDescription() {
        return getDocumentation(rapidMethod);
    }

    public RamlRequest getRequest() {
        RamlRequest result = new RamlRequest(rapidMethod.getRequest());
        result.attachLogger(logger);
        return result;
    }

    public List<RamlResponse> getResponses() {
        ArrayList<RamlResponse> ramlResponses = new ArrayList<RamlResponse>();
        for (TypedResponse rapidResponse : rapidMethod.getResponses()) {
            RamlResponse ramlResponse = new RamlResponse(rapidResponse);
            ramlResponse.attachLogger(logger);
            ramlResponses.add(ramlResponse);
        }
        return ramlResponses;
    }

    public void attachLogger(Logger logger) {
        this.logger = logger;
    }
}
