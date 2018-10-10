package com.reprezen.genflow.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

	public Optional<String> inject(String name) {
		return Optional.ofNullable(injections.get(name));
	}

	public static Injection headTop(String htmlSnippet) {
		return new Injection(HEAD_TOP, htmlSnippet);
	}

	public static Injection headBottom(String htmlSnippet) {
		return new Injection(HEAD_BOTTOM, htmlSnippet);
	}

	public static Injection bodyTop(String htmlSnippet) {
		return new Injection(BODY_TOP, htmlSnippet);
	}

	public static Injection bodyBottom(String htmlSnippet) {
		return new Injection(BODY_BOTTOM, htmlSnippet);
	}

	public static class Injection {
		private final String name;
		private final String htmlSnippet;

		public Injection(String name, String htmlSnippet) {
			super();
			this.name = name;
			this.htmlSnippet = htmlSnippet;
		}

		public String getName() {
			return name;
		}

		public String getHtmlSnippet() {
			return htmlSnippet;
		}

	}
}
