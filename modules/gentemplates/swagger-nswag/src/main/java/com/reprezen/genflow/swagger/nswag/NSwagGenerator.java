package com.reprezen.genflow.swagger.nswag;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.reprezen.genflow.api.GenerationException;

public class NSwagGenerator {

	private Config config;

	public static boolean isPlatformSupported() {
		String os = System.getProperty("os.name");

		return os != null && os.startsWith("Windows");
	}

	public NSwagGenerator(Config config) {
		this.config = config;
	}

	public void generate(File nswagConfigFile) throws GenerationException {
		try {
			checkNSwag();
			runNSwag(nswagConfigFile);
		} catch (IOException | InterruptedException | GenerationException e) {
			throw new GenerationException(
					"Please install or upgrade NSwag, or specify correct location of NSwag program, in order to use this GenTemplate",
					e);
		}
	}

	private void checkNSwag() throws IOException, GenerationException, InterruptedException {
		Process p = null;
		try {
			p = execNSwag("version");

			List<String> stdout = null;
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				stdout = reader.lines().collect(Collectors.toList());
			}

			String version = p.exitValue() == 0 ? parseVersionFromCommandOutput(stdout) : null;
			if (version == null) {
				version = parseVersionFromHeader(stdout);
			}
			if (version != null) {
				String[] parts = StringUtils.split(version, '.');
				Integer major = Integer.valueOf(parts[0]);
				if (major == 8 || major == 9) {
					return;
				}
			}
			throw new GenerationException("NSwag version 8.x or 9.x is required; version " + version + " installed");
		} finally {
			if (p != null) {
				p.getInputStream().close();
				p.getErrorStream().close();
			}
		}
	}

	private void runNSwag(File nswagConfigFile) throws IOException, InterruptedException, GenerationException {
		Process p = null;
		try {
			p = execNSwag("run", nswagConfigFile.getAbsolutePath());
			String processOutput = "";
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
				processOutput = reader.lines().collect(Collectors.joining("\n"));
			}
			System.out.println(processOutput);
		} finally {
			if (p != null) {
				p.getInputStream().close();
				p.getErrorStream().close();
			}
		}
	}

	private Process execNSwag(String... args) throws InterruptedException, GenerationException, IOException {
		String nswag = config.getnSwagPath();
		if (nswag == null || nswag.isEmpty()) {
			nswag = Config.getNSwagLocationDefault();
		}
		if (nswag == null || nswag.isEmpty()) {
			nswag = "nswag";
		} else if (new File(nswag).isDirectory()) {
			nswag = new File(new File(nswag), "nswag").getPath();
		}
		String[] cmdLine = new String[args.length + 1];
		cmdLine[0] = nswag;
		System.arraycopy(args, 0, cmdLine, 1, args.length);
		System.out.println(Arrays.asList(cmdLine));
		Process p = null;
		p = new ProcessBuilder(cmdLine).redirectErrorStream(true).start();
		p.getOutputStream().close();
		p.waitFor();
		return p;
	}

	private static Pattern toolchainPat = Pattern.compile("toolchain\\s+v(\\d+([.]\\d+){3})");

	private String parseVersionFromHeader(List<String> stdout) {
		for (String line : stdout) {
			Matcher matcher = toolchainPat.matcher(line);
			if (matcher.find()) {
				return matcher.group(1);
			}
		}
		return null;
	}

	private static Pattern versionPat = Pattern.compile("NSwag version:\\s*(.+)");

	private String parseVersionFromCommandOutput(List<String> stdout) {
		for (String line : stdout) {
			Matcher matcher = versionPat.matcher(line);
			if (matcher.matches()) {
				return matcher.group(1);
			}
		}
		return null;
	}
}
