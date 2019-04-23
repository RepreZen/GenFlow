/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Finds disconnected subgraphs.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class GraphSplitter<V> {

	/**
	 * Finds disconnected subgraphs inside the graph
	 * 
	 * @param directed graph
	 * @return a list of disconnected subgraphs
	 */
	public List<DirectedGraph<V>> splitToDisconnectedSubGraphs(DirectedGraph<V> graph) {
		List<LinkedHashSet<DirectedGraphNode<V>>> subgraphs = new ArrayList<>();
		for (DirectedGraphNode<V> next : graph) {
			LinkedHashSet<DirectedGraphNode<V>> subgraph = findOrCreateEntry(subgraphs, next);
			Collection<DirectedGraphNode<V>> edges = next.edgesFrom();
			for (DirectedGraphNode<V> edge : edges) {
				LinkedHashSet<DirectedGraphNode<V>> subgraph1 = findOrCreateEntry(subgraphs, edge);
				if (subgraph != subgraph1) {
					subgraph.addAll(subgraph1);
					subgraphs.remove(subgraph1);
				}
			}
		}
		List<DirectedGraph<V>> result = new ArrayList<>();
		for (LinkedHashSet<DirectedGraphNode<V>> subgraph : subgraphs) {
			result.add(new DirectedGraph<>(subgraph));
		}
		return result;
	}

	private LinkedHashSet<DirectedGraphNode<V>> findOrCreateEntry(List<LinkedHashSet<DirectedGraphNode<V>>> subgraphs,
			DirectedGraphNode<V> element) {
		for (LinkedHashSet<DirectedGraphNode<V>> subgraph : subgraphs) {
			if (subgraph.contains(element)) {
				return subgraph;
			}
		}
		LinkedHashSet<DirectedGraphNode<V>> newSubgraph = new LinkedHashSet<>();
		newSubgraph.add(element);
		subgraphs.add(newSubgraph);
		return newSubgraph;
	}

}
