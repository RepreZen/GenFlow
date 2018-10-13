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
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.reprezen.genflow.api.GenerationException;

/**
 * @author Konstantin Zaitsev
 * @date May 29, 2015
 */
public final class FileUtils {

	// prevent instantiation
	private FileUtils() {
	}

	/**
	 * Copies resources from JAR file to specified location. Used class to lookup
	 * JAR file location.
	 * 
	 * @param codeBase class to lookup JAR file location
	 * @param source   source path in JAR
	 * @param target   target location in local file system
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static List<File> copyResources(Class<?> codeBase, String source, File target) throws Exception {
		URL codeLocation = null;
		File codeSource = null;
		try {

			codeLocation = codeBase.getProtectionDomain().getCodeSource().getLocation();
			codeSource = new File(codeLocation.toURI());
		} catch (URISyntaxException e) {
			// bug in code locations located in plugin bundles - URL incorrectly includes
			// bare spaces, so above fails, but this should work OK.
			codeSource = new File(codeLocation.getPath());
		}
		return copyResources(codeSource, source, target);
	}

	/**
	 * Copies resources from JAR file to specified location.
	 * 
	 * @param codeSource code source location (usually a JAR file, in dev it may be
	 *                   a classes directory)
	 * @param source     source path in JAR
	 * @param target     target location in local file system
	 * @throws IOException
	 */
	public static List<File> copyResources(File codeSource, String source, File target) throws Exception {
		if (codeSource.isFile()) {
			return copyJarResources(codeSource, source, target);
		} else {
			// Unlikely except in dev environment
			return copyFileResources(new File(codeSource, source), target);
		}
	}

	public static List<File> copyJarResources(File jarFile, String source, File target) throws IOException {
		List<File> result = Lists.newArrayList();
		String prefix = source.equals(".") ? "" : source + "/";
		try (JarFile jar = new JarFile(jarFile)) {
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.equals(source)) {
					result.add(copyJarEntry(jar, entry, target));
				} else if (name.startsWith(prefix) && !entry.isDirectory()) {
					File outputFile = new File(target, entry.getName().substring(prefix.length()));
					result.add(copyJarEntry(jar, entry, outputFile));
				}
			}
		}
		return result;
	}

	private static File copyJarEntry(JarFile jar, JarEntry entry, File output) throws IOException {
		if (isDirectory(output)) {
			output = new File(output, new File(entry.getName()).getName());
		}
		try (InputStream in = jar.getInputStream(entry)) {
			Files.createParentDirs(output);
			if (!output.exists() || output.lastModified() != entry.getTime()) {
				// Safe to use: copyInputStreamToFile in Apache Commons IO since v.2.0
				org.apache.commons.io.FileUtils.copyInputStreamToFile(in, output);
				output.setLastModified(entry.getTime());
			}
			return output;
		}
	}

	public static List<File> copyFileResources(File source, File target) throws Exception {
		if (!source.exists()) {
			throw new GenerationException(
					"Static resource copying: the source folder does not exist: " + source.getPath());
		}
		if (source.isDirectory()) {
			return copyDirectory(source, target);
		} else {
			return Collections.singletonList(copyFile(source, target));
		}
	}

	private static File copyFile(File sourceFile, File output) throws IOException {
		if (isDirectory(output)) {
			output = new File(output, sourceFile.getName());
		}
		if (!output.exists() || output.lastModified() != sourceFile.lastModified()) {
			Files.createParentDirs(output);
			Files.copy(sourceFile, output);
			output.setLastModified(sourceFile.lastModified());
		}
		return output;
	}

	private static boolean isDirectory(File file) {
		if (file.exists()) {
			return file.isDirectory();
		} else {
			return file.getPath().endsWith("/");
		}
	}

	private static List<File> copyDirectory(File source, File target) throws IOException {
		List<File> result = Lists.newArrayList();
		File[] filesInDirectory = source.listFiles();
		for (File file : filesInDirectory) {
			if (file.isFile()) {
				File outputFile = new File(target, file.getName());
				result.add(copyFile(file, outputFile));
			} else {
				result.addAll(copyDirectory(file, new File(target, file.getName())));
			}
		}
		return result;
	}
}
