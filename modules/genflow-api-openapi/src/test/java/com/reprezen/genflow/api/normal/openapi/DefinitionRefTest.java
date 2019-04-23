/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Response;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;

/**
 * @author Andy Lowry
 * 
 */
public class DefinitionRefTest extends NormalizerTestBase {

    private Map<String, Response> responses;

    @Before
    public void setup() {
        this.responses = spec.getPath("/testDefinitionRefs").getOperationMap().get(HttpMethod.GET).getResponses();
    }

    @Test
    public void testDefinitionSignatures() {
        checkDefinitionSignature("200", "string");
        checkDefinitionSignature("201", "{a:{b:{a:ref}},b:{a:{b:ref}}}");
        checkDefinitionSignature("201", "{a:{b:{a:ref}},b:{a:{b:ref}}}");
    }

    private void checkDefinitionSignature(String code, String sig) {
        Property schema = responses.get(code).getSchema();
        assertNotNull("Missing response code" + code, schema);
        assertEquals("Wrong schema signature for response code " + code, sig, signature(schema));
    }

    private String signature(Model schema) {
        if (schema instanceof ModelImpl) {
            return signature((ModelImpl) schema);
        } else {
            return "";
        }
    }

    private String signature(ModelImpl schema) {
        switch (schema.getType()) {
        case "object":
            return signature(schema.getProperties());
        default:
            return schema.getType();
        }
    }

    private String signature(Map<String, Property> properties) {
        List<String> propSigs = Lists.newArrayList();
        for (Entry<String, Property> entry : sortedMapEntries(properties)) {
            propSigs.add(entry.getKey() + ":" + signature(entry.getValue()));
        }
        return "{" + StringUtils.join(propSigs, ",") + "}";
    }

    private String signature(Property property) {
        if (property instanceof ObjectProperty) {
            return signature(((ObjectProperty) property).getProperties());
        } else {
            return property.getType();
        }
    }

    private <K, V> Iterable<Entry<K, V>> sortedMapEntries(Map<K, V> map) {
        List<Entry<K, V>> entries = Lists.newArrayList(map.entrySet());
        Collections.sort(entries, new Comparator<Entry<K, V>>() {
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return ((String) o1.getKey()).compareTo((String) o2.getKey());
            }
        });
        return entries;
    }
}
