package com.reprezen.genflow.tools;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
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
import com.reprezen.rapidml.ZenModel;

import io.swagger.models.Swagger;

public class GenTool {

	private TextPrompt textPrompt = new TextPrompt();

	public static void main(String[] args) throws IOException, GenerationException {
		new GenTool().run(args.length > 0 ? args[0] : null);
	}

	private void run(String initGlob) throws IOException {
		Optional<String> glob = Optional.ofNullable(initGlob);
		while (true) {
			if (!glob.isPresent()) {
				glob = textPrompt.getString("Glob pattern to select GenTemplates: ", null);
			}
			if (!glob.isPresent()) {
				break;
			}
			// all glob matches are unanchored
			glob = glob.map(String::toLowerCase)
					.map(g -> (g.startsWith("*") ? "" : "*") + g + (g.endsWith("*") ? "" : "*"));
			PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob.get());
			List<AbstractGenTemplate> matches = GenTemplateRegistry.getGenTemplates().stream()
					.filter(t -> matcher.matches(Paths.get(t.getId().toLowerCase())))
					.sorted((a, b) -> a.getId().compareTo(b.getId())).map(t -> {
						return (AbstractGenTemplate) t;
					}).collect(Collectors.toList());
			if (matches.size() == 0) {
				System.out.println("No matching gentemplates");
				glob = Optional.empty();
				continue;
			}
			List<String> choices = matches.stream().<Optional<String>>map(t -> {
				try {
					return Optional.of(String.format("%s [aka: %s]", t.getId(),
							t.getAlsoKnownAsIds().stream().collect(joining(", "))));
				} catch (Exception e) {
					return Optional.<String>empty();
				}
			}).filter(Optional::isPresent).map(Optional::get).collect(toList());
			Optional<Integer> choice = textPrompt.getChoice("Select GenTemplate to execute: ", choices);
			choice.ifPresent(i -> exec(matches.get(i)));
			if (!choice.isPresent()) {
				glob = Optional.empty();
			}
		}
		System.out.println("All done");

	}

	private void exec(IGenTemplate template) {
		try {
			template = template.newInstance();
			GenTarget target = buildTarget(template);
			File sourceFile = target.getPrimarySource().getPath();
			System.out.println("Executing: " + template.getId() + " with source " + sourceFile.toString()
					+ "(auto delete on exit)");
			System.out.println("Generating in dir " + target.getOutputDir() + " (auto delete on exit)");
			GenTargetUtils.execute(null, false, false, null, target);
		} catch (Throwable t) {
			t.printStackTrace(System.out);
		}
	}

	private GenTarget buildTarget(IGenTemplate template) throws IOException, GenerationException {
		GenTargetInfo target = new GenTargetInfo(template);
		target.setPrimarySource(getDefaultPrimarySource(template));
		setParameterDefaults(target);
		while (true) {
			System.out.print(target.describe());
			Optional<BuildChoice> choice = textPrompt.getChoice("Alter GenTarget: ", BuildChoice.class);
			if (choice.isPresent()) {
				System.out.println(choice.get().name());
				switch (choice.get()) {
				case PRIMARY_SOURCE:
					choosePrimarySource(target);
					break;
				case PARAMETER:
					addParameter(target);
					break;
				}
			} else {
				break;
			}
		}
		return target.build();
	}

	private SourceFile getDefaultPrimarySource(IGenTemplate template) throws GenerationException {
		return getPrimarySourceType(template).map(t -> getDefaultModelFile(t)).orElse(null);
	}

	private Optional<String> getPrimarySourceType(IGenTemplate template) throws GenerationException {
		return template.getDependencies().stream()
				.filter(d -> d.getType().equals(GenTemplateDependencyType.PRIMARY_SOURCE)).findFirst()
				.map(d -> d.getInfo());
	}

	private void setParameterDefaults(GenTargetInfo target) throws GenerationException {
		IGenTemplate template = target.getGenTemplate();
		template.getDependencies().stream().filter(d -> d.getType() == GenTemplateDependencyType.PARAMETER)
				.forEach(d -> {
					if (d.getInfo() != null) {
						JsonNode node;
						try {
							node = mapper.readTree(d.getInfo());
						} catch (IOException e) {
							node = JsonNodeFactory.instance.textNode(d.getInfo());
						}
						target.setParam(d.getName(), mapper.convertValue(node, Object.class));
					}
				});
	}

	private enum ValType implements Supplier<String> {
		STRING("Simple String"), //
		EMPTY_STRING("Empty String"), //
		NUMBER("Numeric value"), //
		BOOLEAN("True/False value (type 'true' or 'false'"), //
		JSON("Serialized JSON Value");

		private String description;

		ValType(String description) {
			this.description = description;
		}

		@Override
		public String get() {
			return description;
		}
	}

	private File lastUsedDirectory = null;

	private void choosePrimarySource(GenTargetInfo target) throws GenerationException, IOException {
		while (true) {
			List<SourceFile> sources = new ArrayList<>();
			IGenTemplate template = target.getGenTemplate();
			sources.add(getDefaultPrimarySource(template));
			if (lastUsedDirectory != null) {
				sources.addAll(scanForModelFiles(lastUsedDirectory, getPrimarySourceType(template)));
			}
			List<String> choices = sources.stream().map(s -> s.toString()).collect(toList());
			choices.add("Something else...");
			Optional<Integer> choice = textPrompt.getChoice("Choose a file: ", choices);
			if (!choice.isPresent()) {
				return;
			}
			if (choice.get() == choices.size() - 1) {
				Optional<String> path = textPrompt.getString("Full file path: ", s -> {
					return new File(s).exists() ? Optional.empty() : Optional.of("No such file");
				});
				if (path.isPresent()) {
					File file = new File(path.get().toString());
					if (file.isDirectory()) {
						System.out.println("Scanning directory for model files");
						lastUsedDirectory = file;
					} else {
						target.setPrimarySource(new SourceFile(SourceType.FILE, path.get()));
						return;
					}
				} else {
					return;
				}
			} else {
				target.setPrimarySource(sources.get(choice.get()));
				return;
			}
		}
	}

	private List<SourceFile> scanForModelFiles(File dir, Optional<String> type) throws IOException {
		if (!type.isPresent()) {
			return Collections.emptyList();
		}
		List<Path> paths = new ArrayList<>();
		Set<String> extensions = new HashSet<>();
		// currently we only support OpenApi v2 & v3 models, so only one possibility
		extensions.add(".yaml");
		extensions.add(".json");
		Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
				String fileName = path.getFileName().toString();
				if (fileName.contains(".")) {
					if (extensions.contains(fileName.substring(fileName.lastIndexOf(".")))) {
						paths.add(path);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
		return paths.stream().map(p -> new SourceFile(SourceType.FILE, p.toString())).collect(toList());
	}

	private static ObjectMapper mapper = new ObjectMapper();

	private void addParameter(GenTargetInfo target) throws IOException, GenerationException {

		Optional<String> name = Optional.empty();
		List<String> names = getParameterNames(target.getGenTemplate());
		if (names.isEmpty()) {
			name = textPrompt.getString("Parameter name: ", null);
		} else {
			names.add("Something else...");
			Optional<Integer> choice = textPrompt.getChoice("Which paramater do you want to set? ", names);
			if (choice.isPresent()) {
				if (choice.get() == names.size() - 1) {
					name = textPrompt.getString("Parameter name: ", null);
				} else {
					name = Optional.of(names.get(choice.get()));
				}
			}
		}
		if (!name.isPresent()) {
			return;
		}
		Optional<ValType> valType = textPrompt.getChoice("How will  you enter the value? ", ValType.class);
		if (!valType.isPresent()) {
			return;
		}
		// don't prompt for an empty string
		Optional<String> value = valType.get() == ValType.EMPTY_STRING ? Optional.of("")
				: textPrompt.getString("Enter value: ", getParamValueValidator(valType.get()));
		if (value.isPresent()) {
			Object val = null;
			switch (valType.get()) {
			case STRING:
				val = value.get();
				break;
			case EMPTY_STRING:
				val = "";
				break;
			case NUMBER: {
				val = value.get().matches("[-+]?[0-9]+") //
						? (Object) Integer.parseInt(value.get())
						: (Object) Double.parseDouble(value.get());
				break;
			}
			case BOOLEAN:
				val = Boolean.parseBoolean(value.get());
				break;
			case JSON:
				val = mapper.readTree(value.get());
				break;
			}
			target.setParam(name.get(), val);
		}
	}

	private List<String> getParameterNames(IGenTemplate template) throws GenerationException {
		return template.getDependencies().stream().filter(d -> d.getType() == GenTemplateDependencyType.PARAMETER)
				.map(d -> d.getName()).collect(toList());
	}

	private Function<String, Optional<String>> getParamValueValidator(ValType type) {
		switch (type) {
		case STRING:
		case EMPTY_STRING:
			return s -> Optional.empty();
		case NUMBER:
			return s -> {
				JsonNode node;
				try {
					node = mapper.readTree(s);
					if (node.isNumber()) {
						return Optional.empty();
					}
				} catch (IOException e) {
				}
				return Optional.of("Invalid number");
			};
		case BOOLEAN:
			return s -> {
				JsonNode node;
				try {
					node = mapper.readTree(s);
					if (node.isBoolean()) {
						return Optional.empty();
					}
				} catch (IOException e) {
				}
				return Optional.of("Type 'true' or 'false'");
			};
		case JSON:
			return s -> {
				try {
					mapper.readTree(s);
					return Optional.empty();
				} catch (IOException e) {
					return Optional.of("Invalid JSON value");
				}
			};
		}
		return s -> Optional.empty();
	}

	private static class GenTargetInfo {
		private IGenTemplate template;
		private SourceFile primarySource = null;
		private Map<String, Object> params = new LinkedHashMap<>();

		public GenTargetInfo(IGenTemplate template) {
			this.template = template;
		}

		public IGenTemplate getGenTemplate() {
			return template;
		}

		public void setPrimarySource(SourceFile primarySource) {
			this.primarySource = primarySource;
		}

		public void setParam(String name, Object value) {
			params.put(name, value);
		}

		public String describe() {
			String result = String.format("GenTarget for %s\n", template.getId());
			result += String.format("  Primary Source: %s\n",
					primarySource != null ? primarySource.toString() : "(not set)");
			result += "  Parameters: ";
			result += params.isEmpty() ? "(none set)\n" : "\n";
			for (Entry<String, Object> param : params.entrySet()) {
				Object value = param.getValue();
				result += String.format("    %s [%s]: %s\n", param.getKey(),
						value != null ? value.getClass().getSimpleName() : "", String.valueOf(value));
			}
			return result;
		}

		public GenTarget build() throws IOException {
			GenTargetBuilder builder = GenTargetBuilder.get().forGenTemplate(template);
			if (primarySource != null) {
				builder.withPrimarySource(primarySource.resolve());
			}
			for (Entry<String, Object> param : params.entrySet()) {
				builder.withParameter(param.getKey(), param.getValue());
			}
			File dir = Files.createTempDirectory("genflow").toFile();
			Runtime.getRuntime().addShutdownHook(new Thread(() -> rmdir(dir.toPath())));
			builder.withBaseDir(dir);
			builder.withOutputDir(".");
			return builder.build();
		}

	}

	private SourceFile getDefaultModelFile(String sourceType) {
		String fileName = null;
		if (Swagger.class.getName().equals(sourceType)) {
			fileName = "PetStoreV2.yaml";
		} else if (OpenApi3.class.getName().equals(sourceType)) {
			fileName = "PetStoreV3.yaml";
		} else if (OpenApiDocument.class.getName().equals(sourceType)) {
			fileName = "PetStoreV3.yaml";
		} else if (ZenModel.class.getName().equals(sourceType)) {
			fileName = "TaxBlaster.rapid";
		}
		if (fileName != null) {
			return new SourceFile(SourceType.DEFAULT, fileName);
		}
		return null;
	}

	private enum BuildChoice implements Supplier<String> {
		PRIMARY_SOURCE("Primary Source"), PARAMETER("Parameter");
		private String description;

		BuildChoice(String description) {
			this.description = description;
		}

		@Override
		public String get() {
			return description;
		}
	}

	private enum SourceType {
		DEFAULT, FILE
	};

	private static class SourceFile {

		SourceType type;
		String path;

		public SourceFile(SourceType type, String path) {
			this.type = type;
			this.path = path;
		}

		public File resolve() throws IOException {
			switch (type) {
			case DEFAULT:
				try (InputStream in = GenTool.class.getResourceAsStream("defaultModels/" + path)) {
					File file = Files.createTempFile("genflow", path.substring(path.lastIndexOf("."))).toFile();
					file.deleteOnExit();
					Charset utf8 = Charset.forName("UTF-8");
					FileUtils.write(file, IOUtils.toString(in, utf8), utf8);
					return file;
				}
			case FILE:
				return new File(path);
			}
			return null;
		}

		@Override
		public String toString() {
			return type.name() + ": " + path;
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
