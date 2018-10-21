/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.observe;

import org.eclipse.core.resources.IFile;

/**
 * Contains information about a RepreZen-generated artifact.
 * 
 * @author jimleroyer
 */
public class GeneratedArtifact {

	protected IFile file;
	protected String type;
	protected String relatedModelName;

	public GeneratedArtifact(IFile file) {
		this(file, "", "");
	}

	public GeneratedArtifact(IFile file, String relatedModelName) {
		this(file, "", relatedModelName);
	}

	public GeneratedArtifact(IFile file, String type, String relatedModelName) {
		this.file = file;
		this.type = type;
		this.relatedModelName = relatedModelName;
	}

	public IFile getFile() {
		return file;
	}

	public void setFile(IFile file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRelatedModelName() {
		return relatedModelName;
	}

	public void setRelatedModelName(String relatedModelName) {
		this.relatedModelName = relatedModelName;
	}

}
