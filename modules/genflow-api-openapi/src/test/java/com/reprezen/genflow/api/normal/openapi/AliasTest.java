package com.reprezen.genflow.api.normal.openapi;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;

public class AliasTest extends NormalizerTestBase {

    private Operation op;

    @Before
    public void setup() {
        op = spec.getPaths().get("/testAliases").getOperationMap().get(HttpMethod.GET);
    }

    @Test
    public void testConsumes() {
        testMedia(op.getConsumes());
    }

    @Test
    public void testProduces() {
        testMedia(op.getProduces());
    }

    public void testMedia(List<String> media) {
        String[] expected = { "application/json", "application/xml" };
        assertEquals("Wrong size media list", expected.length, media.size());
        for (String type : media) {
            assertTrue("Missing media type: " + type, media.contains(type));
        }
    }
}
