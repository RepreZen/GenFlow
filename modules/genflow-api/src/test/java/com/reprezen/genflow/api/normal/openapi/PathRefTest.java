/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.parameters.Parameter;

/**
 * @author Andy Lowry
 * 
 */
public class PathRefTest extends NormalizerTestBase {

    private Map<String, Path> paths;
    private static Set<Path> notedPaths = Sets.newHashSet();

    @Before
    public void setup() {
        paths = spec.getPaths();
    }

    @Test
    public void testPathRefs() {
        notedPaths.add(checkPath("/testPathRef1", "test1Get", "x"));
        notedPaths.add(checkPath("/testPathRef2/{id}", "test2Get", "id"));
        notedPaths.add(checkPath("/testPathRef3/{id}", "test3Get", "id"));
    }

    private Path checkPath(String pathName, String opId, String paramName) {
        Path path = paths.get(pathName);
        assertNotNull("Missing path " + pathName);
        Operation get = path.getOperationMap().get(HttpMethod.GET);
        assertNotNull("No GET operation defined for path " + pathName, get);
        assertEquals("Wrong operationId on GET " + pathName, opId, get.getOperationId());
        List<Parameter> params = get.getParameters();
        assertTrue("GET " + pathName + " should have exactly one parameter", params != null && params.size() == 1);
        Parameter param = params.get(0);
        assertEquals("Wrong parameter name for GET " + pathName, paramName, param.getName());
        return path;
    }
}
