/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.target;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplateRegistry;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplate.Generator;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

/**
 * @author Konstantin Zaitsev
 * @date May 19, 2015
 */
public class GenTarget {

    /**
     * Name of this GenTarget (used in naming of .gen file.
     * <p>
     * This is typically copied from the name of the GenTemplate executed by this GenTarget, but it need not be. For
     * example, two GenTargets in the same directory that execute the same GenTemplate must have different names.
     */
    private String name;

    /** ID of GenTemplate executed by this GenTarget */
    private String genTemplateId;

    /**
     * Instance of GenTemplate type to be executed.
     * <p>
     * If this is null when execution is requested, it will be looked up in the headless registry using the id.
     */
    @JsonIgnore
    private IGenTemplate genTemplate = null;

    /**
     * Base directory for resolving file paths.
     * <p>
     * This is not saved in the serialized form, and it is set during deserialization to the directory containing the
     * GenTarget, to achieve a level of portability.
     */
    @JsonIgnore
    private File baseDir;

    /** Generation output directory relative to {@link #baseDir}. */
    private File relativeOutputDir;

    private GenTargetPrimarySource primarySource;

    /** Paths to prerequisite .gen files for prerequisite GenTargets. */
    Map<String, GenTargetPrerequisite> prerequisites = Maps.newHashMap();

    private final Map<String, GenTargetNamedSource> namedSources = Maps.newHashMap();

    private Map<String, Object> parameters = Maps.newHashMap();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenTemplateId() {
        return genTemplateId;
    }

    public void setGenTemplateId(String genTemplateId) {
        this.genTemplateId = genTemplateId;
    }

    public void setGenTemplate(IGenTemplate genTemplate) {
        this.genTemplate = genTemplate;
        setGenTemplateId(genTemplate.getId());
    }

    public File getBaseDir() {
        return baseDir;
    }

    public void setBaseDir(File baseDir) {
        this.baseDir = baseDir;
    }

    public File getRelativeOutputDir() {
        return relativeOutputDir;
    }

    public void setRelativeOutputDir(File relativeOutputDir) {
        this.relativeOutputDir = relativeOutputDir;
    }

    public GenTargetPrimarySource getPrimarySource() {
        return primarySource;
    }

    public void setPrimarySource(GenTargetPrimarySource primarySource) {
        this.primarySource = primarySource;
    }

    public Map<String, GenTargetPrerequisite> getPrerequisites() {
        return prerequisites;
    }

    @JsonProperty("prerequisites")
    public Collection<GenTargetPrerequisite> getPrerequisiteList() {
        return prerequisites.values();
    }

    @JsonProperty("prerequisites")
    public void setPrerequisiteList(List<GenTargetPrerequisite> prerequisiteList) {
        prerequisites.clear();
        if (prerequisiteList != null) {
            for (GenTargetPrerequisite prerequisite : prerequisiteList) {
                this.prerequisites.put(prerequisite.getName(), prerequisite);
            }
        }
    }

    public Map<String, GenTargetNamedSource> getNamedSources() {
        return namedSources;
    }

    @JsonProperty("namedSources")
    public Collection<GenTargetNamedSource> getNamedSourceList() {
        return namedSources.values();
    }

    @JsonProperty("namedSources")
    public void SetNamedSourceList(List<GenTargetNamedSource> namedSourceList) {
        namedSources.clear();
        if (namedSourceList != null) {
            for (GenTargetNamedSource namedSource : namedSourceList) {
                namedSources.put(namedSource.getName(), namedSource);
            }
        }
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(Object parameters) throws GenerationException {
        if (parameters == null) {
            this.parameters = Maps.newHashMap();
        } else if (parameters instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> parameterMap = (Map<String, Object>) parameters;
            this.parameters = parameterMap;
        } else if (parameters instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Map<String, Object>> parameterList = (Collection<Map<String, Object>>) parameters;
            Map<String, Object> parameterMap = Maps.newHashMap();
            for (Map<String, Object> parameter : parameterList) {
                parameterMap.put((String) parameter.get("name"), parameter.get("value"));
            }
            this.parameters = parameterMap;
        } else {
            throw new GenerationException("Invalid type for 'parameters' property in GenTarget file");
        }
    }

    public File getOutputDir() throws GenerationException {
        return resolvePath(relativeOutputDir);
    }

    public File resolvePath(File path) throws GenerationException {
        if (path.isAbsolute()) {
            return path;
        } else {
            try {
                return new File(getBaseDir(), path.getPath()).getCanonicalFile();
            } catch (IOException e) {
                throw new GenerationException(
                        "Failed to resolve path " + path + " relative to GenTarget base " + getBaseDir());
            }
        }
    }

    public File resolvePrimarySourcePath() throws GenerationException {
        return primarySource != null ? resolvePath(primarySource.getPath()) : null;
    }

    public File resolveOutputPath(File path) throws GenerationException {
        if (path.isAbsolute()) {
            return path;
        } else {
            return new File(getOutputDir(), path.toString());
        }
    }

    public GenTemplateTrace execute(GenTemplateTraces traces) throws GenerationException {
        return execute(traces, null);
    }

    public GenTemplateTrace execute(Logger logger) throws GenerationException {
        return execute(null, logger);
    }

    public GenTemplateTrace execute() throws GenerationException {
        return execute(null, null);
    }

    public GenTemplateTrace execute(GenTemplateTraces traces, Logger logger) throws GenerationException {
        if (traces == null) {
            traces = new GenTemplateTraces();
        }
        if (logger == null) {
            logger = Logger.getAnonymousLogger();
        }
        if (genTemplate == null) {
            try {
                genTemplate = GenTemplateRegistry.getGenTemplate(genTemplateId).getInstance();
            } catch (Throwable e) {
                logger.severe("Failed to locate GenTemplate " + genTemplateId);
                e.printStackTrace();
                throw new GenerationException("doh", e);
            }
        }
        Generator generator = genTemplate.getGenerator();
        generator.attachLogger(logger);
        return generator.generate(this, traces);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((baseDir == null) ? 0 : baseDir.hashCode());
        result = prime * result + ((genTemplateId == null) ? 0 : genTemplateId.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((namedSources == null) ? 0 : namedSources.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((prerequisites == null) ? 0 : prerequisites.hashCode());
        result = prime * result + ((primarySource == null) ? 0 : primarySource.hashCode());
        result = prime * result + ((relativeOutputDir == null) ? 0 : relativeOutputDir.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GenTarget other = (GenTarget) obj;
        if (baseDir == null) {
            if (other.baseDir != null)
                return false;
        } else if (!baseDir.equals(other.baseDir))
            return false;
        if (genTemplateId == null) {
            if (other.genTemplateId != null)
                return false;
        } else if (!genTemplateId.equals(other.genTemplateId))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (namedSources == null) {
            if (other.namedSources != null)
                return false;
        } else if (!namedSources.equals(other.namedSources))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (prerequisites == null) {
            if (other.prerequisites != null)
                return false;
        } else if (!prerequisites.equals(other.prerequisites))
            return false;
        if (primarySource == null) {
            if (other.primarySource != null)
                return false;
        } else if (!primarySource.equals(other.primarySource))
            return false;
        if (relativeOutputDir == null) {
            if (other.relativeOutputDir != null)
                return false;
        } else if (!relativeOutputDir.equals(other.relativeOutputDir))
            return false;
        return true;
    }

}
