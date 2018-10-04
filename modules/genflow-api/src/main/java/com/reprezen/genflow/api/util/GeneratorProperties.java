package com.reprezen.genflow.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GeneratorProperties {

	private static Properties props = new Properties();

	private static final String VERSION_PROP = "version";

	public static String getRepreZenVersion() {
		return getProperties().containsKey(VERSION_PROP) ? props.getProperty(VERSION_PROP) : "unknown";
	}

	private static Properties getProperties() {
		if (props.isEmpty()) {
			try (InputStream propsStream = GeneratorProperties.class.getResourceAsStream("generator.properties")) {
				props.load(propsStream);
			} catch (IOException e) {
			}
		}
		return props;
	}
}
