/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.core.runtime.FileLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenType;

public class SwaggerCodegenModulesInfo {
	private static Logger logger = LoggerFactory.getLogger(SwaggerCodegenDiscovery.class);

	private final String scgVersion;
	private final Map<String, Info> modulesInfo = Maps.newTreeMap();
	private final CSVFormat csvFormat = CSVFormat.RFC4180.withHeader();

	public static SwaggerCodegenModulesInfo load(String scgVersion, URL baseUrl)
			throws IOException, URISyntaxException {
		String bestVersion = null;
		for (String version : getAvailableVersions(baseUrl)) {
			if (versionCompare(version, scgVersion) <= 0) {
				bestVersion = version;
			} else {
				break;
			}
		}
		return bestVersion != null ? new SwaggerCodegenModulesInfo(bestVersion).load(baseUrl) : null;
	}

	public SwaggerCodegenModulesInfo(String scgVersion) {
		this.scgVersion = scgVersion;
	}

	private static List<String> availableVersions = null;

	private static List<String> getAvailableVersions(URL baseUrl) throws URISyntaxException, IOException {
		if (availableVersions == null) {
			switch (baseUrl.getProtocol().toLowerCase()) {
			case "file":
				getAvailableFileUrlVersions(baseUrl);
				break;
			case "jar":
				getAvailableJarUrlVersions(baseUrl);
				break;
			case "bundleresource":
				return getAvailableVersions(FileLocator.resolve(baseUrl));
			default:
				throw new IllegalArgumentException("Can't load module info files from base URL: " + baseUrl);
			}
		}
		return availableVersions;
	}

