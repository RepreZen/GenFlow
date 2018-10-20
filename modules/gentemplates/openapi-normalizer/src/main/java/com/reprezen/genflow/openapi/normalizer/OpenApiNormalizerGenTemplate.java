package com.reprezen.genflow.openapi.normalizer;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.OPENAPI3_MODEL_VERSION;
import static com.reprezen.genflow.api.normal.openapi.ObjectType.SWAGGER_MODEL_VERSION;
import static com.reprezen.genflow.common.CommonParams.FOLD_MULTILINE_YAML_STRINGS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.ObjectType;
import com.reprezen.genflow.api.normal.openapi.Option;
import com.reprezen.genflow.api.normal.openapi.Option.OptionType;
import com.reprezen.genflow.api.normal.openapi.Option.OrderingScheme;
import com.reprezen.genflow.api.normal.openapi.Option.RetentionScopeType;
import com.reprezen.genflow.api.openapi.OpenApiDocument;
import com.reprezen.genflow.api.openapi.OpenApiGenTemplate;
import com.reprezen.genflow.api.openapi.OpenApiSource;
import com.reprezen.genflow.api.source.ISource;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.openapi.normalizer.NormalizerParameters.BadParameterException;

import io.swagger.util.Json;

public class OpenApiNormalizerGenTemplate extends OpenApiGenTemplate {

	public static final String OUTPUTS_OPTION = "OUTPUTS";

	public static enum OutputOptions {
		YAML, JSON, BOTH;
		public static OutputOptions fromString(String optName) {
			try {
				return OutputOptions.valueOf(optName);
			} catch (IllegalArgumentException e) {
			}
			return null;
		}
	}

