/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.codegen;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class GenModulesInfo {

	private final String libVersion;
	private final Map<String, Info> modulesInfo = Maps.newTreeMap();
	private final CSVFormat csvFormat = CSVFormat.RFC4180.withHeader();

	public static GenModulesInfo load(String libVersion, URL baseUrl, GenModuleWrapper<?> dummyWrapper)
			throws IOException, URISyntaxException {
		String bestVersion = null;
		for (String version : getAvailableVersions(baseUrl)) {
			if (versionCompare(version, libVersion) <= 0) {
				bestVersion = version;
			} else {
				break;
			}
		}
		return bestVersion != null ? new GenModulesInfo(bestVersion).load(baseUrl, dummyWrapper) : null;
	}

	public GenModulesInfo(String libVersion) {
		this.libVersion = libVersion;
	}

	private static List<String> getAvailableVersions(URL baseUrl) throws URISyntaxException, IOException {
		List<String> versions;
		switch (baseUrl.getProtocol().toLowerCase()) {
		case "file":
			versions = getAvailableFileUrlVersions(baseUrl);
			break;
		case "jar":
			versions = getAvailableJarUrlVersions(baseUrl);
			break;
		case "bundleresource":
			versions = getAvailableVersions(FileLocator.resolve(baseUrl));
			break;
		default:
			throw new IllegalArgumentException("Can't load module info files from base URL: " + baseUrl);
		}
		Collections.sort(versions, versionComparator);
		return versions;
	}

	private static List<String> getAvailableFileUrlVersions(URL baseUrl) throws URISyntaxException {
		File baseDir = new File(baseUrl.toURI());
		List<String> versions = Stream.of(baseDir.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return isModulesInfoFile(file);
			}
		})).map(f -> getModulesInfoFileVersion(f)).collect(Collectors.toList());
		return versions;
	}

	private static Pattern jarUrlPat = Pattern.compile("[jJ][aA][rR]:[fF][iI][lL][eE]:([^!]+)!(.*)");

	private static List<String> getAvailableJarUrlVersions(URL baseUrl) throws URISyntaxException, IOException {
		Matcher matcher = jarUrlPat.matcher(baseUrl.toString());
		if (matcher.matches()) {
			try (JarFile jarFile = new JarFile(new File(matcher.group(1)))) {
				return getAvailableJarFileVersions(jarFile, matcher.group(2));
			}
		} else {
			throw new IllegalArgumentException("Unable to locate module info from URL: " + baseUrl);
		}
	}

	private static List<String> getAvailableJarFileVersions(JarFile jarFile, String basePath) {
		List<String> versions = new ArrayList<>();
		for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
			JarEntry entry = entries.nextElement();
			File entryFile = new File(entry.getName());
			if (isModulesInfoFile(entryFile)) {
				versions.add(getModulesInfoFileVersion(entryFile));
			}
		}
		return versions;
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

	public String getLibVersion() {
		return libVersion;
	}

	public List<String> getModuleNames() {
		return Lists.newArrayList(modulesInfo.keySet());
	}

	public Info getInfo(GenModuleWrapper<?> wrapper) {
		return getInfo(wrapper, false);
	}

	public Info getInfo(GenModuleWrapper<?> wrapper, boolean createIfMissing) {
		Info info = getInfo(wrapper.getClassName());
		if (info == null && createIfMissing) {
			info = new Info(wrapper);
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
		}
	}

	public void addOrUpdateInfo(String className, Info info) {
		Info existing = getInfo(className);
		if (existing == null) {
			existing = new Info();
			existing.setNew(true);
			existing.setVetted(info.isVetted());
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

	public GenModulesInfo load(URL baseUrl, GenModuleWrapper<?> dummyWrapper) throws IOException {
		loadMainFile(new URL(baseUrl, getMainFileName()), dummyWrapper);
		URL paramsFileUrl = new URL(baseUrl, getParamsFileName());
		loadParamsFile(paramsFileUrl);
		return this;
	}

	private void loadMainFile(URL url, GenModuleWrapper<?> dummyWrapper) throws IOException {
		try (InputStream in = url.openStream()) {
			try (CSVParser parser = CSVParser.parse(new InputStreamReader(in), csvFormat)) {
				for (CSVRecord record : parser) {
					String className = record.get(MainColumns.ClassName);
					Info info = new Info(record, dummyWrapper);
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
		return String.format("moduleParams_%s.csv", libVersion);
	}

	private String getMainFileName() {
		return String.format("modulesInfo_%s.csv", libVersion);
	}

	private enum MainColumns {
		ClassName, Type, ReportedName, DerivedDisplayName, DisplayName, Suppressed, Builtin, Changed, New, Vetted
	};

	public static class Info {

		private Enum<?> type;
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

		public Info(GenModuleWrapper<?> wrapper) {
			this();
			try {
				setType(wrapper.getType());
			} catch (Throwable e) {
				// internal config problem in codegen module... ignore
			}
			setReportedName(wrapper.getName());
			setDerivedDisplayName(wrapper.getDerivedName());
			setParameters(wrapper.getParameters());

		}

		public Info(CSVRecord record, GenModuleWrapper<?> dummyWrapper) {
			this();
			setType(dummyWrapper.typeNamed(record.get(MainColumns.Type)));
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

		public final Enum<?> getType() {
			return type;
		}

		public final void setType(Enum<?> type) {
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
