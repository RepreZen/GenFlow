/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema.help;

/**
 * @author Konstantin Zaitsev
 * @date Mar 18, 2015
 */
public final class NodeCheckerFactory {
    public static JsonNodeChecker createNumberConstraintsChecker(String type, String format, String min, String max,
            Boolean exMin, Boolean exMax) {
        JsonNodeChecker checker = new TypedFieldJsonNodeChecker(false, type);
        if (format != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.FORMAT, true, new StringFieldNodeChecker(format));
        }
        if (min != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.MINIMUM, true, new StringFieldNodeChecker(min));
        }
        if (max != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.MAXIMUM, true, new StringFieldNodeChecker(max));
        }
        if (exMin != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.EXCLUSIVE_MINIMUM, true, new BooleanFieldNodeChecker(exMin));
        }
        if (exMax != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.EXCLUSIVE_MAXIMUM, true, new BooleanFieldNodeChecker(exMax));
        }
        return checker;
    }

    public static JsonNodeChecker createStringConstraintsChecker(Integer minLength, Integer maxLength, String pattern) {
        JsonNodeChecker checker = new TypedFieldJsonNodeChecker(false, JSONSchemaKeywords.TYPE_STRING);
        if (minLength != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.MIN_LENGTH, true, new IntegerFieldNodeChecker(minLength));
        }
        if (maxLength != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.MAX_LENGTH, true, new IntegerFieldNodeChecker(maxLength));
        }
        if (pattern != null) {
            checker.addJsonNodeChecker(JSONSchemaKeywords.PATTERN, true, new StringFieldNodeChecker(pattern));
        }
        return checker;
    }
}
