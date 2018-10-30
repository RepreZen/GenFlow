/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import java.util.List;

import com.reprezen.rapidml.HttpMessageParameterLocation;
import com.reprezen.rapidml.TypedRequest;

public class RamlRequest extends RamlMessage {

    public RamlRequest(TypedRequest request) {
        super(request);
    }

    public List<RamlParameter> getQueryParameters() {
        return getMessageParameters(HttpMessageParameterLocation.QUERY);
    }
}
