package com.reprezen.genflow.rapidml.csharp;

import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.reprezen.rapidml.ZenModel;

public class Config {
    private String rootNamespace;
    private String modelsFolder;
    private String controllersFolder;
    @JsonProperty("generateModelPOCOs")
    private boolean generateModelPocos;
    @JsonProperty("generateJSONSerialization")
    private boolean generateJsonSerialization;
    private boolean generateDelegateController;
    private Framework framework;

    public void validate(ZenModel model) throws IllegalArgumentException {
        if (rootNamespace == null) {
            rootNamespace = model.getName();
        }
        checkValidName(rootNamespace, "rootNamespace");
        checkValidFolder(modelsFolder, "modelsFolder");
        checkValidFolder(controllersFolder, "controllersFolder");
        checkValidFramework(framework);
    }

    private static Pattern namePat = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*");

    private void checkValidName(String name, String paramName) throws IllegalArgumentException {
        if (!namePat.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "Illegal namespace '" + name + "' specified for " + paramName + " in GenTarget");
        }
    }

    private void checkValidFolder(String folder, String paramName) throws IllegalArgumentException {
        try {
            Paths.get(folder);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Illegal folder '" + folder + "' specified for " + paramName + " in GenTarget", e);
        }
    }

    private void checkValidFramework(Framework framework) {
        if (framework == null) {
            throw new IllegalArgumentException("Illegal framework parameter value in GenTarget - use one of: "
                    + StringUtils.join(Framework.allFullNames(), ", "));
        }
    }

    public String getRootNamespace() {
        return rootNamespace;
    }

    public String getModelsFolder() {
        return modelsFolder;
    }

    public String getControllersFolder() {
        return controllersFolder;
    }

    public static Pattern getNamePat() {
        return namePat;
    }

    public boolean isGenerateModelPocos() {
        return generateModelPocos;
    }

    public boolean isGenerateJsonSerialization() {
        return generateJsonSerialization;
    }

    public boolean isGenerateDelegateController() {
        return generateDelegateController;
    }

    public Framework getFramework() {
        return framework;
    }

    public void setFramework(String fullName) {
        this.framework = Framework.forFullName(fullName);
    }

    public enum Framework {
        ASP_DOTNET_WEBAPI_2("ASP.NET Web API 2"), ASP_DOTNET_CORE_2_0_MVC("ASP.NET Core 2.0 MVC");

        private String fullName;

        Framework(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() {
            return fullName;
        }

        public static Framework forFullName(String fullName) {
            for (Framework value : values()) {
                if (value.fullName.equals(fullName)) {
                    return value;
                }
            }
            return null;
        }

        public static List<String> allFullNames() {
            List<String> result = Lists.newArrayList();
            for (Framework value : values()) {
                result.add(value.fullName);
            }
            return result;
        }

    }
}