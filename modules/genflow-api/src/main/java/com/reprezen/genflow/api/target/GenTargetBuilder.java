package com.reprezen.genflow.api.target;

import java.io.File;
import java.util.Map;

import com.reprezen.genflow.api.template.IGenTemplate;

public class GenTargetBuilder {

	private GenTarget target = new GenTarget();

	private GenTargetBuilder() {
	}

	public static GenTargetBuilder get() {
		return new GenTargetBuilder();
	}

	public GenTargetBuilder named(String name) {
		target.setName(name);
		return this;
	}

	public GenTargetBuilder forGenTemplate(String id) {
		target.setGenTemplateId(id);
		return this;
	}

	public GenTargetBuilder forGenTemplate(IGenTemplate template) {
		target.setGenTemplate(template);
		return this;
	}

	public GenTargetBuilder withBaseDir(File baseDir) {
		target.setBaseDir(baseDir);
		return this;
	}

	public GenTargetBuilder withBaseDir(String outputDir) {
		return withBaseDir(new File(outputDir));
	}

	public GenTargetBuilder withOutputDir(File outputDir) {
		target.setRelativeOutputDir(outputDir);
		return this;
	}

	public GenTargetBuilder withOutputDir(String outputDir) {
		return withOutputDir(new File(outputDir));
	}

	public GenTargetBuilder withPrimarySource(File source) {
		GenTargetPrimarySource primarySource = new GenTargetPrimarySource();
		primarySource.setPath(source);
		target.setPrimarySource(primarySource);
		return this;
	}

	public GenTargetBuilder withNamedSource(String name, File file) {
		GenTargetNamedSource namedSource = new GenTargetNamedSource();
		namedSource.setName(name);
		namedSource.setPath(file);
		target.getNamedSourceList().add(namedSource);
		return this;
	}

	public GenTargetBuilder withPrerequisite(File prereqGenFile) {
		GenTargetPrerequisite prereq = new GenTargetPrerequisite();
		prereq.setGenFilePath(prereqGenFile);
		target.getPrerequisiteList().add(prereq);
		return this;
	}

	public GenTargetBuilder withParameter(String name, Object value) {
		target.getParameters().put(name, value);
		return this;
	}

	public GenTargetBuilder withParameters(Map<String, Object> parameters) {
		target.getParameters().putAll(parameters);
		return this;
	}

	public GenTarget build() {
		return target;
	}
}
