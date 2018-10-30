package com.reprezen.genflow.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.reprezen.genflow.api.template.IGenTemplateContext;

public class HtmlInjections {

	@JsonInclude
	private Map<String, String> injections = new HashMap<>();

	public HtmlInjections(Injection... injections) {
		for (Injection injection : injections) {
			addInjection(injection);
		}
	}

	public void addInjection(Injection injection) {
		addInjection(injection.getName(), injection.getHtmlSnippet());
	}

	public void addInjection(String name, String htmlSnippet) {
		if (injections.containsKey(name)) {
			injections.put(name, injections.get(name) + "\n" + htmlSnippet);
		} else {
			injections.put(name, htmlSnippet);
		}
	}

	public static final String HEAD_TOP = "HEAD_TOP";
	public static final String HEAD_BOTTOM = "HEAD_BOTTOM";
	public static final String BODY_TOP = "BODY_TOP";
	public static final String BODY_BOTTOM = "BODY_BOTTOM";

	public static final String HTML_INJECTIONS_PARAM = "HTML_INJECTIONS";

	public static HtmlInjections fromContext(IGenTemplateContext context) {
		Object obj = context.getGenTargetParameters().get(HTML_INJECTIONS_PARAM);
		if (obj == null) {
			return new HtmlInjections();
		} else if (obj instanceof HtmlInjections) {
			return (HtmlInjections) obj;
		} else if (obj instanceof Map<?, ?>) {
			HtmlInjections injections = new HtmlInjections();
			for (Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
				try {
					String name = (String) entry.getKey();
					String htmlSnippet = (String) entry.getValue();
					injections.addInjection(name, htmlSnippet);
				} catch (ClassCastException e) {
					String msg = String.format("Invalid entry in Html Injections map: %s => %s", entry.getKey(),
							entry.getValue());
					throw new IllegalArgumentException(msg, e);
				}
			}
			return injections;
		} else {
			String msg = String.format("Invalid value for parameter '%': %s", HTML_INJECTIONS_PARAM, obj);
			throw new IllegalArgumentException(msg);
		}
	}

	public String inject(String name) {
		return getInjection(name).orElse("");
	}

	public Optional<String> getInjection(String name) {
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
		@JsonInclude
		private final String name;
		@JsonInclude
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
