package com.reprezen.genflow.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.openapi.OpenApiDocument;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetBuilder;
import com.reprezen.genflow.api.target.GenTargetUtils;
import com.reprezen.genflow.api.template.AbstractGenTemplate;
import com.reprezen.genflow.api.template.GenTemplateDependency.GenTemplateDependencyType;
import com.reprezen.genflow.api.template.GenTemplateRegistry;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.kaizen.oasparser.model3.OpenApi3;

import io.swagger.models.Swagger;

public class GenTool {
	public static void main(String[] args) throws IOException, GenerationException {
		String glob = null;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		if (args.length > 0 && args[0].trim().length() > 0) {
			glob = args[0].trim();
		}
		while (true) {
			if (glob == null) {
				System.out.print("Glob pattern to select GenTemplates: ");
				glob = in.readLine().trim();
				if (glob == null || glob.length() == 0) {
					break;
				}
			}
			if (!glob.matches("[*?\\[\\]]")) {
				glob = "*" + glob + "*";
			}
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
			List<IGenTemplate> matches = GenTemplateRegistry.getGenTemplates().stream()
					.filter(t -> matcher.matches(Paths.get(t.getId()))).sorted((a, b) -> a.getId().compareTo(b.getId()))
					.collect(Collectors.toList());
			if (matches.size() == 0) {
				System.out.println("No matching gentemplates");
				glob = null;
				continue;
			}
			int doGen;
			do {
				for (int i = 0; i < matches.size(); i++) {
					IGenTemplate template = matches.get(i);
					System.out.printf("[%d] %s [aka: %s]\n", i + 1, template.getId(),
							getAkas(template).stream().collect(Collectors.joining(", ")));
				}
				doGen = 0;
				String response = null;
				do {
					System.out.print("Selection to execute: ");
					response = in.readLine().trim();
					if (response == null || response.length() == 0) {
						doGen = 0;
						glob = null;
						break;
					}
					if (response.matches("^[0-9]+$")) {
						int num = Integer.parseInt(response);
						if (num > 0 && num <= matches.size()) {
							doGen = num;
							break;
						}
					}
					System.out.println("Please indicate the number one of the GenTemplates by its number.");
					doGen = -1;
				} while (doGen == -1);
				if (doGen > 0) {
					exec(GenTemplateRegistry.getGenTemplate(matches.get(doGen - 1).getId()));
				}
			} while (doGen > 0);
		}
		System.out.println("All done");
	}

	private static List<String> getAkas(IGenTemplate template) throws GenerationException {
		return template instanceof AbstractGenTemplate ? ((AbstractGenTemplate) template).getAlsoKnownAsIds()
				: Collections.emptyList();
	}

	private static void exec(IGenTemplate template) throws GenerationException, IOException {
		String sourceFileName = getSourceFileName(template);
		if (sourceFileName == null) {
			System.err.println("No sample model file available for that GenTarget");
			return;
		}
		URL sourceModel = GenTool.class.getResource("models/" + sourceFileName);
		String sourceExt = sourceFileName.substring(sourceFileName.lastIndexOf("."));
		File sourceFile = File.createTempFile("genflow", sourceExt);
		sourceFile.deleteOnExit();
		Files.write(sourceFile.toPath(), IOUtils.toString(sourceModel.openStream()).getBytes());
		System.out.println(
				"Executing: " + template.getId() + " with source " + sourceFile.toString() + "(auto delete on exit)");
		Path dir = Files.createTempDirectory("genflow");
		System.out.println("Generating in dir " + dir.toString() + " (auto delete on exit)");
		Runtime.getRuntime().addShutdownHook(new Thread(() -> rmdir(dir)));
		GenTarget target = GenTargetBuilder.get().forGenTemplate(template) //
				.withBaseDir(dir.toFile()) //
				.withOutputDir(".") //
				.withPrimarySource(sourceFile) //
				.build();
		GenTargetUtils.execute(null, false, false, null, target);
	}

	private static String getSourceFileName(IGenTemplate template) throws GenerationException {
		String sourceType = template.getDependencies().stream()
				.filter(d -> d.getType() == GenTemplateDependencyType.PRIMARY_SOURCE).map(d -> d.getInfo()).findFirst()
				.orElse(null);
		if (Swagger.class.getName().equals(sourceType)) {
			return "PetStoreV2.yaml";
		} else if (OpenApi3.class.getName().equals(sourceType)) {
			return "PetStoreV3.yaml";
		} else if (OpenApiDocument.class.getName().equals(sourceType)) {
			return "PetStoreV3.yaml";
		} else {
			return null;
		}
	}

	private static void rmdir(Path dir) {
		try {
			Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					file.toFile().delete();
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					dir.toFile().delete();
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
		}
	}
}
