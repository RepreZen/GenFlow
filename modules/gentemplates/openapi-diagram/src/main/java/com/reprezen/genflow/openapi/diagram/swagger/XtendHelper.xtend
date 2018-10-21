/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.diagram.swagger

import com.google.common.collect.Maps
import java.util.Collections
import java.util.Map
import java.util.SortedMap
import org.eclipse.xtext.xbase.lib.Functions
import java.util.Comparator
import java.util.LinkedList

class XtendHelper {

	def <K, V> Map<K, V> safe(Map<K, V> map) {
		map ?: Collections.<K, V>emptyMap
	}

	def <T> Iterable<T> safe(Iterable<T> list) {
		list ?: Collections.<T>emptyList
	}

	def <K extends Comparable<?>, V> SortedMap<K, V> sorted(Map<K, V> map) {
		if (map instanceof SortedMap<?, ?>) {
			return map as SortedMap<K, V>
		}
		val result = Maps::<K, V>newTreeMap
		if (map !== null) {
			result.putAll(map)
		}
		return result
	}

	def <K, V> SortedMap<K, V> sortedWith(Map<K, V> map, Comparator<K> comparator) {
		if (map instanceof SortedMap<?, ?>) {
			return map as SortedMap<K, V>
		}
		val result = Maps::<K, K, V>newTreeMap(comparator)
		if (map !== null) {
			result.putAll(map)
		}
		return result
	}
	
	def <T> Iterable<T> andAlso(Iterable<T> list, Iterable<T> another) {
		if (list === null || list.empty) {
			return another.safe
		}
		if (another === null || another.empty) {
			return list
		}
		val result = new LinkedList<T>()
		result.addAll(list)
		result.addAll(another)
		return result
	}
 
 	def <T> String joinedMap(Iterable<? extends T> list, String separator, Functions.Function2<T, Integer, String> op) {
		if (list === null || list.empty) {
			return ''
		}
		val int[] idxRef = #[0]
		list.map(
			[
				val idx = idxRef.get(0)
				idxRef.set(0, idx + 1)
				op.apply(it, idx)
			]).join(separator)
	}
	
	def <T> excludeType(Iterable<T> list, Class<?> excluded) {
		return list.filter[it | !excluded.isInstance(it)]
	}
	
	def <T> excludeTypes(Iterable<T> list, Class<?> ...excluded) {
		return excluded.fold(list, [l, c | l.excludeType(c)])
	}
	
	def String spaceIfNeeded(String prefix, String text) {
		if (prefix.nullOrEmpty) text else prefix + ' ' + text 
	}

}
