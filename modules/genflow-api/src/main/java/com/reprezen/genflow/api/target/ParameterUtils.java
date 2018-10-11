package com.reprezen.genflow.api.target;

import java.util.Optional;

import com.reprezen.genflow.api.template.IGenTemplateContext;

public class ParameterUtils {

	private final IGenTemplateContext context;

	public ParameterUtils(IGenTemplateContext context) {
		this.context = context;
	}

	public Optional<Parameter> getParameter(String name) {
		Object value = context.getGenTargetParameters().get(name);
		return Optional.ofNullable(Parameter.of(value));
	}

	public static class Parameter {
		private final Object value;

		private Parameter(Object value) {
			this.value = value;
		}

		public static Parameter of(Object value) {
			return value != null ? new Parameter(value) : null;
		}

		public String asString() {
			return asString(false);
		}

		public String asString(boolean canConvert) {
			if (value instanceof String) {
				return (String) value;
			} else if (canConvert) {
				return value.toString();
			}
			return null;
		}

		public Boolean asBoolean() {
			return asBoolean(true);
		}

		public Boolean asBoolean(boolean canConvert) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (canConvert) {
				if (value instanceof String) {
					return Boolean.valueOf((String) value);
				}
			}
			return null;
		}

		public Object asObject() {
			return value;
		}
	}
}
