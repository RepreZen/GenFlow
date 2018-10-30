/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import com.reprezen.rapidml.Documentable;
import com.reprezen.rapidml.Documentation;

public abstract class RamlObject {

    static final String UNDEFINED = "UNDEFINED!";
    static final String NL = System.getProperty("line.separator");

    String getDocumentation(Documentable item) {
        Documentation documentation = item.getDocumentation();
        String docText = documentation != null ? documentation.getText() : "";
        return docText.isEmpty() ? "No documentation found" : docText.replace('\t', ' ');
    }

    String defaultIfValueIsNull(String value, String def) {
        return value == null ? def : value;
    }

}
