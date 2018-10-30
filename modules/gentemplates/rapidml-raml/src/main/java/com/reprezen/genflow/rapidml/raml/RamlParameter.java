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

import org.eclipse.emf.common.util.EList;

import com.reprezen.rapidml.Constraint;
import com.reprezen.rapidml.EnumConstant;
import com.reprezen.rapidml.Enumeration;
import com.reprezen.rapidml.Parameter;
import com.reprezen.rapidml.RegExConstraint;

public class RamlParameter extends RamlObject {
    private final Parameter rapidParameter;

    public RamlParameter(Parameter rapidParameter) {
        this.rapidParameter = rapidParameter;
    }

    public String getName() {
        return rapidParameter.getName();
    }

    public String getDisplayName() {
        return getName();
    }

    public String getDescription() {
        return getDocumentation(rapidParameter);
    }

    public String getType() {
        String type;
        switch (rapidParameter.getPrimitiveType().getName()) {
        case "anyURI":
            type = "string";
            break;
        case "duration":
            type = "string";
            break;
        case "base64Binary":
            type = "string";
            break;
        case "boolean":
            type = "boolean";
            break;
        case "date":
            type = "date";
            break;
        case "dateTime":
            type = "date";
            break;
        case "decimal":
            type = "number";
            break;
        case "double":
            type = "number";
            break;
        case "float":
            type = "number";
            break;
        case "gMonth":
            type = "string";
            break;
        case "gMonthDay":
            type = "string";
            break;
        case "gDay":
            type = "string";
            break;
        case "gYearMonth":
            type = "string";
            break;
        case "gYear":
            type = "string";
            break;
        case "QName":
            type = "string";
            break;
        case "time":
            type = "string";
            break;
        case "string":
            type = "string";
            break;
        case "NCName":
            type = "string";
            break;
        case "int":
            type = "integer";
            break;
        case "integer":
            type = "integer";
            break;
        case "long":
            type = "integer";
            break;
        default:
            type = "string";
        }
        return type;
    }

    public boolean isRequired() {
        return rapidParameter.isRequired();
    }

    public boolean hasDefaultValue() {
        return rapidParameter.getDefault() != null;
    }

    public String getDefaultValue() {
        return rapidParameter.getDefault();
    }

    public boolean isStringEnumeration() {
        return rapidParameter.getType() instanceof Enumeration && isTypeString();
    }

    public String[] getEnumerationConstants() {
        if (!this.isStringEnumeration()) {
            return null;
        }
        ArrayList<String> constants = new ArrayList<String>();
        for (EnumConstant constant : ((Enumeration) rapidParameter.getType()).getEnumConstants()) {
            constants.add(constant.getLiteralValue().toString());
        }
        String[] response = new String[constants.size()];
        return constants.toArray(response);

    }

    public boolean hasConstraint() {
        return !rapidParameter.getConstraints().isEmpty();
    }

    public RamlConstraint getConstraint() {
        if (!hasConstraint()) {
            return null;
        }
        EList<Constraint> constraints = rapidParameter.getConstraints();
        // Should never be more than 2 constraints.
        if (constraints.size() > 2) {
            throw new RuntimeException("More than 2 constraints found: " + constraints.size() + NL + constraints);
        }
        Constraint constraint1 = constraints.get(0);
        // If there are 2 constraints then the RegexConstraint takes precedence for RAML.
        if (constraints.size() == 2) {
            Constraint constraint2 = constraints.get(1);
            if (constraint1 instanceof RegExConstraint) {
                return new RamlConstraint(constraint1);
            } else if (constraint2 instanceof RegExConstraint) {
                return new RamlConstraint(constraint2);
            } else {
                throw new RuntimeException("Parameter has 2 constraints neither of which is a RegExConstraint");
            }
        }
        return new RamlConstraint(constraint1);
    }

    private boolean isTypeString() {
        return getType().equals("string");
    }
}
