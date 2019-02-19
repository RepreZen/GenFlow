package com.reprezen.genflow.api.template;

import static com.reprezen.genflow.api.template.GenTemplateProperty.StandardProperties.PROVIDER;

import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.template.builders.PropertyBuilder;

public class GenTemplateProperty {

	private String name;
	private Map<String, String> subProperties = Maps.newHashMap();

	public GenTemplateProperty(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return getSubProperty(StandardSubProperties.VALUE);
	}

	public String getDescription() {
		return getSubProperty(StandardSubProperties.DESCRIPTION);
	}

	public String getUiSymbol() {
		return getSubProperty(StandardSubProperties.UI_SYMBOL);
	}

	public String getUiColor() {
		return getSubProperty(StandardSubProperties.UI_COLOR);
	}

	public void setSubProperty(String name, String value) {
		subProperties.put(name, value);
	}

	public String getSubProperty(String name) {
		return subProperties.get(name);
	}

	public String getSubProperty(Enum<?> name) {
		return getSubProperty(name.name());
	}

	public enum StandardProperties {
		PROVIDER, DESCRIPTION, GENERATOR_TYPE
	}

	public enum StandardSubProperties {
		VALUE, DESCRIPTION, UI_SYMBOL, UI_COLOR
	}

	public static PropertyBuilder reprezenProvider() {
		return new PropertyBuilder().named(PROVIDER) //
				.withValue("RepreZen") //
				.withUiSymbol("R").withUiColor("195,77,39");
	}

	public static PropertyBuilder swaggerCodegenProvider() {
		return new PropertyBuilder().named(PROVIDER) //
				.withValue("Swagger Codegen") //
				.withUiSymbol("S").withUiColor("109,154,0");
	}

	public static PropertyBuilder swaggerCodegenV3Provider() {
		return new PropertyBuilder().named(PROVIDER) //
				.withValue("Swagger Codegen v3") //
				.withUiSymbol("S").withUiColor("109,154,0");
	}

	public static PropertyBuilder openApiGeneratorProvider() {
		return new PropertyBuilder().named(PROVIDER) //
				.withValue("OpenAPI Generator") //
				.withUiSymbol("O").withUiColor("86,99,58");
	}

	public static PropertyBuilder nswagProvider() {
		return new PropertyBuilder().named(PROVIDER) //
				.withValue("NSwag") //
				.withUiSymbol("N").withUiColor("46,117,181");
	}
}