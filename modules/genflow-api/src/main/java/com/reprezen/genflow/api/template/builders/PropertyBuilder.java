package com.reprezen.genflow.api.template.builders;

import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.template.GenTemplateProperty.StandardSubProperties;

public class PropertyBuilder extends NamedBuilderBase<PropertyBuilder> {
	private Map<String, String> subProperties = Maps.newHashMap();

	public PropertyBuilder named(Enum<?> name) {
		return named(name.name());
	}

	public PropertyBuilder withValue(String value) {
		return withSubProperty(StandardSubProperties.VALUE, value);
	}

	public PropertyBuilder withDescription(String description) {
		return withSubProperty(StandardSubProperties.DESCRIPTION, description);
	}

	public PropertyBuilder withUiSymbol(String symbol) {
		return withSubProperty(StandardSubProperties.UI_SYMBOL, symbol);
	}

	public PropertyBuilder withUiColor(String color) {
		return withSubProperty(StandardSubProperties.UI_COLOR, color);
	}

	public PropertyBuilder withSubProperty(Enum<?> name, String value) {
		return withSubProperty(name.name(), value);
	}

	public PropertyBuilder withSubProperty(String name, String value) {
		subProperties.put(name, value);
		return this;
	}

	public GenTemplateProperty build() throws GenerationException {
		GenTemplateProperty prop = new GenTemplateProperty(name);
		for (String subPropName : subProperties.keySet()) {
			prop.setSubProperty(subPropName, subProperties.get(subPropName));
		}
		return prop;
	}

}
