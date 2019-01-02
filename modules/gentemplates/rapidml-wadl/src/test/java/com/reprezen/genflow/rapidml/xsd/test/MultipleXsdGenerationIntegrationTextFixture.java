/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd.test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Extension of XsdGeneratorIntegrationTestFixture that allows generator create many .xsd files.
 * 
 * XsdGeneratorIntegrationTestFixture expects that generator creates only one .xsd file.
 * XsdGeneratorIntegrationTestFixture filters generator's output and uses found .xsd file as target for testing.
 * 
 * MultipleXsdGenerationIntegrationTextFixture allows generator creates many .xsd files.
 * MultipleXsdGenerationIntegrationTextFixture chooses target file for testing by name according to 'targetFileName'
 * field.
 */
public class MultipleXsdGenerationIntegrationTextFixture extends XsdGeneratorIntegrationTestFixture {
    private final String targetFileName;

    public MultipleXsdGenerationIntegrationTextFixture(String targetFileName) {
        super();
        this.targetFileName = targetFileName;
    }

    @Override
    protected File prepareFile(File scratchDir, Map<String, String> generated, String extension) throws IOException {
        File targetFile = null;
        for (Entry<String, String> entry : generated.entrySet()) {
            String filePath = entry.getKey();
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                file = new File(scratchDir, filePath);
            }
            Files.write(file.toPath(), entry.getValue().getBytes());
            if (file.getName().equals(targetFileName)) {
                targetFile = file;
            }
        }
        if (null == targetFile) {
            throw new RuntimeException("Target file " + targetFileName + " isn't generated.");
        }
        return targetFile;
    }
}
