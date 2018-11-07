package com.reprezen.genflow.api.template;

import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.GENERATOR;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.NAMED_SOURCE;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.PARAMETER;
import static com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType.PRIMARY_SOURCE;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType;

/**
 * Class that can supply all the metadata for a GenTemplate.
 *
 * @author Andy Lowry
 *
 */
public class GenTemplateInfo implements Comparable<GenTemplateInfo> {

	@JsonIgnore
	private IGenTemplate instance;
	private String id;
	private String name;
	private List<String> akaIds;
	private String className;
	private boolean suppressed;
	private SourceInfo primarySource;
	private Map<String, SourceInfo> namedSources = new LinkedHashMap<>();
	private Map<String, ParameterInfo> parameters = new LinkedHashMap<>();
	private Map<String, PrereqInfo> prereqs = new LinkedHashMap<>();
	@JsonIgnore // do this later
	private Map<String, GenTemplateProperty> properties;

	public GenTemplateInfo(IGenTemplate genTemplate) {
		this.instance = genTemplate;
		extractIdInfo(genTemplate);
		extractSourceInfo(genTemplate);
		extractParameterInfo(genTemplate);
		extractPrereqInfo(genTemplate);
		extractProperties(genTemplate);
		this.suppressed = genTemplate.isSuppressed();
	};

	@JsonCreator
	private GenTemplateInfo() {
	}

