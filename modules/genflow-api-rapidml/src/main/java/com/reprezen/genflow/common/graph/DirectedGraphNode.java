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
import java.util.List;

/**
 * A vertex of a directed graph.
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class DirectedGraphNode<T> {

	private List<DirectedGraphNode<T>> edgesToOtherElements = new ArrayList<DirectedGraphNode<T>>();
	private final T value;

	public DirectedGraphNode(T value) {
		this.value = value;
	}

	/**
	 * @param target - the destination of the newly created edge Creates an edge
	 *               from the current element to the provided target element.
	 */
	public void addEdgeTo(DirectedGraphNode<T> target) {
		edgesToOtherElements.add(target);
	}

	/**
	 * @return all outgoing edges from the current element.
	 */
	public Collection<DirectedGraphNode<T>> edgesFrom() {
		return edgesToOtherElements;
	}

	@Override
	public boolean equals(Object obj) {
		// Use default
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return value.toString();
	}

}
