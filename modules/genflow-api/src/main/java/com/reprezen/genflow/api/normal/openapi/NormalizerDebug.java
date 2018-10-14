package com.reprezen.genflow.api.normal.openapi;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Print debugging output depending on options named in
 * REPREZEN_DEBUG_NORMALIZER environment variable.
 * <p>
 * The env var can be set to a comma-separated list of names of enums in the
 * Option enum. Debug output tagged to the corresponding options - and output
 * not tagged to any output - is thereby enabled.
 * <p>
 * Alternatively the env var can be set to a non-empty value that does not
 * actually name any options (e.g. "true" or "on" or "all") to enable all
 * options.
 */
public class NormalizerDebug {
	private final static String DEBUG_VAR = "REPREZEN_DEBUG_NORMALIZER";
	private static ObjectMapper mapper = Yaml.mapper();
	static {
		mapper.setSerializationInclusion(Include.NON_NULL);
		mapper.setSerializationInclusion(Include.NON_EMPTY);
	}
	private static Set<Option> activeOptions = Sets.newHashSet();
	private static boolean debugAll = false;
	static {
		String optNames = System.getenv(DEBUG_VAR);
		if (optNames != null) {
			for (String optName : optNames.split(",")) {
				Option option = Option.named(optName.trim());
				if (option != null) {
					activeOptions.add(option);
				}
			}
			if (activeOptions.isEmpty()) {
				debugAll = true;
			} else {
				activeOptions.add(Option.DEFAULT);
			}
		}
	}

	/**
	 * Output base-level debug output, enabled whenever at least one named option is
	 * enabled
	 */
	public static void debug(Object... args) {
		if (activeOptions.contains(Option.DEFAULT) || debugAll) {
			doDebug("*", args);
		}
	}

	/**
	 * Output tagged to a named option
	 */
	public static void debug(Option option, Object... args) {
		if (activeOptions.contains(option) || debugAll) {
			doDebug(option.name(), args);
		}
	}

	private static void doDebug(String optName, Object... args) {
		System.err.printf("NORMALIZER [%s] ==============================================================\n", optName);
		for (Object arg : args) {
			print(arg);
		}
	}

	private static void print(Object obj) {
		if (obj == null) {
			System.err.println("null");
		} else if (obj instanceof JsonNode) {
			printYaml(obj);
		} else if (obj instanceof Swagger) {
			printYaml(obj);
		} else if (obj instanceof OpenAPI) {
			printYaml(obj);
		} else if (obj instanceof Throwable) {
			printThrowable((Throwable) obj);
		} else if (obj.getClass().isArray()) {
			List<String> elts = Lists.newArrayList();
			for (Object elt : (Object[]) obj) {
				elts.add(elt.toString());
			}
			System.err.println("[" + StringUtils.join(elts, ", \n") + "]");
		} else {
			System.err.println(obj.toString());
		}
	}

	private static void printYaml(Object obj) {
		try {
			String content = mapper.writeValueAsString(obj);
			if (!content.endsWith("\n")) {
				content = content + "\n";
			}
			System.err.print(content);
		} catch (JsonProcessingException e) {
			System.err.println(obj.toString());
		}
	}

	private static void printThrowable(Throwable e) {
		System.err.println(e.toString());
		e.printStackTrace(System.err);
	}

	public enum Option {
		FINAL_SPEC, INLINED_SPEC, RESOLUTION, RESOLVED_CONTENT, PRE_PARSE_SPEC, POST_PARSE_SPEC, SIMPLE_REFS, RECURSION,
		DEFAULT;

		public static Option named(String name) {
			try {
				return Option.valueOf(name.toUpperCase());
			} catch (IllegalArgumentException e) {
				return null;
			}
		}
	}

}
