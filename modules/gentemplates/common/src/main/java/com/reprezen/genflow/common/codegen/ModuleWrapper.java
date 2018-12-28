package com.reprezen.genflow.common.codegen;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.common.codegen.GenModulesInfo.Parameter;

public interface ModuleWrapper {

	public String getClassName();

	public String getClassSimpleName();

	public Enum<?> getType();

	public GenericType getGenericType();

	public String getName();

	public default String getDerivedName() {
		String name = getClassSimpleName();
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

	public default List<Parameter> getParameters() {
		List<Parameter> params = Lists.newArrayList();
		Set<String> paramNames = Sets.newHashSet();
		for (CliOptWrapper option : getClientOptions()) {
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

	public List<CliOptWrapper> getClientOptions();

	public Enum<?> typeNamed(String name);

	public Logger getLogger();

	public static class ScgModuleWrapper implements ModuleWrapper {
		private Logger logger = LoggerFactory.getLogger(ScgModuleWrapper.class);

		private io.swagger.codegen.CodegenConfig config;

		public ScgModuleWrapper(io.swagger.codegen.CodegenConfig config) {
			this.config = config;
		}

		public static ModuleWrapper getDummyInstance() {
			return new ScgModuleWrapper(null);
		}

		@Override
		public String getClassName() {
			return config.getClass().getName();
		}

		@Override
		public String getClassSimpleName() {
			return config.getClass().getSimpleName();
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
		public List<CliOptWrapper> getClientOptions() {
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

	public static class OagModuleWrapper implements ModuleWrapper {
		private Logger logger = LoggerFactory.getLogger(ScgModuleWrapper.class);

		private org.openapitools.codegen.CodegenConfig config;

		public OagModuleWrapper(org.openapitools.codegen.CodegenConfig config) {
			this.config = config;
		}

		public static ModuleWrapper getDummyInstance() {
			return new OagModuleWrapper(null);
		}

		@Override
		public String getClassName() {
			return config.getClass().getName();
		}

		@Override
		public String getClassSimpleName() {
			return config.getClass().getSimpleName();
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
		public List<CliOptWrapper> getClientOptions() {
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

	public static interface CliOptWrapper {

		public String getName();

		public String getDescription();

	}

	public static class ScgCliOptWrapper implements CliOptWrapper {
		private io.swagger.codegen.CliOption cliOpt;

		public ScgCliOptWrapper(io.swagger.codegen.CliOption cliOpt) {
			this.cliOpt = cliOpt;
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

	public static class OagCliOptWrapper implements CliOptWrapper {
		private org.openapitools.codegen.CliOption cliOpt;

		public OagCliOptWrapper(org.openapitools.codegen.CliOption cliOpt) {
			this.cliOpt = cliOpt;
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
