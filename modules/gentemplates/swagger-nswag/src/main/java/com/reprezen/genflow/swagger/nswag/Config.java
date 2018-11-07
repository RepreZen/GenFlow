package com.reprezen.genflow.swagger.nswag;

import static com.reprezen.genflow.swagger.nswag.Config.Output.CS_CLIENT;
import static com.reprezen.genflow.swagger.nswag.Config.Output.CS_SERVER;
import static com.reprezen.genflow.swagger.nswag.Config.Output.TS_CLIENT;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;

import io.swagger.models.Swagger;

// N.B. Suppress any fields that should NOT appear in gentarget files using @JsonIgnore.
// Currently, there are no such fields, and this setting makes it impossible to forget
// to mark a field for JSON processing.
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
public class Config {

	/*
	 * Most of the member variables are part of the "run" file that is produced to
	 * drive NSwag. The @NSwagParam annotation captures which "outputs" the
	 * parameter applies to, as well as documentation and default value. To get
	 * around limitations in annotation member types, default values appear as
	 * strings, but any value that begins with "JSON:" is parsed to yield a JSON
	 * object, and that object is used.
	 * 
	 * This class is capable of doing three important and related things:
	 * 
	 * 1. Define parameter definitions for a gentemplate for whichever output the
	 * gentemplate is for.
	 * 
	 * 2. Create an instance based the values in a gentarget file.
	 * 
	 * 3. Create a run file for NSwag, based on the output of the gentemplate.
	 */

	private String nSwagPath;

	@NSwagParam(output = CS_CLIENT, //
			description = "The exception class (default 'SwaggerException', may use '{controller}' placeholder).", //
			defaultValue = "SwaggerException")
	private String exceptionClass;

	@NSwagParam(output = { CS_CLIENT, TS_CLIENT }, //
			description = "Specifies whether generate client classes.", //
			defaultValue = "JSON:true")
	private Boolean generateClientClasses;

	@NSwagParam(output = { CS_CLIENT, TS_CLIENT }, //
			description = "Specifies whether generate interfaces for the client classes.", //
			defaultValue = "JSON:false")
	private Boolean generateClientInterfaces;

	@NSwagParam(output = { CS_CLIENT, TS_CLIENT }, //
			description = "Specifies whether to generate DTO classes.", //
			defaultValue = "JSON:true")
	private Boolean generateDtoTypes;

	@NSwagParam(output = CS_CLIENT, //
			description = "Specifies whether to call CreateHttpClientAsync on the base class to create a new HttpClient.", //
			defaultValue = "JSON:false")
	private Boolean useHttpClientCreationMethod;

	@NSwagParam(output = CS_CLIENT, //
			description = "", // TODO
			defaultValue = "JSON:false")
	private Boolean useHttpRequestMessageCreationMethod;

