/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.graph;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A DirectedGraph consisting of {@link DirectedGraphNode}s.
 * 
 * @param <V> the node value type
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 */
public class DirectedGraph<V> implements Iterable<DirectedGraphNode<V>> {
	private final Set<DirectedGraphNode<V>> nodes;

	public DirectedGraph(Collection<DirectedGraphNode<V>> nodes) {
		// We convert all collections to sets to use it in #equals() and #hashCode() and
		// avoid conversions there
		// we use LinkedHashSet to preserve the original order
		this.nodes = new LinkedHashSet<>(nodes);
	}

	@Override
	public Iterator<DirectedGraphNode<V>> iterator() {
		return nodes.iterator();
	}

	@Override
	public String toString() {
		return Objects.toString(nodes);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("unchecked")
		final DirectedGraph<V> other = (DirectedGraph<V>) obj;
		return Objects.equals(this.nodes, other.nodes);

	}

	@Override
	public int hashCode() {
		return com.google.common.base.Objects.hashCode(this.nodes);
	}
}
