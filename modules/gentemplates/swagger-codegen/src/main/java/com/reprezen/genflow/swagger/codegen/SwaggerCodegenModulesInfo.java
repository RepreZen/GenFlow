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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.swagger.codegen.CodegenType;

public class SwaggerCodegenModulesInfo {
    private final Map<String, Info> modulesInfo = Maps.newTreeMap();

    private final String MAIN_FILE = "modulesInfo.csv";
    private final String PARAMS_FILE = "moduleParams.csv";
    private final CSVFormat csvFormat = CSVFormat.RFC4180.withHeader();

    public List<String> getModuleNames() {
        return Lists.newArrayList(modulesInfo.keySet());
    }

    public Info getInfo(Class<?> c) {
        return getInfo(c.getName());
    }

    public Info getInfo(String className) {
        return modulesInfo.get(className);
    }

    public void resetStatus() {
        for (Info info : modulesInfo.values()) {
            info.setChanged(false);
            info.setDiscovered(false);
        }
    }

    public void addOrUpdateInfo(Class<?> c, Info info) {
        Info existing = getInfo(c.getName());
        if (existing == null) {
            existing = new Info();
            modulesInfo.put(c.getName(), existing);
        }
        existing.setDiscovered(true);
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

    public Collection<String> getClassNames() {
        return modulesInfo.keySet();
    }

    public void load(URL baseUrl) throws IOException {
        try {
            loadMainFile(new URL(baseUrl, MAIN_FILE));
            URL paramsFileUrl = new URL(baseUrl, PARAMS_FILE);
            loadParamsFile(paramsFileUrl);
        } catch (IOException e) {
        }
    }

    private void loadMainFile(URL url) throws IOException {
        try (CSVParser parser = parseUrl(url)) {
            for (CSVRecord record : parser) {
                String className = record.get(MainColumns.ClassName);
                Info info = new Info(record);
                modulesInfo.put(className, info);
            }
        }
    }

    private CSVParser parseUrl(URL url) throws IOException {
        return CSVParser.parse(new InputStreamReader(url.openStream()), csvFormat);
    }

    private void loadParamsFile(URL url) throws IOException {
        try (CSVParser parser = parseUrl(url)) {
            for (CSVRecord record : parser) {
                String className = record.get(ParamColumns.ClassName);
                Parameter param = new Parameter(record);
                if (modulesInfo.containsKey(className)) {
                    modulesInfo.get(className).addParameter(param);
                }
            }
        }
    }

    public void save(File dir) throws IOException {
        saveMainFile(dir);
        saveParamsFile(dir);
    }

    private void saveMainFile(File baseDir) throws IOException {
        File mainFile = new File(baseDir, MAIN_FILE);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(mainFile), csvFormat.withHeader(MainColumns.class))) {
            List<String> classNames = Lists.newArrayList(modulesInfo.keySet());
            Collections.sort(classNames);
            for (String name : classNames) {
                printer.printRecord(modulesInfo.get(name).values(name));
            }
        }
    }

    private void saveParamsFile(File baseDir) throws IOException {
        File paramsFile = new File(baseDir, PARAMS_FILE);
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(paramsFile),
                csvFormat.withHeader(ParamColumns.class))) {
            List<String> classNames = Lists.newArrayList(modulesInfo.keySet());
            Collections.sort(classNames);
            for (String name : classNames) {
                for (Parameter param : modulesInfo.get(name).getParameters()) {
                    printer.printRecord(param.values(name));
                }
            }
        }
    }

    private enum MainColumns {
        ClassName, Type, ReportedName, DerivedDisplayName, DisplayName, Suppressed, Discovered, Changed
    };

    public static class Info {

        private CodegenType type;
        private String reportedName;
        private String derivedDisplayName;
        private String displayName;
        private boolean suppressed;
        private boolean discovered;
        private boolean changed;
        private List<Parameter> parameters;

        public Info() {
            parameters = Lists.newArrayList();
        }

        public Info(CSVRecord record) {
            this();
            setType(codegenTypeRecordValue(record.get(MainColumns.Type)));
            setReportedName(stringRecordValue(record.get(MainColumns.ReportedName)));
            setDerivedDisplayName(stringRecordValue(record.get(MainColumns.DerivedDisplayName)));
            setDisplayName(stringRecordValue(record.get(MainColumns.DisplayName)));
            setSuppressed(boolRecordValue(record.get(MainColumns.Suppressed)));
            setDiscovered(boolRecordValue(record.get(MainColumns.Discovered)));
            setChanged(boolRecordValue(record.get(MainColumns.Changed)));
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
                    result.add(isSuppressed() ? "*" : "");
                    break;
                case Discovered:
                    result.add(isDiscovered() ? "*" : "");
                    break;
                case Changed:
                    result.add(isChanged() ? "*" : "");
                    break;
                }
            }
            return result;
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

        public boolean isDiscovered() {
            return discovered;
        }

        public void setDiscovered(boolean discovered) {
            this.discovered = discovered;
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }

        public final List<Parameter> getParameters() {
            return parameters != null ? parameters : Lists.<Parameter> newArrayList();
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
}