	@Override
	protected void configure() throws GenerationException {
		alsoKnownAs("com.modelsolv.reprezen.gentemplates.swaggernorm.SwaggerNormalizerGenTemplate");
		define(primarySource().ofType(CustomSwaggerSource.class));
		define(parameter().named(OptionType.INLINE.name()).withDescription("List of model object types to inline.",
				"For v2 models, allowed values are PATH, DEFINITION, PARAMETER, and RESPONSE.",
				"For v3 models, allowed values are PATH, SCHEMA, RESPONSE, PARAMETER, EXAMPLE,",
				"REQUEST_BODY, HEADER, SECURITY_SCHEME, LINK, and CALLBACK.",
				"In both cases, ALL means all of those; COMPONENTS means all but PATH; NONE means none of them.",
				"Note that recursive objects cannot be fully inlined. Each explicit reference to such an",
				"object will be inlined, but recursive references encocuntered during that inlining will be",
				"retained as references, forcing the object to be retained in the normalized model.")
				.withDefault(Arrays.asList(ObjectType.PARAMETER, ObjectType.RESPONSE)));
		define(parameter().named(OptionType.RETAIN.name())
				.withDescription("List of model object types to retain. Allowed values are same as for INLINE.",
						"As for INLINE, ALL means all types, and COMPONENTS means all but PATH.",
						"PATH_OR_COMPONENT means PATH if the model has any paths, and COMPONENTS otherwise.",
						"Note that any object for which there is a non-inlined reference will be retained regardless",
						"of this setting. If you see retained objects that you didn't expect, you may have to adjust",
						"your INLINE setting.")
				.withDefault("ALL"));
		define(parameter().named(OptionType.RETENTION_SCOPE.name())
				.withDescription("Files whose unreferenced objects are eligible for retention",
						"ROOTS means the top-level file and files specified in ADDITIONAL_FILES.",
						"ALL means ROOTS plus any file that is loaded to resolve references.")
				.withDefault(RetentionScopeType.ROOTS));
		define(parameter().named(OptionType.ADDITIONAL_FILES.name()) //
				.withDescription("List of additional files to be treated as top-level.",
						"Each file is specifed as a URL; relative URLs are resolved relative to",
						"the primary source model (NOT this GenTarget configuration file).") //
				.withDefault(Collections.emptyList()));
		define(parameter().named(OptionType.HOIST.name()).withDescription(
				"List of hoisting operations to perform, including MEDIA_TYPE, PARAMETER, and SECURITY_REQUIREMENT.",
				"ALL means all of the above; NONE means none of them.", "This option applies only to v2 models") //
				.withDefault("ALL"));
		define(parameter().named(OptionType.REWRITE_SIMPLE_REFS.name())
				.withDescription(
						"Rewrite 'simple refs' to full refs. E.g. '$ref: Pet' => '$ref: \"#/definitions/Pet\"'",
						"This option applies only to v2 models.")
				.withDefault(true));
		define(parameter().named(OptionType.CREATE_DEF_TITLES.name()) //
				.withDescription("Copy schema definition name to title for definitions without titles")
				.withDefault(false));
		define(parameter().named(OptionType.INSTANTIATE_NULL_COLLECTIONS.name()) //
				.withDescription("Replace null lists and maps with empty ones",
						" This option applies only to v2 models.") //
				.withDefault(true));
		define(parameter().named(OptionType.FIX_MISSING_TYPES.name()) //
				.withDescription("Fill in missing 'object' types", "This option applies only to v2 models.") //
				.withDefault(true));
		define(parameter().named(OptionType.ADD_JSON_POINTERS.name()) //
				.withDescription("Write JSON Pointers as RepreZen vendor extension properties") //
				.withDefault(false));
		define(parameter().named(OptionType.ORDERING.name()) //
				.withDescription("How objects should be ordered in normalized model",
						"AS_DECLARED means leave things as they are in the source model",
						"SORTED means sort obects mostly alphabetically by name; see documentation for full details",
						"Note: ordering is currently enforced only for the root-level file") //
				.withDefault(OrderingScheme.AS_DECLARED.name()));
		define(parameter().named(OptionType.RETAIN_EXTENSION_DATA.name()) //
				.withDescription(
						"Which aspects of the x-reprezen-normalization extension properties to retain in the generated spec. Choose from:",
						"- ORDERING for data recorded to reconstruct AS_DECLARED ordering",
						"- POINTER and FILE for data identifying definitions of certain objects",
						"- TYPE_NAME for property names of defined schema definitions",
						"- BAD_REF for information about unresolvable references", "You may also specify ALL or NONE") //
				.withDefault("NONE"));
		define(parameter().named(OUTPUTS_OPTION) //
				.withDescription(
						"Specify YAML, JSON, or BOTH (the default) to specify which form(s) of output file to produce")
				.withDefault(OutputOptions.BOTH));
		define(parameter().named(OptionType.FIX_X_EXAMPLES.name()) //
				.withDescription(
						"In body parameters with x-examples vendor extension, convert non-text values into json-serialized text.",
						"This overcomes a bug in SwaggerParser that requires these examples to be text.",
						"This option applies only to v2 models.") //
				.withDefault(true));
		define(parameter() //
				.named(FOLD_MULTILINE_YAML_STRINGS) //
				.withDescription("Fold multi-line descriptions in YAML output.") //
				.withDefault(true));
		define(GenTemplateProperty.reprezenProvider());
	}

	@Override
	public String getName() {
		return "KaiZen OpenAPI Normalizer [YAML+JSON]";
	}

	@Override
	public GenTemplate.StaticGenerator<OpenApiDocument> getStaticGenerator() {
		return new Generator(this, context);
	}

	public Option[] getNormalizerOptions() throws BadParameterException {
		return new NormalizerParameters(context.getGenTargetParameters()).getOptions();
	}

	@Override
	public ISource<?> getPrimarySource() throws GenerationException {
		return new CustomSwaggerSource(this);
	}

	public static class CustomSwaggerSource extends OpenApiSource {

		private OpenApiNormalizerGenTemplate genTemplate;

		public CustomSwaggerSource() {
		}

		public CustomSwaggerSource(OpenApiNormalizerGenTemplate genTemplate) {
			this.genTemplate = genTemplate;
		}

		@Override
		protected Option[] getNormalizerOptions() throws GenerationException {
			try {
				Option[] options = genTemplate.getNormalizerOptions();
				return options;
			} catch (BadParameterException e) {
				throw new GenerationException("Failed to collect SwaggerNormalizer options", e);
			}
		}

		@Override
		public Class<?> getValueType() throws GenerationException {
			return super.getValueType();
		}

	}

	public static class Generator extends GenTemplate.StaticGenerator<OpenApiDocument> {

