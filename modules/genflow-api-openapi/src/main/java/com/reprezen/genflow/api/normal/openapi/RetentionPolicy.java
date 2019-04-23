package com.reprezen.genflow.api.normal.openapi;

import java.net.URL;

/**
 * Class used to determine whether to retain objects in input files into the
 * normalized output
 */
public class RetentionPolicy {

	private boolean topHasPaths;
	private Options options;

	public RetentionPolicy(Content topContent, Options options) {
		this.topHasPaths = topContent.getObjectNames(ObjectType.PATH).iterator().hasNext();
		this.options = options;
	}

	public boolean shouldRetain(URL url, ObjectType type) {
		boolean inScope = options.isUrlInScope(url.toString());
		boolean retained = options.isRetained(type, topHasPaths);
		boolean result = inScope && retained;
		return result;
	}
}
