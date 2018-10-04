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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetUtils;
import com.reprezen.genflow.api.target.IncorrectGenTargetException;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

public class GeneratorLauncher {

	private Logger logger = createLogger();
	private GenLogSupport genLogSupport = new GenLogSupport();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("GL");
		if (args.length == 0) {
			throw new IllegalArgumentException(
					"No GenTargets specified. Please provide one or more GenTarget name or path");//$NON-NLS-1$
		}
		if (!(new GeneratorLauncher().run(args))) {
			throw new RuntimeException("Failed to execute all GenTargets (some may have been executed)");
		}
	}

	public boolean run(String[] args) {
		final List<File> genTargetFiles;
		try {
			genTargetFiles = locateGenTargetFiles(args);
		} catch (IncorrectGenTargetException e) {
			logger.severe("Invalid GenTarget file: " + e.getMessage());
			// Failed to locate the GenTargets. It also means that corresponding log files
			// could not be created.
			return false;
		}
		List<GenTarget> targets;
		try {
			targets = loadGenTargets(genTargetFiles);
		} catch (IncorrectGenTargetException e) {
			// GenTarget file exists, but it's invalid. The error was logged by the
			// corresponding GenTarget-specific
			// logger
			return true;
		}
		logger.info("GenTargets to execute: " + genTargetNames(targets));
		logger.info("Adding prerequisities and determinining execution order");
		try {
			targets = GenTargetUtils.resolveTargetList(targets);
		} catch (GenerationException e) {
			logger.log(Level.SEVERE, "Unable to resolve GenTargets");
			e.printStackTrace();
			return true;
		}
		logger.info("Final execution schedule: " + genTargetNames(targets));
		GenTemplateTraces allTraces = null;
		boolean fail = false;
		for (Handler handler : logger.getHandlers()) {
			handler.flush();
		}
		for (GenTarget target : targets) {
			Logger genTargetLogger = getLogger(target);
			try {
				allTraces = GenTargetUtils.execute(genTargetLogger, true, false, allTraces, target);
			} catch (GenerationException e) {
				// GenTarget-specific exception, can happen when the source model is invalid,
				// something expected
				genTargetLogger.log(Level.SEVERE, "GenTarget " + target.getName() + " failed", e);
				fail = false; // because it's a GenTarget-specific exception
			} catch (Exception e) {
				// Something unexpected
				genTargetLogger.log(ABNORMAL_EXIT, "GenTarget " + target.getName() + " failed", e);
				fail = true;
			} finally {
				for (Handler handler : genTargetLogger.getHandlers()) {
					handler.flush();
					if (handler instanceof FileHandler) {
						handler.close();
					}
				}
			}
		}
		if (fail) {
			logger.severe("One or more GenTemplates failed");
		} else {
			logger.info("All GenTemplates executed successfully");
		}
		return !fail;
	}

	private List<GenTarget> loadGenTargets(List<File> genTargetFiles) throws IncorrectGenTargetException {
		ArrayList<GenTarget> targets = Lists.newArrayList();
		for (File file : genTargetFiles) {
			try {
				targets.add(GenTargetUtils.load(file));
			} catch (IncorrectGenTargetException e) {
				Logger genTargetLogger = getLogger(file.getName(), file);
				genTargetLogger.severe(e.getMessage());
				for (Handler handler : genTargetLogger.getHandlers()) {
					handler.flush();
					if (handler instanceof FileHandler) {
						handler.close();
					}
				}
				throw e;
			}
		}
		return targets;
	}

	private String genTargetNames(Collection<GenTarget> targets) {
		String names = "";
		for (GenTarget target : targets) {
			names += ", " + target.getName();
		}
		return names.substring(2);
	}

	private List<File> locateGenTargetFiles(String[] genTargetFileLocations) throws IncorrectGenTargetException {
		ArrayList<File> targets = Lists.newArrayList();
		File baseDir = new File(".");
		for (String genTargetFile : genTargetFileLocations) {
			targets.add(getGenTargetFile(genTargetFile, baseDir));
		}
		return targets;
	}

	private File getGenTargetFile(String genTargetFileLocation, File baseDir) throws IncorrectGenTargetException {
		File file = new File(genTargetFileLocation);
		if (file.exists()) {
			return file;
		}
		file = GenTargetUtils.getGenTargetFile(genTargetFileLocation, baseDir);
		if (file.exists()) {
			return file;
		}
		logger.severe("GenTarget file does not exist: " + genTargetFileLocation);
		throw new IncorrectGenTargetException("GenTarget file does not exist: " + genTargetFileLocation);
	}

	private Logger createLogger() {
		return createLogger("RepreZen Code Generation");
	}

	private Logger getLogger(GenTarget genTarget) {
		return getLogger(genTarget.getName(), GenTargetUtils.getGenTargetFile(genTarget));
	}

	private Logger getLogger(String genTargetName, File genTargetFile) {
		Logger logger = createLogger("RepreZen Code Generation: " + genTargetName);
		try {
			String logFile = genLogSupport.getLogFile(genTargetFile);
			new File(logFile).delete();
			Handler handler = new FileHandler(logFile);
			handler.setFormatter(new Formatter() {

				@Override
				public String format(LogRecord record) {
					String serialized = "";
					try {
						serialized = genLogSupport.toWriteString(record);
					} catch (JsonProcessingException e) {
						GeneratorLauncher.this.logger.info("Error during LogRecord serialization: " + e.getMessage());
					}
					return serialized;
				}
			});
			logger.addHandler(handler);
		} catch (Exception e) {
			GeneratorLauncher.this.logger.info("Error during FileHandler setup for the logger: " + e.getMessage());
		}
		return logger;
	}

	private Logger createLogger(String loggerName) {
		Logger logger = Logger.getLogger(loggerName);
		SimpleFormatter formatter = new SimpleFormatter() {
			@Override
			public synchronized String format(LogRecord record) {
				if (record.getLevel() != Level.SEVERE) {
					return record.getMessage() + System.lineSeparator();
				} else {
					return super.format(record);
				}
			}
		};
		Handler handler = new StreamHandler(System.out, formatter);
		logger.addHandler(handler);
		logger.setUseParentHandlers(false);
		return logger;
	}

	public static Level ABNORMAL_EXIT = new AbnormalLevel();

	private static class AbnormalLevel extends Level {
		private static final long serialVersionUID = 8609272102720116018L;

		protected AbnormalLevel() {
			super("ABNORMAL", Level.SEVERE.intValue() + 100);
		}
	}
}