	@NSwagParam(output = CS_CLIENT, //
			description = "Specifies whether to generate contracts output (interface and models in a separate file set with the ContractsOutput parameter).", //
			defaultValue = "JSON:false")
	private String generateContractsOutput;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER, TS_CLIENT }, //
			description = "The class name of the generated client.", //
			csServerDefault = "{controller}", //
			csClientDefault = "{controller}Client", //
			tsClientDefault = "{controller}Client")
	private String className;

	@NSwagParam(output = TS_CLIENT, //
			description = "The TypeScript module name ('' for no module).", //
			defaultValue = "")
	private String moduleName;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The namespace of the generated classes ('' for no namespace in TypeScript client).", //
			defaultValue = "MyNamespaceX", //
			tsClientDefault = "")
	private String namespace;

	@NSwagParam(output = TS_CLIENT, //
			description = "The target TypeScript version.", //
			defaultValue = "1.8")
	private String typeScriptVersion;

	@NSwagParam(output = TS_CLIENT, //
			description = "The type of the asynchronism handling ('JQueryCallbacks', 'JQueryPromises', 'AngularJS', 'Angular2', 'Fetch', 'Aurelia')", //
			defaultValue = "JQueryCallbacks")
	private String template;

	@NSwagParam(output = TS_CLIENT, //
			description = "The promise type ('Promise' or 'QPromise').", //
			defaultValue = "Promise")
	private String promiseType;

	@NSwagParam(output = TS_CLIENT, //
			description = "Specifies whether DTO exceptions are wrapped in a SwaggerException instance.", //
			defaultValue = "JSON:false")
	private Boolean wrapDtoExceptions;

	@NSwagParam(output = TS_CLIENT, //
			description = "The base class of the generated client classes (optional, must be imported or implemented in the extension code).", //
			defaultValue = "JSON:null")
	private String clientBaseClass;

	@NSwagParam(output = TS_CLIENT, //
			description = "Call 'transformOptions' on the base class or extension class.", //
			defaultValue = "JSON:false")
	private Boolean useTransformOptionsMethod;

	@NSwagParam(output = TS_CLIENT, //
			description = "Call 'transformResult' on the base class or extension class.", //
			defaultValue = "JSON:false")
	private Boolean useTransformResultMethod;

	@NSwagParam(output = TS_CLIENT, //
			description = "Specifies whether to mark optional properties with '?'.", // TODO
			defaultValue = "JSON:false")
	private Boolean markOptionalProperties;

	@NSwagParam(output = TS_CLIENT, //
			description = "The type style.", //
			defaultValue = "Class")
	private String typeStyle;

	@NSwagParam(output = TS_CLIENT, //
			description = "The list of extended classes.", //
			defaultValue = "JSON:[]")
	private List<String> extendedClasses;

	@NSwagParam(output = TS_CLIENT, //
			description = "The extension code (string or file path", //
			defaultValue = "JSON:null")
	private String extensionCode;

	@NSwagParam(output = TS_CLIENT, //
			description = "Overrides the service host of the web document (optional, use '.' to remove the hostname).", //
			defaultValue = "JSON:null")
	private String serviceHost;

	@NSwagParam(output = TS_CLIENT, //
			description = "Overrides the allowed schemes of the web service (optional array, 'http', 'https', 'ws', 'wss').", //
			defaultValue = "JSON:null")
	private List<String> serviceSchemes;

	@NSwagParam(output = CS_SERVER, //
			description = "Additional namespaces to appear in 'using' directives", //
			defaultValue = "JSON:[\"System.Web.Http\"]")
	private List<String> additionalNamespaceUsages;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "Specifies whether a required property must be defined in JSON (sets Required.Always when the property is required).", //
			defaultValue = "JSON:true")
	private Boolean requiredPropertiesMustBeDefined;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The date .NET type.", //
			defaultValue = "DateTime")
	private String dateType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER, TS_CLIENT }, //
			description = "The date time type ('Date', 'MomentJS', 'string' for TypeScript Client).", //
			defaultValue = "DateTime", //
			tsClientDefault = "Date")
	private String dateTimeType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The time .NET type.", //
			defaultValue = "TimeSpan")
	private String timeType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The time span .NET type.", //
			defaultValue = "TimeSpan")
	private String timespanType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The generic array .NET type.", //
			csServerDefault = "IEnumerable", //
			csClientDefault = "ObservableCollection")
	private String arrayType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The generic dictionary .NET type.", //
			defaultValue = "Dictionary")
	private String dictionaryType;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER }, //
			description = "The CSharp class style, 'Poco' or 'Inpc'.", //
			defaultValue = "Inpc")
	private String classStyle;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER, TS_CLIENT }, //
			description = "The operation generation mode, one of: 'SingleClientFromOperationId', 'MultipleClientsFromPathSegments', 'MultipleClientsFromOperationId'", //
			defaultValue = "MultipleClientsFromOperationId")
	private String operationGenerationMode;

	@NSwagParam(output = { CS_CLIENT, CS_SERVER, TS_CLIENT }, //
			description = "Specifies whether to generate default values for properties (may generate CSharp 6 code, default: true).", //
			defaultValue = "JSON:true")
	private Boolean generateDefaultValues;

	private static ObjectMapper jsonMapper = new ObjectMapper();
	static {
		jsonMapper.setSerializationInclusion(Include.NON_NULL);
	}
	private static ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

	public static void defineGenTemplateParameters(GenTemplate<?> gt, Output output) throws GenerationException {
		gt.define(gt.parameter() //
				.named("nSwagPath") //
				.withDescription("Location of NSwag command line tool (nswag.exe). May be omitted if NSwag is in PATH",
						"or, when run from within API Studio, when the location is set in Preferences (RepreZen/Code Generation)."));
		for (Field field : getNSwagConfigFields(output)) {
			gt.define( //
					gt.parameter() //
							.named(field.getName()) //
							.withDescription(getFieldDescription(field)) //
							.withDefault(getFieldDefault(field, output)));
		}
	}

	public static Config fromContext(GenTemplateContext context) {
		return yamlMapper.convertValue(context.getGenTargetParameters(), Config.class);
	}

	public File getNSwagRunFile(Swagger swagger, Output output, File outputFile) throws IOException {
		List<Field> serverFields = getNSwagConfigFields(output);
		return buildNSwagRunFile(swagger, getSectionName(output), serverFields, outputFile);
	}

	private String getSectionName(Output output) {
		switch (output) {
		case CS_SERVER:
			return "swaggerToCSharpController";
		case CS_CLIENT:
			return "swaggerToCSharpClient";
		case TS_CLIENT:
			return "swaggerToTypeScriptClient";
		default:
			throw new IllegalArgumentException("Invalid Output option supplied by GenTemplate");
		}
	}

	public static final String NSWAG_TOOL_LOCATION = "com.reprezen.genflow.nswag.location";

	public static String getNSwagLocationDefault() {
		return System.getProperty(NSWAG_TOOL_LOCATION);
	}

	private static List<Field> getNSwagConfigFields(Output output) {
		List<Field> fields = Lists.newArrayList();
		for (Field field : Config.class.getDeclaredFields()) {
			NSwagParam param = field.getAnnotation(NSwagParam.class);
			if (param != null && paramIsForOutput(param, output)) {
				fields.add(field);
			}
		}
		return fields;
	}

	private static boolean paramIsForOutput(NSwagParam param, Output output) {
		return Arrays.asList(param.output()).contains(output);
	}

	private static String getFieldDescription(Field field) {
		return field.getAnnotation(NSwagParam.class).description();
	}

	private static Object getFieldDefault(Field field, Output output) throws GenerationException {
		NSwagParam param = field.getAnnotation(NSwagParam.class);
		String value = "";
		switch (output) {
		case CS_CLIENT:
			value = param.csClientDefault();
			break;
		case CS_SERVER:
			value = param.csServerDefault();
			break;
		case TS_CLIENT:
			value = param.tsClientDefault();
			break;
		default:
		}
		if (value.isEmpty()) {
			value = param.defaultValue();
		}
		if (value.startsWith("JSON:")) {
			try {
				return jsonMapper.readValue(value.substring("JSON:".length()), Object.class);
			} catch (IOException e) {
				throw new GenerationException("Internal GenTemplate error", e);
			}
		} else {
			return value;
		}
	}

	private File buildNSwagRunFile(Swagger swagger, String sectionName, List<Field> sectionFields, File outputFile)
			throws IOException {
		File swaggerFile = writeSwaggerTempFile(swagger);
		File nswagFile = File.createTempFile("NSwagConfig", ".json");
		nswagFile.deleteOnExit();
		ObjectNode root = jsonMapper.createObjectNode();
		mkNodePath(root, "swaggerGenerator", "fromSwagger").put("url", swaggerFile.getAbsolutePath());
		mkNodePath(root, "codeGenerators").set(sectionName, getRunFileObject(sectionFields, outputFile));
		// ZEN-4270 Safe to use: existed in Apache Commins IO v.2.2
		// See
		// https://commons.apache.org/proper/commons-io/javadocs/api-2.2/org/apache/commons/io/FileUtils.html#writeStringToFile(java.io.File,%20java.lang.String)
		FileUtils.writeStringToFile(nswagFile, jsonMapper.writeValueAsString(root));
		return nswagFile;
	}

	private File writeSwaggerTempFile(Swagger swagger) throws IOException {
		File file = File.createTempFile("NSwagInput", ".json");
		file.deleteOnExit();
		// ZEN-4270 Safe to use: existed in Apache Commins IO v.2.2
		// See
		// https://commons.apache.org/proper/commons-io/javadocs/api-2.2/org/apache/commons/io/FileUtils.html#writeStringToFile(java.io.File,%20java.lang.String)
		FileUtils.writeStringToFile(file, jsonMapper.writeValueAsString(swagger));
		return file;
	}

	private ObjectNode mkNodePath(ObjectNode parent, String... path) {
		for (String component : path) {
			ObjectNode child = yamlMapper.createObjectNode();
			parent.set(component, child);
			parent = child;
		}
		return parent;
	}

	private ObjectNode getRunFileObject(List<Field> fields, File outputFile) {
		ObjectNode node = yamlMapper.createObjectNode();
		for (Field field : fields) {
			try {
				Object value = field.get(this);
				if (value != null) {
					node.set(field.getName(), jsonMapper.convertValue(value, JsonNode.class));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
			}
		}
		node.put("output", outputFile.getAbsolutePath());
		return node;
	}

	public String getnSwagPath() {
		return nSwagPath;
	}

	public enum Output {
		CS_CLIENT, CS_SERVER, TS_CLIENT,
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface NSwagParam {

		public Output[] output() default {};

		public String description() default "";

		public String defaultValue() default "";

		public String csClientDefault() default "";

		public String csServerDefault() default "";

		public String tsClientDefault() default "";
	}
}
