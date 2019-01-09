/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;

import io.swagger.models.HttpMethod;
import io.swagger.models.Response;

/**
 * @author Andy Lowry
 * 
 */
public class ResponseRefTest extends NormalizerTestBase {

    private Map<String, Response> responses;
    private static final Set<Response> notedResponses = Sets.newHashSet();

    @Before
    public void setup() {
        responses = spec.getPath("/testResponseRefs").getOperationMap().get(HttpMethod.GET).getResponses();
    }

    @Test
    public void testResponseRefs() {
        notedResponses.add(checkForResponse("200", "Standard Response", "string"));
        notedResponses.add(checkForResponse("404", "Not Found Response", null));
        notedResponses.add(checkForResponse("500", "Error Response", "object"));
    }

    @Test
    public void testAllResponsesNoted() {
        for (Entry<String, Response> e : responses.entrySet()) {
            if (!notedResponses.contains(e.getValue())) {
                fail("Unexpected response for code " + e.getKey());
            }
        }
    }

    private Response checkForResponse(String code, String expectedDesc, String expectedType) {
        Response resp = responses.get(code);
        assertNotNull("Missing response for code " + code, resp);
        assertEquals("Incorrect description for response code " + code, expectedDesc, resp.getDescription());
        String actualType = resp.getSchema() != null ? resp.getSchema().getType() : null;
        assertEquals("Incorrect schema type for response code " + code, expectedType, actualType);
        return resp;
    }
}
