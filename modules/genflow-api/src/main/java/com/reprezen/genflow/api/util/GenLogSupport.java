/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.util;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;

public class GenLogSupport {
	private final ObjectMapper mapper;
	private static final String YAML_DOC_PREFIX = "---";

	public GenLogSupport() {
		mapper = new ObjectMapper(new YAMLFactory());
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public String toWriteString(LogRecord record) throws JsonProcessingException {
		String serialized = mapper.writeValueAsString(Lists.newArrayList(record));
		if (serialized.startsWith(YAML_DOC_PREFIX)) {
			serialized = serialized.substring(YAML_DOC_PREFIX.length());
		}
		return serialized;
	}

	public List<LogRecord> fromString(File logFile) throws JsonParseException, JsonMappingException, IOException {
		List<LogRecord> result = Lists.newArrayList();
		List<Map<String, Object>> logEntries = mapper.readValue(logFile,
				mapper.getTypeFactory().constructCollectionType(List.class, Object.class));
		for (Map<String, Object> logEntry : logEntries) {
			result.add(fromMap(logEntry));
		}
		return result;

	}

	protected LogRecord fromMap(Map<String, Object> logRecord) {
		// We can't bind directly to LogRecord because of missing default constructors
		@SuppressWarnings("unchecked")
		Map<String, Object> level = (Map<String, Object>) logRecord.get("level");
		String levelName = (String) level.get("name");
		String message = (String) logRecord.get("message");
		Throwable exception = mapper.convertValue(logRecord.get("thrown"), Throwable.class);
		if (exception != null) {
			message += ": " + exception.getMessage();
		}
		Level severity = Level.INFO;
		switch (levelName) {
		case "ABNORMAL":
			severity = GeneratorLauncher.ABNORMAL_EXIT;
			break;
		case "SEVERE":
			severity = Level.SEVERE;
			break;
		case "WARNING":
			severity = Level.WARNING;
			break;
		case "INFO":
			severity = Level.INFO;
			break;
		default:
			severity = Level.INFO;
			break;
		}
		LogRecord result = new LogRecord(severity, message);
		if (exception != null) {
			result.setThrown(exception);
		}
		return result;
	}

	public String getLogFile(File genTargetFile) {
		return genTargetFile.getAbsolutePath() + ".log";
	}

}
