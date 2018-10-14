/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.trace;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Iterables.any;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * This class contains a number of utility methods for locating trace items in a
 * trace.
 * 
 * @author Konstantin Zaitsev
 * @date Jun 16, 2015
 */
public final class GenTemplateTraceUtils {

	/**
	 * Find trace items of a given type and containing at least one source item with
	 * a given role and locator.
	 * 
	 * @param trace         trace object whose items are searched
	 * @param traceItemType item type trace item type
	 * @param sourceRole    source role source item role
	 * @param sourceLocator source locator source locator
	 * @return matching trace items
	 */
	public static List<GenTemplateTraceItem> getTraceItems(GenTemplateTrace trace, String traceItemType,
			String sourceRole, String sourceLocator) {
		List<GenTemplateTraceItem> items = new ArrayList<>();
		for (GenTemplateTraceItem item : trace.getTraceItems()) {
			if (item.getType().equals(traceItemType)
					&& any(item.getSources(), getSourcePredicate(sourceRole, sourceLocator))) {
				items.add(item);
			}
		}
		return items;
	}

	/**
	 * Find trace items of a given type.
	 * 
	 * @param trace         trace object whose items are searched
	 * @param traceItemType trace item type
	 * @return matching trace items
	 */
	public static List<GenTemplateTraceItem> getTraceItemsOfType(GenTemplateTrace trace, String traceItemType) {
		return Lists.newArrayList(filter(trace.getTraceItems(), hasType(traceItemType)));
	}

	/**
	 * Find the single trace item with a given type and at least one source item
	 * with a given source role and locator.
	 * 
	 * @param trace         trace object whose items are searched
	 * @param traceItemType trace item type
	 * @param sourceRole    source item role
	 * @param sourceLocator source locator
	 * @return matching trace item, or <code>null</code> if no item matches
	 * @throws IllegalArgumentException if multiple matching trace items are found
	 */
	public static GenTemplateTraceItem getTraceItem(GenTemplateTrace trace, String traceItemType, String sourceRole,
			String sourceLocator) {
		try {
			return Iterables.getOnlyElement(getTraceItems(trace, traceItemType, sourceRole, sourceLocator));
		} catch (NoSuchElementException e) {
			return null;
		}
	}

	/**
	 * Predicate to test if a source item has a given role and locator
	 * 
	 * @param sourceRole    source item role
	 * @param sourceLocator locator
	 * @return predicate
	 */
	public static Predicate<GenTemplateTraceSourceItem> getSourcePredicate(final String sourceRole,
			final String sourceLocator) {
		return new Predicate<GenTemplateTraceSourceItem>() {
			@Override
			public boolean apply(GenTemplateTraceSourceItem input) {
				return sourceRole.equals(input.role) && sourceLocator.equals(input.locator);
			}
		};
	}

	/**
	 * Predicate to test if a trace item has a given type
	 * 
	 * @param traceItemType trace item type
	 * @return predicate
	 */
	public static Predicate<GenTemplateTraceItem> hasType(final String traceItemType) {
		return new Predicate<GenTemplateTraceItem>() {

			@Override
			public boolean apply(GenTemplateTraceItem item) {
				return item.getType().equals(traceItemType);
			}
		};
	}

	/**
	 * Test if a trace item has a given output file.
	 * 
	 * @param file output file
	 * @return true if the trace item matches
	 */
	public static Predicate<GenTemplateTraceItem> hasFile(final File file) {
		return new Predicate<GenTemplateTraceItem>() {

			@Override
			public boolean apply(GenTemplateTraceItem item) {
				return item.getOutputFile().equals(file);
			}
		};
	}

	/**
	 * Test if a source item has a given role.
	 * 
	 * @param sourceRole source role
	 * @return true if the source item matches
	 */
	public static Predicate<GenTemplateTraceSourceItem> hasSourceRole(final String sourceRole) {
		return new Predicate<GenTemplateTraceSourceItem>() {
			@Override
			public boolean apply(GenTemplateTraceSourceItem input) {
				return sourceRole.equals(input.role);
			}
		};
	}

	/**
	 * Test if a trace item has at least one source item with a given role.
	 * 
	 * @param item       trace item
	 * @param sourceRole source role
	 * @return true if the trace item matches
	 */
	public static boolean hasSourceRole(GenTemplateTraceItem item, final String sourceRole) {
		return hasSourceItemMeetingCondition(item, hasSourceRole(sourceRole));
	}

	/**
	 * Predicate that tests whether source item has a given locator
	 * 
	 * @param sourceLocator locator
	 * @return predicate
	 */
	public static Predicate<GenTemplateTraceSourceItem> hasSourceLocator(final String sourceLocator) {
		return new Predicate<GenTemplateTraceSourceItem>() {
			@Override
			public boolean apply(GenTemplateTraceSourceItem input) {
				return sourceLocator.equals(input.locator);
			}
		};
	}

	/**
	 * Test whether a trace item has a source item with a given role.
	 * 
	 * @param item       trace item
	 * @param sourceRole source role
	 * @return true if the trace item matches
	 */
	public static boolean hasSourceLocator(GenTemplateTraceItem item, final String sourceRole) {
		return hasSourceItemMeetingCondition(item, hasSourceLocator(sourceRole));
	}

	/**
	 * Test whether a trace item has a source item satisfying a given predicate.
	 * 
	 * @param item      trace tiem
	 * @param condition predicate
	 * @return true if the trace item matches
	 */
	public static boolean hasSourceItemMeetingCondition(GenTemplateTraceItem item,
			Predicate<GenTemplateTraceSourceItem> condition) {
		return any(item.getSources(), condition);
	}
}
