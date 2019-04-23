/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.graph;

import static com.reprezen.rapidml.util.RapidmlModelUtils.getZenModel;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.reprezen.rapidml.ReferenceLink;
import com.reprezen.rapidml.ResourceDefinition;
import com.reprezen.rapidml.ServiceDataResource;
import com.reprezen.rapidml.ZenModel;

/**
 * Topologically sorts resources to achieve the left-to-right direction of
 * reference links.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class ResourceSorter {

	private BiMap<ServiceDataResource, DirectedGraphNode<String>> resourceToGraph = HashBiMap
			.<ServiceDataResource, DirectedGraphNode<String>>create();

	private List<DirectedGraphNode<String>> graph = new ArrayList<>();

	/**
	 * Topologically sorts resources to achieve the left-to-right direction of
	 * reference links.
	 * 
	 * @param service data resources
	 * @return a list of service data resources ordered to favor left-to-right
	 *         direction of reference links.
	 */
	public List<ServiceDataResource> sort(List<ServiceDataResource> resources) {
		DirectedGraph<String> graph = buildResourcesGraph(resources);
		List<DirectedGraphNode<String>> sorted = TopologicalSorter.splitAndSort(graph);
		List<ServiceDataResource> result = new ArrayList<>();
		BiMap<DirectedGraphNode<String>, ServiceDataResource> graphNodeToResource = resourceToGraph.inverse();
		for (DirectedGraphNode<String> node : sorted) {
			ServiceDataResource resource = graphNodeToResource.get(node);
			result.add(resource);
		}
		return result;
	}

	/**
	 * Builds a directed graph consisting of {@link DirectedGraphNode} from the
	 * provided service data resources. Resources are used as vertices, reference
	 * links are used as edges.
	 * 
	 * @param resources
	 * @return a directed graph
	 */
	protected DirectedGraph<String> buildResourcesGraph(List<ServiceDataResource> resources) {
		resourceToGraph.clear();
		// build graph nodes first to preserve order
		for (ServiceDataResource next : resources) {
			createNode(next);
		}
		for (ServiceDataResource next : resources) {
			DirectedGraphNode<String> graphNode = getOrCreateNode(next);
			ZenModel model = getZenModel(next);
			for (ReferenceLink link : next.getReferenceLinks()) {
				ResourceDefinition target = link.getTargetResource();
				if (target == null || model != getZenModel(target)) {
					continue;
				}
				DirectedGraphNode<String> linkedResourceGraphNode = getOrCreateNode((ServiceDataResource) target);
				graphNode.addEdgeTo(linkedResourceGraphNode);
			}
		}
		return new DirectedGraph<>(graph);
	}

	private DirectedGraphNode<String> getOrCreateNode(ServiceDataResource resource) {
		DirectedGraphNode<String> graphNode = getNode(resource);
		if (graphNode != null) {
			return graphNode;
		}
		return createNode(resource);
	}

	private DirectedGraphNode<String> getNode(ServiceDataResource resource) {
		DirectedGraphNode<String> graphNode = resourceToGraph.get(resource);
		return graphNode;
	}

	private DirectedGraphNode<String> createNode(ServiceDataResource resource) {
		DirectedGraphNode<String> graphNode = new DirectedGraphNode<String>(resource.getName());
		resourceToGraph.put(resource, graphNode);
		graph.add(graphNode);
		return graphNode;
	}
}
