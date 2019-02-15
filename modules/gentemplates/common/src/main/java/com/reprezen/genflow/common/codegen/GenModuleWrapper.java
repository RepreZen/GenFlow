package com.reprezen.genflow.common.codegen;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Parameter;

public abstract class GenModuleWrapper<Config> {

	protected Config config;
	private Class<Config> configClass;

	public GenModuleWrapper(Config config) {
		this.config = config;
		@SuppressWarnings("unchecked")
		Class<Config> castClass = config != null ? (Class<Config>) config.getClass() : null;
		this.configClass = castClass;
	}

	public GenModuleWrapper(Class<Config> configClass) {
		this.config = null;
		this.configClass = configClass;
	}

	public Config getWrappedInstance() {
		return config;
	}

	public Class<Config> getWrappedClass() {
		return configClass;
	}

	public String getClassName() {
		return configClass.getName();
	}

	public String getSimpleName() {
		return configClass.getSimpleName();
	}

	public String getPackageName() {
		return configClass.getPackage().getName();
	}

	public boolean canWrap(Class<?> cls) {
		return configClass.isAssignableFrom(cls);
	}

	public ClassLoader getClassLoader() {
		return configClass.getClassLoader();
	}

	public String getLibraryVersion() {
		return configClass.getPackage().getImplementationVersion();
	}

	public Config newInstance() throws InstantiationException, IllegalAccessException {
		return configClass.newInstance();
	}

	public abstract Enum<?> getType();

	public abstract GenericType getGenericType();

	public abstract String getName();

	public String getDerivedName() {
		String name = getSimpleName();
		name = Util.trimFromEnd(name, "Generator");
		name = Util.trimFromEnd(name, "Codegen");

		switch (getGenericType()) {
		case CLIENT:
			name = Util.trimFromEnd(name, "Client") + " Client";
			break;
		case SERVER:
			name = Util.trimFromEnd(name, "Server") + " Server";
			break;
		case DOCUMENTATION:
			name = Util.trimFromEnd(name, "Doc", "Documentation") + " Documentation";
			break;
		case CONFIG:
			name = Util.trimFromEnd(name, "Config", "Configuration") + " Configuration";
			break;
		case OTHER:
		default:
			name = Util.trimFromEnd(name, "Client", "Server", "Doc", "Documentation", "Config", "Confiuration");
			break;
		}
		name = Util.camelToNatural(name);
		return name;
	}

	public enum GenericType {
		CLIENT, SERVER, DOCUMENTATION, CONFIG, OTHER
	};

	public List<Parameter> getParameters() {
		List<Parameter> params = Lists.newArrayList();
		Set<String> paramNames = Sets.newHashSet();
		for (CliOptWrapper<?> option : getClientOptions()) {
			if (paramNames.contains(option.getName())) {
				getLogger().warn("Duplicate parameter '{}' ignored for SCG module {}", option.getName(),
						getClassName());
			} else {
				Parameter param = new Parameter();
				param.setName(option.getName());
				param.setDescription(option.getDescription());
				param.setRequired(false);
				params.add(param);
				paramNames.add(option.getName());
			}
		}
		return params.size() > 0 ? params : null;
	}

	public abstract List<CliOptWrapper<?>> getClientOptions();

	public abstract Enum<?> typeNamed(String name);

	public abstract Logger getLogger();

	public static abstract class CliOptWrapper<CliOpt> {

		protected CliOpt cliOpt;

		public CliOptWrapper(CliOpt cliOpt) {
			this.cliOpt = cliOpt;
		}

		public abstract String getName();

		public abstract String getDescription();

	}

	static class Util {
		private static String trimFromEnd(String s, String... suffixes) {
			for (String suffix : suffixes) {
				s = s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
			}
			return s.trim();
		}

		private static String camelToNatural(String s) {
			return s.replaceAll("(\\p{Lower})(\\p{Upper})", "$1 $2");
		}
	}
}