		private static ObjectMapper jsonMapper = Json.mapper();
		private static ObjectMapper yamlMapper = io.swagger.util.Yaml.mapper();

		public Generator(GenTemplate<OpenApiDocument> genTemplate, GenTemplateContext context) {
			super(genTemplate, context);
		}

		boolean outputYaml = false;
		boolean outputJson = false;

		@Override
		public void generate(OpenApiDocument model) throws GenerationException {
			chooseOutputs();
			JsonNode tree = new TreeOrderingFixer(getModelVersion(model)).reorder(model.asJson());
			if (outputYaml) {
				generate(tree, new TreeWriter(yamlMapper, "yaml"), "yaml");
			}
			if (outputJson) {
				generate(tree, new TreeWriter(jsonMapper, "json"), "json");
			}
		}

		private Integer getModelVersion(OpenApiDocument model) {
			return model.isSwagger() ? SWAGGER_MODEL_VERSION : model.isOpenApi3() ? OPENAPI3_MODEL_VERSION : null;
		}

		private void generate(JsonNode tree, TreeWriter writer, String extension) throws GenerationException {
			try {
				Path filePath = getFile(tree, extension).toPath();
				Files.createDirectories(filePath.getParent());
				Files.write(filePath, writer.writeTree(tree, isFoldMultiline()).getBytes());
			} catch (IOException e) {
				throw new GenerationException("Failed to create normalized " + extension.toUpperCase() + " file", e);
			}
		}

		static private Set<String> falseValues = Sets.newHashSet("false", "no");

		private boolean isFoldMultiline() {
			Object foldParam = context.getGenTargetParameters().get(FOLD_MULTILINE_YAML_STRINGS);
			if (foldParam instanceof Boolean) {
				return (Boolean) foldParam;
			} else if (foldParam instanceof String) {
				if (falseValues.contains(((String) foldParam).trim().toLowerCase())) {
					return false;
				}
			}
			return true;
		}

		private File getFile(JsonNode tree, String extension) {
			JsonNode titleNode = tree.at("/info/title");
			// since we haven't parsed this, we can't count on presence of the required
			// title property
			String title = titleNode.asText("Unknown");
			String fileName = String.format("%s.%s", title, extension);
			return new File(context.getOutputDirectory(), fileName);
		}

		private void chooseOutputs() throws GenerationException {
			Object outputs = context.getGenTargetParameters().get(OUTPUTS_OPTION);
			if (outputs == null) {
				outputs = OutputOptions.BOTH.name();
			}
			if (outputs instanceof String) {
				OutputOptions opt = OutputOptions.fromString((String) outputs);
				if (opt != null) {
					switch (opt) {
					case YAML:
						outputYaml = true;
						return;
					case JSON:
						outputJson = true;
						return;
					case BOTH:
						outputYaml = true;
						outputJson = true;
						return;
					default:
					}
				}
			}
			throw new GenerationException(String.format("Invalid OUTPUTS parameter value: %s; use %s, %s, or %s",
					outputs, OutputOptions.YAML, OutputOptions.JSON, OutputOptions.BOTH));
		}
	}

	private static class TreeWriter {
		private ObjectMapper mapper;
		private String outputType;

		public TreeWriter(ObjectMapper mapper, String outputType) {
			this.mapper = mapper;
			this.outputType = outputType;
		}

		public String writeTree(JsonNode tree, boolean foldMultiline) throws GenerationException {
			try {
				if (outputType.equals("yaml")) {
					return writeYamlTree(tree, foldMultiline);
				} else {
					return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tree);
				}
			} catch (JsonProcessingException e) {
				throw new GenerationException("Failed to render normalized output", e);
			}
		}

		private String writeYamlTree(JsonNode tree, boolean foldMultiline) throws JsonProcessingException {
			if (foldMultiline) {
				DumperOptions opts = new DumperOptions();
				opts.setDefaultFlowStyle(FlowStyle.BLOCK);
				Yaml yaml = new Yaml(opts);
				// Note that we are using SnakeYAML directly here. See
				// https://github.com/ModelSolv/RepreZen/pull/1348
				return yaml.dump(mapper.convertValue(tree, Object.class));
			} else {
				return new ObjectMapper(new YAMLFactory()).writeValueAsString(tree);
			}
		}
	}

}