	public IGenTemplate getInstance() throws GenerationException {
		if (instance == null) {
			try {
				// TODO this won't work because some GenTemplates don't have no-art constructors
				// (e.g. SCG and OAG
				// instances, which need their modelInfo). So for now we need to avoid this by
				// always keeping the
				// discovered instance. This will prevent de/serialization, so we're OK until we
				// want to rely on that.
				instance = (IGenTemplate) this.getClass().getClassLoader().loadClass(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new GenerationException("Failed to instantiate GenTemplate class " + className, e);
			}
		}
		return instance != null ? instance.newInstance() : null;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<String> getAkaIds() {
		return Collections.unmodifiableList(akaIds);
	}

	public String getClassName() {
		return className;
	}

	public boolean isSuppressed() {
		return suppressed;
	}

	public Optional<SourceInfo> getPrimarySource() {
		return Optional.ofNullable(primarySource);
	}

	public Optional<Class<?>> getPrimarySourceType() {
		return getPrimarySource().map(si -> si.getValueType());
	}

	public boolean isSuppressedForValueType(Class<?> valueType) {
		try {
			return getInstance().isSuppressed(valueType);
		} catch (GenerationException e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<String, SourceInfo> getNamedSources() {
		return Collections.unmodifiableMap(namedSources);
	}

	public Optional<SourceInfo> getNamedSource(String name) {
		return Optional.ofNullable(namedSources.get(name));
	}

	public Optional<Class<?>> getNamedSourceType(String name) {
		return getNamedSource(name).map(si -> si.getValueType());
	}

	public Map<String, ParameterInfo> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public Optional<ParameterInfo> getParameter(String name) {
		return Optional.ofNullable(parameters.get(name));
	}

	public Map<String, PrereqInfo> getPrereqs() {
		return Collections.unmodifiableMap(prereqs);
	}

	public Optional<PrereqInfo> getPrereq(String name) {
		return Optional.ofNullable(prereqs.get(name));
	}

	public Map<String, GenTemplateProperty> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public Optional<GenTemplateProperty> getProperty(String name) {
		return Optional.ofNullable(properties.get(name));
	}

	public Optional<GenTemplateProperty> getProperty(Enum<?> nameEnum) {
		return getProperty(nameEnum.name());
	}

	private void extractIdInfo(IGenTemplate genTemplate) {
		this.id = genTemplate.getId();
		this.name = genTemplate.getName();
		if (genTemplate instanceof AbstractGenTemplate) {
			try {
				this.akaIds = ((AbstractGenTemplate) genTemplate).getAlsoKnownAsIds();
			} catch (GenerationException e) {
				e.printStackTrace();
				this.akaIds = Collections.emptyList();
			}
		}
		this.className = genTemplate.getClass().getName();
	}

	private void extractSourceInfo(IGenTemplate genTemplate) {
		Optional<GenTemplateDependency> primary = getDependencies(genTemplate, PRIMARY_SOURCE).findFirst();
		primary.ifPresent(d -> {
			this.primarySource = new SourceInfo(d);
		});
		for (GenTemplateDependency d : getDependencies(genTemplate, NAMED_SOURCE).collect(toList())) {
			this.namedSources.put(d.getName(), new SourceInfo(d));
		}
	}

	private void extractParameterInfo(IGenTemplate genTemplate) {
		for (GenTemplateDependency d : getDependencies(genTemplate, PARAMETER).collect(toList())) {
			this.parameters.put(d.getName(), new ParameterInfo(d));
		}
	}

	private void extractPrereqInfo(IGenTemplate genTemplate) {
		for (GenTemplateDependency d : getDependencies(genTemplate, GENERATOR).collect(toList())) {
			this.prereqs.put(d.getName(), new PrereqInfo(d));
		}
	}

	private void extractProperties(IGenTemplate genTemplate) {
		try {
			this.properties = genTemplate.getProperties();
		} catch (GenerationException e) {
			e.printStackTrace();
			this.properties = Collections.emptyMap();
		}
	}

	private Stream<GenTemplateDependency> getDependencies(IGenTemplate genTemplate, GenTemplateDependencyType type) {
		try {
			return genTemplate.getDependencies().stream().filter(d -> d.getType() == type);
		} catch (GenerationException e) {
			e.printStackTrace();
			return Stream.of();
		}
	}

	@Override
	public int compareTo(GenTemplateInfo o) {
		return name.toLowerCase().compareTo(o.name.toLowerCase());
	}

	public static class SourceInfo {
		private String name;
		private String valueClassName;
		private String description;
		private boolean required;
		private boolean primary;

		public SourceInfo(GenTemplateDependency d) {
			this.name = d.getName();
			this.valueClassName = d.getInfo();
			this.description = d.getDescription();
			this.required = d.isRequired();
			this.primary = d.getType() == PRIMARY_SOURCE;
		}

		@JsonCreator
		private SourceInfo() {
		}

		public String getName() {
			return name;
		}

		public String getValueClassName() {
			return valueClassName;
		}

		public Class<?> getValueType() {
			if (valueClassName != null) {
				try {
					return this.getClass().getClassLoader().loadClass(valueClassName);
				} catch (ClassNotFoundException e) {
				}
			}
			return null;
		}

		public String getDescription() {
			return description;
		}

		public boolean isRequired() {
			return required;
		}

		public boolean isPrimary() {
			return primary;
		}
	}

	public static class ParameterInfo {
		private String name;
		private String description;
		private String defaultValue;
		private boolean required;

		public ParameterInfo(GenTemplateDependency d) {
			this.name = d.getName();
			this.description = d.getDescription();
			this.defaultValue = d.getInfo();
			this.required = d.isRequired();
		}

		@JsonCreator
		private ParameterInfo() {
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

		public boolean isRequired() {
			return required;
		}
	}

	public static class PrereqInfo {
		private String name;
		private String description;
		private String id;
		private boolean required;

		public PrereqInfo(GenTemplateDependency d) {
			this.name = d.getName();
			this.description = d.getDescription();
			this.id = d.getInfo();
			this.required = d.isRequired();
		}

		@JsonCreator
		private PrereqInfo() {
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getId() {
			return id;
		}

		public boolean isRequired() {
			return required;
		}
	}

	@Override
	public String toString() {
		return String.format("GenTemplateInfo[id: %s, name: %s, primType: %s]", getId(), getName(),
				getPrimarySource().map(s -> s.getValueClassName()).orElse("none"));
	}
}
