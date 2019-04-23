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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.normal.openapi.Reference;

import io.swagger.models.HttpMethod;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.RefParameter;
import io.swagger.models.parameters.SerializableParameter;

/**
 * @author Andy Lowry
 * 
 */
public class ParameterTest extends NormalizerTestBase {

    private List<Parameter> params;
    private final Set<Parameter> notedParams = Sets.newHashSet();

    @Before
    public void setup() {
        params = spec.getPath("/testParams").getOperationMap().get(HttpMethod.GET).getParameters();
    }

    @Test
    public void parametersAreCorrect() {
        notedParams.add(assertHasParam("local", "query", "string"));
        notedParams.add(assertHasParam("globalOverridden", "query", "integer"));
        // notedParams.add(assertHasParam("globalNotOverridden", "query", "string"));
        notedParams.add(assertHasParam("pathOverridden", "query", "integer"));
        notedParams.add(assertHasParam("pathNotOverridden", "query", "string"));
        notedParams.add(assertHasParam("externalOverridden", "query", "integer"));
        notedParams.add(assertHasParam("externalNotOverridden", "query", "string"));
        // notedParams.add(assertUnresolvedParamRef("#/parameters/noSuchParameter"));
        // notedParams.add(assertUnresolvedParamRef("./ext/params.yaml#/parameters/noSuchParameter"));
        for (Parameter param : params) {
            assertNoted(param);
        }
    }

    private Parameter assertHasParam(String name, String in, String type) {
        Optional<Parameter> param = findParam(name, in);
        assertTrue(String.format("Missing %s parameter: %s", in, name), param.isPresent());
        assertTrue(param.get() instanceof SerializableParameter);
        SerializableParameter serParam = (SerializableParameter) param.get();
        assertEquals(String.format("%s parameter %s wrong type", in, name), type, serParam.getType());
        return param.get();
    }

    private Parameter assertUnresolvedParamRef(String refString) {
        for (Parameter param : params) {
            if (param instanceof RefParameter) {
                RefParameter refParam = (RefParameter) param;
                if (refParam.get$ref().equals("#/" + Reference.UNRESOLVABLE_NAME + "/" + refString)) {
                    return param;
                }
            }
        }
        fail("Missing unresolved ref parameter with ref string: " + refString);
        return null;
    }

    private void assertNoted(Parameter param) {
        String msg;
        if (param instanceof SerializableParameter) {
            msg = String.format("Unexpected %s parameter %s of type %s", param.getIn(), param.getName(),
                    ((SerializableParameter) param).getType());
        } else if (param instanceof RefParameter) {
            msg = String.format("Unexpected ref parameter with ref string %s", ((RefParameter) param).get$ref());
        } else {
            msg = String.format("Unexpected parameter of class %s", param.getClass());
        }
        assertTrue(msg, notedParams.contains(param));
    }

    private Optional<Parameter> findParam(String name, String in) {
        for (Parameter param : params) {
            if (param instanceof RefParameter) {
                continue;
            }
            if (name.equals(param.getName()) && in.equals(param.getIn())) {
                return Optional.of(param);
            }
        }
        return Optional.absent();
    }
}
