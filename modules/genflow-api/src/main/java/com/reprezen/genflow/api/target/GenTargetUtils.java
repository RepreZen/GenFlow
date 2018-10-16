/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.target;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.template.GenTemplateProperty.StandardProperties;
import com.reprezen.genflow.api.template.GenTemplateRegistry;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceSerializer;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

/**
 * Utility methods to load and save {@link GenTarget}
 * 
 * @author Konstantin Zaitsev
 * @date Jun 22, 2015
 */
public final class GenTargetUtils {
	private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory()).enable(Feature.ALLOW_COMMENTS);

	public GenTargetUtils() {
		// prevent instantiation
	}

	public static GenTarget load(File genTargetFile) throws IncorrectGenTargetException {
		try {
			GenTarget target = mapper.readValue(genTargetFile, GenTarget.class);
			target.setBaseDir(genTargetFile.getCanonicalFile().getParentFile());
			return target;
		} catch (Exception e) {
			throw new IncorrectGenTargetException(
					"GenTarget file has an invalid format, property names or values: " + genTargetFile.toString(), e);
		}
	}

	public static void save(GenTarget target, String name, boolean force) throws GenerationException {
		File file = getGenTargetFile(name, target.getBaseDir());
		try {
			if (!file.exists() || force) {
				mapper.writeValue(file, target);
			}
		} catch (IOException e) {
			throw new GenerationException("Failed to save GenTarget " + name + " to " + file, e);
		}
	}

	public static void saveTrace(GenTarget target, GenTemplateTrace trace) throws GenerationException {
		File file = getTraceFile(target);
		GenTemplateTraceSerializer.save(trace, file);
	}

	public static GenTemplateTraces execute(Logger logger, boolean log, boolean withPrerequisites,
			GenTemplateTraces allTraces, Collection<GenTarget> targets) throws GenerationException {
		if (allTraces == null) {
			allTraces = new GenTemplateTraces();
		}
		if (withPrerequisites) {
			targets = resolveTargetList(targets);
		}
		for (GenTarget target : targets) {
			if (log) {
				logger.info(String.format("Executing GenTarget %s [%s]", target.getName(), getProvider(target)));
			}
			GenTemplateTrace trace = target.execute(allTraces, logger);
			if (log) {
				logger.info("Saving trace data");
			}
			saveTrace(target, trace);
			allTraces.addTrace(target, trace);
		}
		return allTraces;
	}

	public static GenTemplateTraces execute(Logger logger, boolean log, boolean withPrerequisites,
			GenTemplateTraces allTraces, GenTarget... targets) throws GenerationException {
		return execute(logger, log, withPrerequisites, allTraces, Arrays.asList(targets));
	}

	public static List<GenTarget> resolveTargetList(Collection<GenTarget> targets) throws GenerationException {
		Queue<GenTarget> todo = new ArrayDeque<GenTarget>(targets);
		Multimap<GenTarget, GenTarget> graph = HashMultimap.<GenTarget, GenTarget>create();
		Set<GenTarget> allTargets = Sets.newHashSet();
		while (!todo.isEmpty()) {
			GenTarget target = todo.remove();
			if (allTargets.contains(target)) {
				continue;
			}
			allTargets.add(target);
			for (GenTargetPrerequisite prereq : target.getPrerequisites().values()) {
				GenTarget prereqTarget = load(target.resolvePath(prereq.getGenFilePath()));
				if (!allTargets.contains(prereqTarget)) {
					todo.add(prereqTarget);
				}
				graph.put(target, prereqTarget);
			}
		}
		// Reverse because graph has target -> prereq edges, and we want target to
		// follow prereq
		return TopSort.sortReverse(allTargets, graph);
	}

	public static File getGenTargetFile(String name, File baseDir) {
		return new File(baseDir, name + ".gen");
	}

	public static File getGenTargetFile(GenTarget target) {
		return getGenTargetFile(target.getName(), target.getBaseDir());
	}

	public static File getTraceFile(GenTarget target) {
		return new File(target.getBaseDir(), target.getName() + ".trace.json");
	}

	private static String getProvider(GenTarget target) {
		IGenTemplate template = GenTemplateRegistry.getGenTemplate(target.getGenTemplateId());
		String unknown = "unknown provider";
		if (template == null) {
			return unknown;
		} else {
			GenTemplateProperty provider = null;
			try {
				provider = template.getProperty(StandardProperties.PROVIDER.name());
			} catch (GenerationException e) {
			}
			return provider != null ? provider.getValue() : unknown;
		}
	}

	// TOOD This should replace the top sorter in cmr.gentemplates.common.graph,
	// getting rid of all the existing
	// digraph stuff that's not otherwise needed
	public static class TopSort {
		public static <T> List<T> sortReverse(Collection<T> nodes, Multimap<T, T> graph) {
			List<T> result = Lists.newArrayList();
			Set<T> visited = Sets.newHashSet();
			for (T node : nodes) {
				visit(node, graph, result, visited, new HashSet<T>());
			}
			return result;
		}

		public static <T> List<T> sort(Collection<T> nodes, Multimap<T, T> graph) {
			// sortReverse is actually the more efficient option, since nodes are placed
			// after all their graph
			// descendants have been placed, and the only core List implementation with
			// efficient front-loading is the
			// generally inefficient LinkedList.
			List<T> result = sortReverse(nodes, graph);
			Collections.reverse(result);
			return result;
		}

		private static <T> void visit(T node, Multimap<T, T> graph, List<T> result, Set<T> visited, Set<T> inProgress) {
			if (inProgress.contains(node)) {
				throw new IllegalArgumentException("Graph contains at least one cycle");
			} else if (!visited.contains(node)) {
				inProgress.add(node);
				for (T dependant : graph.get(node)) {
					visit(dependant, graph, result, visited, inProgress);
				}
				visited.add(node);
				inProgress.remove(node);
				result.add(node);
			}
		}
	}
}
