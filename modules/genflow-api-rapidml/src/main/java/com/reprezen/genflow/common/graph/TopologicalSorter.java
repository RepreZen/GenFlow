/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.graph;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Topologically sorts elements to achieve the left-to-right link direction.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * @param <V>
 * 
 */
public class TopologicalSorter<V> {

	/**
	 * Partitions the graph into disconnected subgraphs and topologically sorts the
	 * subgraphs. The left-to-right direction is guaranteed only for acyclic graphs.
	 * 
	 * @param       <DirectedGraphNode<V>> the generic type
	 * @param graph the graph
	 * @return a list of topologically sorted vertices
	 */
	public static <V> List<DirectedGraphNode<V>> splitAndSort(DirectedGraph<V> graph) {
		List<DirectedGraph<V>> subgraphs = new GraphSplitter<V>().splitToDisconnectedSubGraphs(graph);
		// Collections.reverse(subgraphs);
		List<DirectedGraphNode<V>> result = new ArrayList<>();
		for (DirectedGraph<V> subgraph : subgraphs) {
			result.addAll(new TopologicalSorter<V>(subgraph).sort());
		}
		return result;
	}

	private final DirectedGraph<V> graph;
	private final List<DirectedGraphNode<V>> reversedResult = new ArrayList<DirectedGraphNode<V>>();
	private final Set<DirectedGraphNode<V>> visited = new HashSet<DirectedGraphNode<V>>();

	public TopologicalSorter(DirectedGraph<V> graph) {
		this.graph = graph;
	}

	/**
	 * Topologically sort the graph
	 * 
	 * @return a list of topologically sorted vertices
	 */
	public List<DirectedGraphNode<V>> sort() {
		reversedResult.clear();
		visited.clear();
		ArrayList<DirectedGraphNode<V>> reversedGraph = newArrayList(graph);
		Collections.reverse(reversedGraph);
		for (DirectedGraphNode<V> n : graph) {
			reversedResult.addAll(visit(n));
		}
		Collections.reverse(reversedResult);
		return reversedResult;
	}

	private LinkedList<DirectedGraphNode<V>> visit(DirectedGraphNode<V> current) {
		LinkedList<DirectedGraphNode<V>> result = new LinkedList<>();
		if (visited.contains(current)) {
			return result;
		}
		visited.add(current);
		for (DirectedGraphNode<V> next : current.edgesFrom()) {
			result.addAll(visit(next));
		}
		result.add(current);
		return result;
	}

}
