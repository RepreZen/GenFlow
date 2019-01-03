/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.reprezen.genflow.common.graph.DirectedGraph;
import com.reprezen.genflow.common.graph.DirectedGraphNode;
import com.reprezen.genflow.common.graph.GraphSplitter;
import com.reprezen.genflow.common.graph.TopologicalSorter;

/**
 * A set of JUnit tests for teh topological sorter.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 *
 */
public class TopologicalSorterTest {
    private List<DirectedGraphNode<String>> graph;
    private DirectedGraphNode<String> nodeA;
    private DirectedGraphNode<String> nodeB;
    private DirectedGraphNode<String> nodeC;
    private DirectedGraphNode<String> nodeD;
    private DirectedGraphNode<String> nodeE;

    @Before
    public void setUp() {
        graph = new ArrayList<DirectedGraphNode<String>>();
    }

    @Test
    public void test1() {
        addABCDE();
        nodeA.addEdgeTo(nodeB);
        nodeC.addEdgeTo(nodeD);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeA, nodeB, nodeC, nodeD, nodeE);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void test2() {
        addABCDE();
        nodeA.addEdgeTo(nodeD);
        nodeC.addEdgeTo(nodeA);
        nodeC.addEdgeTo(nodeB);
        nodeE.addEdgeTo(nodeD);
        nodeE.addEdgeTo(nodeA);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeC, nodeB, nodeE, nodeA, nodeD);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void test3() {
        addABCDE();
        nodeC.addEdgeTo(nodeA);
        nodeE.addEdgeTo(nodeD);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeB, nodeC, nodeA, nodeE, nodeD);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void test4() {
        addABCDE();
        nodeB.addEdgeTo(nodeA);
        nodeC.addEdgeTo(nodeB);
        nodeD.addEdgeTo(nodeC);
        nodeE.addEdgeTo(nodeD);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeE, nodeD, nodeC, nodeB, nodeA);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testCycle() {
        addABCDE();
        nodeA.addEdgeTo(nodeB);
        nodeB.addEdgeTo(nodeA);
        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeA, nodeB, nodeC, nodeD, nodeE);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testCycle2() {
        addABCDE();
        nodeB.addEdgeTo(nodeA);
        nodeC.addEdgeTo(nodeB);
        nodeD.addEdgeTo(nodeC);
        nodeE.addEdgeTo(nodeD);
        nodeA.addEdgeTo(nodeD);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeE, nodeD, nodeC, nodeB, nodeA);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testCycle3() {
        addABCDE();
        nodeB.addEdgeTo(nodeA);
        nodeC.addEdgeTo(nodeB);
        nodeD.addEdgeTo(nodeC);
        nodeE.addEdgeTo(nodeD);
        nodeA.addEdgeTo(nodeD);
        nodeA.addEdgeTo(nodeC);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(nodeE, nodeD, nodeC, nodeB, nodeA);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testTaxBlasterSplitter() {
        DirectedGraphNode<String> indexNode = addNode("Index");
        DirectedGraphNode<String> usersNode = addNode("Users");
        DirectedGraphNode<String> taxFilingsNode = addNode("TaxFilings");
        DirectedGraphNode<String> userNode = addNode("User");
        DirectedGraphNode<String> taxFilingNode = addNode("TaxFiling");

        indexNode.addEdgeTo(usersNode);
        indexNode.addEdgeTo(taxFilingsNode);
        taxFilingsNode.addEdgeTo(usersNode);
        taxFilingNode.addEdgeTo(userNode);

        List<DirectedGraph<String>> subgraphs = new GraphSplitter<String>()
                .splitToDisconnectedSubGraphs(new DirectedGraph<>(graph));

        DirectedGraph<String> expectedSubgraph1 = new DirectedGraph<String>(
                newArrayList(indexNode, usersNode, taxFilingsNode));

        DirectedGraph<String> expectedSubgraph2 = new DirectedGraph<String>(newArrayList(userNode, taxFilingNode));

        List<DirectedGraph<String>> expected = Lists.newArrayList(expectedSubgraph1, expectedSubgraph2);
        assertThat(subgraphs, equalTo(expected));
    }

    @Test
    public void testTaxBlaster_order1() {
        DirectedGraphNode<String> indexNode = addNode("Index");
        DirectedGraphNode<String> usersNode = addNode("Users");
        DirectedGraphNode<String> taxFilingsNode = addNode("TaxFilings");
        DirectedGraphNode<String> userNode = addNode("User");
        DirectedGraphNode<String> taxFilingNode = addNode("TaxFiling");

        indexNode.addEdgeTo(usersNode);
        indexNode.addEdgeTo(taxFilingsNode);
        taxFilingsNode.addEdgeTo(usersNode);
        taxFilingNode.addEdgeTo(userNode);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(indexNode, taxFilingsNode, usersNode,
                taxFilingNode, userNode);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testTaxBlaster_order2() {
        DirectedGraphNode<String> userNode = addNode("User");
        DirectedGraphNode<String> taxFilingNode = addNode("TaxFiling");
        DirectedGraphNode<String> usersNode = addNode("Users");
        DirectedGraphNode<String> taxFilingsNode = addNode("TaxFilings");
        DirectedGraphNode<String> indexNode = addNode("Index");

        indexNode.addEdgeTo(usersNode);
        indexNode.addEdgeTo(taxFilingsNode);
        taxFilingsNode.addEdgeTo(usersNode);
        taxFilingNode.addEdgeTo(userNode);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(taxFilingNode, userNode, indexNode,
                taxFilingsNode, usersNode);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testTaxBlaster_order3() {
        DirectedGraphNode<String> taxFilingNode = addNode("TaxFiling");
        DirectedGraphNode<String> userNode = addNode("User");
        DirectedGraphNode<String> taxFilingsNode = addNode("TaxFilings");
        DirectedGraphNode<String> usersNode = addNode("Users");
        DirectedGraphNode<String> indexNode = addNode("Index");

        indexNode.addEdgeTo(usersNode);
        indexNode.addEdgeTo(taxFilingsNode);
        taxFilingsNode.addEdgeTo(usersNode);
        taxFilingNode.addEdgeTo(userNode);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(taxFilingNode, userNode, indexNode,
                taxFilingsNode, usersNode);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testCustomer() {
        DirectedGraphNode<String> customerNode = addNode("Customer");
        DirectedGraphNode<String> productNode = addNode("Product");
        DirectedGraphNode<String> orderNode = addNode("Order");
        DirectedGraphNode<String> bigOrderNode = addNode("BigOrder");

        customerNode.addEdgeTo(productNode);
        orderNode.addEdgeTo(productNode);
        bigOrderNode.addEdgeTo(productNode);

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(customerNode, orderNode, bigOrderNode,
                productNode);
        assertThat(sorted, equalTo(expected));
    }

    @Test
    public void testFiboEntity() {
        DirectedGraphNode<String> legalPersonNode = addNode("LegalPersonResource");
        DirectedGraphNode<String> naturalPersonNode = addNode("NaturalPersonResource");
        DirectedGraphNode<String> bodyCorporateNode = addNode("BodyCorporateResource");
        DirectedGraphNode<String> artificialPersonNode = addNode("ArtificialPersonResource");

        List<DirectedGraphNode<String>> sorted = sort();
        List<DirectedGraphNode<String>> expected = Lists.newArrayList(legalPersonNode, naturalPersonNode,
                bodyCorporateNode, artificialPersonNode);
        assertThat(sorted, equalTo(expected));
    }

    protected void addABCDE() {
        nodeA = addNode("A");
        nodeB = addNode("B");
        nodeC = addNode("C");
        nodeD = addNode("D");
        nodeE = addNode("E");
    }

    protected DirectedGraphNode<String> addNode(String value) {
        DirectedGraphNode<String> node = new DirectedGraphNode<String>(value);
        graph.add(node);
        return node;
    }

    protected List<DirectedGraphNode<String>> sort() {
        return TopologicalSorter.splitAndSort(new DirectedGraph<>(graph));
    }

}