	private static void getAvailableFileUrlVersions(URL baseUrl) throws URISyntaxException {
		File baseDir = new File(baseUrl.toURI());
		availableVersions = Stream.of(baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return isModulesInfoFile(file);
			}
		})).map(f -> getModulesInfoFileVersion(f)).collect(Collectors.toList());
		Collections.sort(availableVersions, versionComparator);
	}

	private static Pattern jarUrlPat = Pattern.compile("[jJ][aA][rR]:[fF][iI][lL][eE]:([^!]+)!(.*)");

	private static void getAvailableJarUrlVersions(URL baseUrl) throws URISyntaxException, IOException {
		Matcher matcher = jarUrlPat.matcher(baseUrl.toString());
		if (matcher.matches()) {
			getAvailableJarFileVersions(new JarFile(new File(matcher.group(1))), matcher.group(2));
		} else {
			throw new IllegalArgumentException("Unable to locate module info from URL: " + baseUrl);
		}
	}

	private static void getAvailableJarFileVersions(JarFile jarFile, String basePath) {
		availableVersions = new ArrayList<>();
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			File entryFile = new File(entry.getName());
			if (isModulesInfoFile(entryFile)) {
				availableVersions.add(getModulesInfoFileVersion(entryFile));
			}
		}
	}

	private static Pattern moduleFilePattern = Pattern.compile("modulesInfo_(\\d+[.]\\d+[.]\\d+)[.]csv");

	private static boolean isModulesInfoFile(File file) {
		return moduleFilePattern.matcher(file.getName()).matches();
	}

	private static String getModulesInfoFileVersion(File file) {
		Matcher m = moduleFilePattern.matcher(file.getName());
		m.matches();
		return m.group(1);
	}

	public String getScgVersion() {
		return scgVersion;
	}

	public List<String> getModuleNames() {
		return Lists.newArrayList(modulesInfo.keySet());
	}

	public Info getInfo(CodegenConfig config) {
		return getInfo(config, false);

	}

	public Info getInfo(CodegenConfig config, boolean createIfMissing) {
		Info info = getInfo(config.getClass().getName());
		if (info == null && createIfMissing) {
			info = new Info(config);
		}
		return info;
	}

	public Info getInfo(String className) {
		return modulesInfo.get(className);
	}

	public void resetStatus() {
		for (Info info : modulesInfo.values()) {
			info.setChanged(false);
			info.setBuiltin(false);
			info.setNew(false);
			info.setVetted(false);
		}
	}

	public void addOrUpdateInfo(String className, Info info) {
		Info existing = getInfo(className);
		if (existing == null) {
			existing = new Info();
			existing.setNew(true);
			modulesInfo.put(className, existing);
		}
		existing.setBuiltin(true);
		if (info.getType() != null && info.getType() != existing.getType()) {
			existing.setType(info.getType());
			existing.setChanged(true);
		}
		if (info.getReportedName() != null && !info.getReportedName().equals(existing.getReportedName())) {
			existing.setReportedName(info.getReportedName());
			existing.setChanged(true);
		}
		if (info.getDerivedDisplayName() != null
				&& !info.getDerivedDisplayName().equals(existing.getDerivedDisplayName())) {
			existing.setDerivedDisplayName(info.getDerivedDisplayName());
			existing.setChanged(true);
		}
		if (info.getDisplayName() != null && !info.getDisplayName().equals(existing.getDisplayName())) {
			existing.setDisplayName(info.getDisplayName());
			existing.setChanged(true);
		}
		if (info.getParameters() != null) {
			existing.setParameters(info.getParameters());
		}
	}

	public void purgeNonBuiltin() {
		List<String> nonBuiltin = modulesInfo.keySet().stream().filter(cls -> !modulesInfo.get(cls).isBuiltin())
				.collect(Collectors.toList());
		nonBuiltin.forEach(cls -> modulesInfo.remove(cls));
	}

	public Collection<String> getClassNames() {
		return modulesInfo.keySet();
	}

	public SwaggerCodegenModulesInfo load(URL baseUrl) throws IOException {
		loadMainFile(new URL(baseUrl, getMainFileName()));
		URL paramsFileUrl = new URL(baseUrl, getParamsFileName());
		loadParamsFile(paramsFileUrl);
		return this;
	}

	private void loadMainFile(URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			try (CSVParser parser = CSVParser.parse(new InputStreamReader(in), csvFormat)) {
				for (CSVRecord record : parser) {
					String className = record.get(MainColumns.ClassName);
					Info info = new Info(record);
					modulesInfo.put(className, info);
				}
			}
		}
	}

	private void loadParamsFile(URL url) throws IOException {
		try (InputStream in = url.openStream()) {
			try (CSVParser parser = CSVParser.parse(new InputStreamReader(in), csvFormat)) {
				for (CSVRecord record : parser) {
					String className = record.get(ParamColumns.ClassName);
					Parameter param = new Parameter(record);
					if (modulesInfo.containsKey(className)) {
						modulesInfo.get(className).addParameter(param);
					}
				}
			}
		}
	}

	public void save(File dir) throws IOException {
		saveMainFile(dir);
		saveParamsFile(dir);
	}

	private void saveMainFile(File baseDir) throws IOException {
		File mainFile = new File(baseDir, getMainFileName());
		try (CSVPrinter printer = new CSVPrinter(new FileWriter(mainFile), csvFormat.withHeader(MainColumns.class))) {
			List<String> classNames = Lists.newArrayList(modulesInfo.keySet());
			Collections.sort(classNames);
			for (String name : classNames) {
				printer.printRecord(modulesInfo.get(name).values(name));
			}
		}
	}

	private void saveParamsFile(File baseDir) throws IOException {
		try (Writer out = new FileWriter(new File(baseDir, getParamsFileName()))) {
			try (CSVPrinter printer = new CSVPrinter(out, csvFormat.withHeader(ParamColumns.class))) {
				List<String> classNames = Lists.newArrayList(modulesInfo.keySet());
				Collections.sort(classNames);
				for (String name : classNames) {
					for (Parameter param : modulesInfo.get(name).getParameters()) {
						printer.printRecord(param.values(name));
					}
				}
			}
		}
	}

	private String getParamsFileName() {
		return String.format("moduleParams_%s.csv", scgVersion);
	}

	private String getMainFileName() {
		return String.format("modulesInfo_%s.csv", scgVersion);
	}

	private enum MainColumns {
		ClassName, Type, ReportedName, DerivedDisplayName, DisplayName, Suppressed, Builtin, Changed, New, Vetted
	};

	public static class Info {

		private CodegenType type;
		private String reportedName;
		private String derivedDisplayName;
		private String displayName;
		private boolean suppressed;
		private boolean builtin;
		private boolean changed;
		private boolean isNew;
		private boolean vetted;

		private List<Parameter> parameters;

		public Info() {
			parameters = Lists.newArrayList();
		}

		public Info(CodegenConfig config) {
			this();
			try {
				setType(config.getTag());
			} catch (Throwable e) {
				// internal config problem in codegen module... ignore
			}
			setReportedName(config.getName());
			setDerivedDisplayName(BuiltinSwaggerCodegenGenTemplate.getDerivedName(config));
			setParameters(getParameters(config));

		}

		private List<Parameter> getParameters(CodegenConfig config) {
			List<Parameter> params = Lists.newArrayList();
			Set<String> paramNames = Sets.newHashSet();
			for (CliOption option : config.cliOptions()) {
				if (paramNames.contains(option.getOpt())) {
					logger.warn("Duplicate parameter '{}' ignored for SCG module {}", option.getOpt(),
							config.getClass().getName());
				} else {
					Parameter param = new Parameter();
					param.setName(option.getOpt());
					param.setDescription(option.getDescription());
					param.setRequired(false);
					params.add(param);
					paramNames.add(option.getOpt());
				}
			}
			return params.size() > 0 ? params : null;
		}

		public Info(CSVRecord record) {
			this();
			setType(codegenTypeRecordValue(record.get(MainColumns.Type)));
			setReportedName(stringRecordValue(record.get(MainColumns.ReportedName)));
			setDerivedDisplayName(stringRecordValue(record.get(MainColumns.DerivedDisplayName)));
			setDisplayName(stringRecordValue(record.get(MainColumns.DisplayName)));
			setSuppressed(boolRecordValue(record.get(MainColumns.Suppressed)));
			setBuiltin(boolRecordValue(record.get(MainColumns.Builtin)));
			setChanged(boolRecordValue(record.get(MainColumns.Changed)));
			setNew(boolRecordValue(record.get(MainColumns.New)));
			setVetted(boolRecordValue(record.get(MainColumns.Vetted)));
		}

		public List<String> values(String className) {
			List<String> result = Lists.newArrayList();
			for (MainColumns field : MainColumns.values()) {
				switch (field) {
				case ClassName:
					result.add(className);
					break;
				case Type:
					result.add(getType().toString());
					break;
				case ReportedName:
					result.add(getReportedName());
					break;
				case DerivedDisplayName:
					result.add(getDerivedDisplayName());
					break;
				case DisplayName:
					result.add(getDisplayName());
					break;
				case Suppressed:
					result.add(boolMarker(isSuppressed()));
					break;
				case Builtin:
					result.add(boolMarker(isBuiltin()));
					break;
				case Changed:
					result.add(boolMarker(isChanged()));
					break;
				case New:
					result.add(boolMarker(isNew()));
					break;
				case Vetted:
					result.add(boolMarker(isVetted()));
				}
			}
			return result;
		}

		private String boolMarker(boolean value) {
			return value ? "*" : "";
		}

		public final CodegenType getType() {
			return type;
		}

		public final void setType(CodegenType type) {
			this.type = type;
		}

		public final String getReportedName() {
			return reportedName;
		}

		public final void setReportedName(String reportedName) {
			this.reportedName = reportedName;
		}

		public final String getDerivedDisplayName() {
			return derivedDisplayName;
		}

		public final void setDerivedDisplayName(String derivedDisplayName) {
			this.derivedDisplayName = derivedDisplayName;
		}

		public final String getDisplayName() {
			return displayName;
		}

		public final void setDisplayName(String displayName) {
			this.displayName = displayName;
		}

		public final boolean isSuppressed() {
			return suppressed;
		}

		public final void setSuppressed(boolean suppressed) {
			this.suppressed = suppressed;
		}

		public boolean isBuiltin() {
			return builtin;
		}

		public void setBuiltin(boolean bulitin) {
			this.builtin = bulitin;
		}

		public boolean isChanged() {
			return changed;
		}

		public void setChanged(boolean changed) {
			this.changed = changed;
		}

		public boolean isNew() {
			return isNew;
		}

		public void setNew(boolean isNew) {
			this.isNew = isNew;
		}

		public boolean isVetted() {
			return vetted;
		}

		public void setVetted(boolean vetted) {
			this.vetted = vetted;
		}

		public final List<Parameter> getParameters() {
			return parameters != null ? parameters : Lists.<Parameter>newArrayList();
		}

		public final void setParameters(List<Parameter> parameters) {
			this.parameters = parameters;
		}

		public final void addParameter(Parameter parameter) {
			parameters.add(parameter);
		}

	}

	private static String stringRecordValue(String s) {
		return s.isEmpty() ? null : s;
	}

	private static boolean boolRecordValue(String s) {
		return s.equals("*");
	}

	private static CodegenType codegenTypeRecordValue(String s) {
		return s.isEmpty() ? null : CodegenType.valueOf(s);
	}

	private enum ParamColumns {
		ClassName, Name, Description, DefaultValue, Required
	}

	public static class Parameter {

		private String name;
		private String description;
		private String defaultValue;
		private boolean required;

		public Parameter() {
		}

		public Parameter(CSVRecord record) {
			setName(stringRecordValue(record.get(ParamColumns.Name).trim()));
			setDescription(stringRecordValue(record.get(ParamColumns.Description).trim()));
			setDefaultValue(stringRecordValue(record.get(ParamColumns.DefaultValue).trim()));
			setRequired(boolRecordValue(record.get(ParamColumns.Required).trim()));
		}

		public List<String> values(String className) {
			ArrayList<String> result = Lists.newArrayList();
			for (ParamColumns value : ParamColumns.values()) {
				switch (value) {
				case ClassName:
					result.add(className);
					break;
				case Name:
					result.add(getName());
					break;
				case Description:
					result.add(getDescription());
					break;
				case DefaultValue:
					result.add(getDefaultValue());
					break;
				case Required:
					result.add(isRequired() ? "*" : "");
					break;
				}
			}
			return result;
		}

		public final String getName() {
			return name;
		}

		public final void setName(String name) {
			this.name = name;
		}

		public final String getDescription() {
			return description;
		}

		public final void setDescription(String description) {
			this.description = description;
		}

		public final String getDefaultValue() {
			return defaultValue;
		}

		public final void setDefaultValue(String defaultValue) {
			this.defaultValue = defaultValue;
		}

		public final boolean isRequired() {
			return required;
		}

		public final void setRequired(boolean required) {
			this.required = required;
		}
	}

	private static class VersionComparator implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			String[] parts1 = o1.split("[.]");
			String[] parts2 = o2.split("[.]");
			for (int i = 0; i < parts1.length; i++) {
				int i1 = Integer.valueOf(parts1[i]);
				int i2 = Integer.valueOf(parts2[i]);
				if (i1 != i2) {
					return i1 - i2;
				}
			}
			return 0;
		}
	}

	private static VersionComparator versionComparator = new VersionComparator();

	private static int versionCompare(String version1, String version2) {
		return versionComparator.compare(version1, version2);
	}
}
