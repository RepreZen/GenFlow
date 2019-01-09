/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.swagger.models.HttpMethod;
import io.swagger.models.Path;

/**
 * @author Andy Lowry
 * 
 */
public class MediaTypeHoistingTest extends NormalizerTestBase {

    @Test
    public void testHoisting() {
        assertMediaTypesHoisted("None", true, true);
        assertMediaTypesHoisted("ConsumesOnly", false, true);
        assertMediaTypesHoisted("ProducesOnly", true, false);
        assertMediaTypesHoisted("Both", false, false);
    }

    private void assertMediaTypesHoisted(String suffix, boolean consumesHoisted, boolean producesHoisted) {
        String pathName = "/testMediaTypes";
        Path path = spec.getPath(pathName + suffix);
        checkHoisted(path.getOperationMap().get(HttpMethod.GET).getConsumes(), consumesHoisted, pathName, "consumes");
        checkHoisted(path.getOperationMap().get(HttpMethod.GET).getProduces(), producesHoisted, pathName, "produces");
    }

    private void checkHoisted(List<String> types, boolean isHoisted, String pathName, String listName) {
        List<String> expected = Arrays.asList(isHoisted ? "text/hoisted" : "text/unhoisted");
        String msg = String.format("GET %s '%s' list is incorrect", pathName, listName);
        assertEquals(msg, expected, types);
    }
}
