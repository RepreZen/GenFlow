/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import com.reprezen.rapidml.TypedResponse;

public class RamlResponse extends RamlMessage {

    private TypedResponse rapidResponse;

    public RamlResponse(TypedResponse rapidResponse) {
        super(rapidResponse);
        this.rapidResponse = rapidResponse;
    }

    public int getStatusCode() {
        return rapidResponse.getStatusCode();
    }
}
