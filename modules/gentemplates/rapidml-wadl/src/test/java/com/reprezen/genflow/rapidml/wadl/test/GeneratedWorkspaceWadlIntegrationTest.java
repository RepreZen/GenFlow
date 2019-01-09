/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl.test;

import static com.reprezen.genflow.rapidml.wadl.XmlDomMatchers.hasValue;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasName;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasStyle;
import static com.reprezen.genflow.rapidml.wadl.test.WadlDomMatchers.hasType;
import static com.reprezen.genflow.rapidml.wadl.test.WadlFunctions.getParametersWithId;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Node;

import com.reprezen.genflow.test.common.GeneratorTestFixture.SampleRestFile;

@SampleRestFile("WorkspaceSetup.rapid")
@SuppressWarnings("nls")
public class GeneratedWorkspaceWadlIntegrationTest {
    @Rule
    public WadlGeneratorIntegrationTestFixture fixture = new WadlGeneratorIntegrationTestFixture();

    @Test
    public void testGeneratedWadlIsValid() {
        fixture.assertGeneratedWadlIsValid();
    }

    @Test
    public void testReferenceToGrammar() throws Exception {
        fixture.requireGrammar("workspaceSetup.xsd");
    }

    /**
     * <resources> <resource id="OldComputer" path="/computer/{name}" type="#OldComputerType"> </resources>
     * <resource_type id="OldComputerType"><param id="OldComputer_resource_computerName" name="computerName" style=
     * "template" type="xs:string" />
     * <param id="OldComputer_resource_cpuType" name="cpuType" style="matrix" type="xs:string" />
     * <param id="OldComputer_resource_cpuClock" name="cpuClock" style="matrix" type="xs:decimal" />
     */
    @Test
    public void testOldComputerResource() throws Exception {
        Node node = fixture.requireResource("OldComputer");
        assertThat(node, hasValue("path", "computer/{computerName}"));
        assertThat(node, hasType("#OldComputerType"));
    }

    @Test
    public void testOldComputerResource_matrixParameters() throws Exception {
        Node oldComputerResource = fixture.requireResourceType("OldComputer");
        List<Node> params = getParametersWithId("OldComputer_resource_cpuType").apply(oldComputerResource);
        assertThat(params.size(), equalTo(1));
        Node cpuType = params.get(0);
        assertNotNull(cpuType);
        assertThat(cpuType, hasStyle("matrix"));
        assertThat(cpuType, hasName("cpuType"));
        assertThat(cpuType, hasType("xs:string"));

        params = getParametersWithId("OldComputer_resource_cpuClock").apply(oldComputerResource);
        assertThat(params.size(), equalTo(1));
        Node cpuClock = params.get(0);
        assertNotNull(cpuClock);
        assertThat(cpuClock, hasStyle("matrix"));
        assertThat(cpuClock, hasType("xs:decimal"));
        assertThat(cpuClock, hasName("cpuClock"));
    }

    @Test
    public void testOldComputerResource_templateParam() throws Exception {
        Node oldComputerResource = fixture.requireResourceType("OldComputer");
        List<Node> params = getParametersWithId("OldComputer_resource_computerName").apply(oldComputerResource);
        assertThat(params.size(), equalTo(1));
        Node computerName = params.get(0);
        assertNotNull(computerName);
        assertThat(computerName, hasStyle("template"));
        assertThat(computerName, hasType("xs:string"));
        assertThat(computerName, hasName("computerName"));
    }

    @Test
    public void testGetOldComputerMethod() throws Exception {
        Node method = fixture.requireMethodById("OldComputer", "getComputer");
        assertThat(method, hasName("GET"));
    }

    @Test
    public void testPutOldComputerMethod() throws Exception {
        Node method = fixture.requireMethodById("OldComputer", "putComputer");
        assertThat(method, hasName("PUT"));
    }

}
