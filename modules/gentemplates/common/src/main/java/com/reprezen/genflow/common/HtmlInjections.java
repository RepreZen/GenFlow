package com.reprezen.genflow.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HtmlInjections {

	private Map<String, String> injections = new HashMap<>();

	public HtmlInjections(Injection... injections) {
		for (Injection injection : injections) {
			this.injections.put(injection.getName(), injection.getHtmlSnippet());
		}
	}

	public static final String HEAD_TOP = "HEAD_TOP";
	public static final String HEAD_BOTTOM = "HEAD_BOTTOM";
	public static final String BODY_TOP = "BODY_TOP";
	public static final String BODY_BOTTOM = "BODY_BOTTOM";

	public static final String HTML_INJECTIONS_PARAM = "HTML_INJECTIONS";

	public Optional<String> inject(String name) {
		return Optional.ofNullable(injections.get(name));
	}

	public static Injection headTop(String... htmlSnippets) {
		return new Injection(HEAD_TOP, htmlSnippets);
	}

	public static Injection headBottom(String... htmlSnippets) {
		return new Injection(HEAD_BOTTOM, htmlSnippets);
	}

	public static Injection bodyTop(String... htmlSnippets) {
		return new Injection(BODY_TOP, htmlSnippets);
	}

	public static Injection bodyBottom(String... htmlSnippets) {
		return new Injection(BODY_BOTTOM, htmlSnippets);
	}

	public static class Injection {
		private final String name;
		private final String htmlSnippet;

		public Injection(String name, String... htmlSnippet) {
			super();
			this.name = name;
			this.htmlSnippet = Stream.of(htmlSnippet).collect(Collectors.joining("\n"));

		}

		public String getName() {
			return name;
		}

		public String getHtmlSnippet() {
			return htmlSnippet;
		}

	}
}
