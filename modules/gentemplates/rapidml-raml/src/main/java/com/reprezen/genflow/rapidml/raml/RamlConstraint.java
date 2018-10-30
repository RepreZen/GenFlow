/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.raml;

import com.reprezen.rapidml.Constraint;
import com.reprezen.rapidml.LengthConstraint;
import com.reprezen.rapidml.RegExConstraint;
import com.reprezen.rapidml.ValueRangeConstraint;

public class RamlConstraint {
    private Constraint rapidConstraint;

    public RamlConstraint(Constraint rapidConstraint) {
        this.rapidConstraint = rapidConstraint;
    }

    // Methods for ValueRangeConstraint
    public boolean isIntValueRangeConstraint() {
        return rapidConstraint instanceof ValueRangeConstraint;
    }

    public String getMinimum() {
        if (!isIntValueRangeConstraint()) {
            return null;
        }
        return ((ValueRangeConstraint) rapidConstraint).getMinValue();
    }

    public String getMaximum() {
        if (!isIntValueRangeConstraint()) {
            return null;
        }
        return ((ValueRangeConstraint) rapidConstraint).getMaxValue();
    }

    // Methods for LengthConstraint
    public boolean isStringLengthConstraint() {
        return rapidConstraint instanceof LengthConstraint;
    }

    public int getMinLength() {
        if (!isStringLengthConstraint()) {
            return -1;
        }
        return ((LengthConstraint) rapidConstraint).getMinLength();
    }

    public int getMaxLength() {
        if (!isStringLengthConstraint()) {
            return -1;
        }
        return ((LengthConstraint) rapidConstraint).getMaxLength();
    }

    // Methods for RegExConstraint
    public boolean isStringRegExConstraint() {
        return rapidConstraint instanceof RegExConstraint;
    }

    public String getPattern() {
        if (!isStringRegExConstraint()) {
            return null;
        }
        return ((RegExConstraint) rapidConstraint).getPattern();
    }
}
