package com.reprezen.genflow.common.codegen;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Parameter;

import io.swagger.codegen.CodegenConfig;

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

	enum GenericType {
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

	public static class ScgModuleWrapper extends GenModuleWrapper<io.swagger.codegen.CodegenConfig> {

		private Logger logger = LoggerFactory.getLogger(ScgModuleWrapper.class);

		public ScgModuleWrapper(io.swagger.codegen.CodegenConfig config) {
			super(config);
		}

		public ScgModuleWrapper(Class<CodegenConfig> configClass) {
			super(configClass);
		}

		public static GenModuleWrapper<io.swagger.codegen.CodegenConfig> getDummyInstance() {
			return new ScgModuleWrapper(io.swagger.codegen.CodegenConfig.class);
		}

		@Override
		public Enum<?> getType() {
			return config.getTag();
		}

		@Override
		public GenericType getGenericType() {
			switch (config.getTag()) {
			case CLIENT:
				return GenericType.CLIENT;
			case SERVER:
				return GenericType.SERVER;
			case DOCUMENTATION:
				return GenericType.DOCUMENTATION;
			case CONFIG:
				return GenericType.CONFIG;
			case OTHER:
			default:
				return GenericType.OTHER;
			}
		}

		@Override
		public String getName() {
			return config.getName();
		}

		@Override
		public List<CliOptWrapper<?>> getClientOptions() {
			return config.cliOptions().stream().map(cliOpt -> new ScgCliOptWrapper(cliOpt))
					.collect(Collectors.toList());
		}

		@Override
		public Logger getLogger() {
			return logger;
		}

		@Override
		public Enum<?> typeNamed(String name) {
			return io.swagger.codegen.CodegenType.valueOf(name);
		}

	}

	public static class OagModuleWrapper extends GenModuleWrapper<org.openapitools.codegen.CodegenConfig> {
		private Logger logger = LoggerFactory.getLogger(ScgModuleWrapper.class);

		private org.openapitools.codegen.CodegenConfig config;

		public OagModuleWrapper(org.openapitools.codegen.CodegenConfig config) {
			super(config);
		}

		public OagModuleWrapper(Class<org.openapitools.codegen.CodegenConfig> configClass) {
			super(configClass);
		}

		public static GenModuleWrapper<org.openapitools.codegen.CodegenConfig> getDummyInstance() {
			return new OagModuleWrapper(org.openapitools.codegen.CodegenConfig.class);
		}

		@Override
		public Enum<?> getType() {
			return config.getTag();
		}

		@Override
		public GenericType getGenericType() {
			switch (config.getTag()) {
			case CLIENT:
				return GenericType.CLIENT;
			case SERVER:
				return GenericType.SERVER;
			case DOCUMENTATION:
				return GenericType.DOCUMENTATION;
			case CONFIG:
				return GenericType.CONFIG;
			case OTHER:
			default:
				return GenericType.OTHER;
			}
		}

		@Override
		public String getName() {
			return config.getName();
		}

		@Override
		public List<CliOptWrapper<?>> getClientOptions() {
			return config.cliOptions().stream().map(cliOpt -> new OagCliOptWrapper(cliOpt))
					.collect(Collectors.toList());
		}

		@Override
		public Enum<?> typeNamed(String name) {
			return org.openapitools.codegen.CodegenType.valueOf(name);
		}

		@Override
		public Logger getLogger() {
			return logger;
		}
	}

	public static abstract class CliOptWrapper<CliOpt> {

		protected CliOpt cliOpt;

		public CliOptWrapper(CliOpt cliOpt) {
			this.cliOpt = cliOpt;
		}

		public abstract String getName();

		public abstract String getDescription();

	}

	public static class ScgCliOptWrapper extends CliOptWrapper<io.swagger.codegen.CliOption> {

		public ScgCliOptWrapper(io.swagger.codegen.CliOption cliOpt) {
			super(cliOpt);
		}

		@Override
		public String getName() {
			return cliOpt.getOpt();
		}

		@Override
		public String getDescription() {
			return cliOpt.getDescription();
		}

	}

	public static class OagCliOptWrapper extends CliOptWrapper<org.openapitools.codegen.CliOption> {

		public OagCliOptWrapper(org.openapitools.codegen.CliOption cliOpt) {
			super(cliOpt);
		}

		@Override
		public String getName() {
			return cliOpt.getOpt();
		}

		@Override
		public String getDescription() {
			return cliOpt.getDescription();
		}

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
