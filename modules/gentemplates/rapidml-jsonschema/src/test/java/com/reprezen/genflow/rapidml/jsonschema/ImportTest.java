/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jsonschema;

import org.junit.Rule;
import org.junit.Test;

import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemaKeywords;
import com.reprezen.genflow.rapidml.jsonschema.help.JSONSchemeGeneratorTestFixture;
import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("import/JsonSchemaImportTest1.rapid")
public class ImportTest {

    private final static String MODEL_1_LABEL = "JsonSchemaImportTestModel1"; //$NON-NLS-1$
    private final static String MODEL_2_LABEL = "JsonSchemaImportTestModel2"; //$NON-NLS-1$
    private final static String MODEL_3_LABEL = "JsonSchemaImportTestModel3"; //$NON-NLS-1$

    @Rule
    public JSONSchemeGeneratorTestFixture fixture = new JSONSchemeGeneratorTestFixture();

    @Test
    public void testIsValidJSONSchema() throws Exception {
        fixture.isValidJsonSchema();
    }

    @Test
    public void testImport() throws Exception {
        checkEnumFromModel("enum1", MODEL_1_LABEL); //$NON-NLS-1$
        checkEnumFromModel("enum3", MODEL_1_LABEL); //$NON-NLS-1$
        checkEnumFromModel("enum2", MODEL_2_LABEL); //$NON-NLS-1$
        checkEnumFromModel("JsonSchemaImportTestModel2.dm2.enum4", MODEL_2_LABEL); //$NON-NLS-1$
        checkEnumFromModel("JsonSchemaImportTestModel2.dm2.enum3", MODEL_2_LABEL); //$NON-NLS-1$
        checkEnumFromModel("ns.JsonSchemaImportTestModel3.dm3.enum4", MODEL_3_LABEL); //$NON-NLS-1$

        checkObjectRefTo("wEnum1fromModel1", "enum1"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum3fromModel1", "enum3"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum2fromModel2", "enum2"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum3fromModel2", "JsonSchemaImportTestModel2.dm2.enum3"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum4fromModel2", "JsonSchemaImportTestModel2.dm2.enum4"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum4fromModel3", "ns.JsonSchemaImportTestModel3.dm3.enum4"); //$NON-NLS-1$ //$NON-NLS-2$
        checkObjectRefTo("wEnum3fromModel12", "JsonSchemaImportTestModel2.dm2.enum3"); //$NON-NLS-1$ //$NON-NLS-2$

    }

    private void checkObjectRefTo(String objectName, String enumQName) throws Exception {
        JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.REF_FIELD_NAME,
                fixture.getDefinition(objectName).get(JSONSchemaKeywords.PROPERTIES).get("field"), //$NON-NLS-1$
                JSONSchemaKeywords.PROPERTIES_DEF_PREFIX + enumQName);
    }

    private void checkEnumFromModel(String enumName, String modelLabel) throws Exception {
        JSONSchemeGeneratorTestFixture.checkFieldValue(JSONSchemaKeywords.ENUM, fixture.getDefinition(enumName),
                new String[] { modelLabel });
    }
}
