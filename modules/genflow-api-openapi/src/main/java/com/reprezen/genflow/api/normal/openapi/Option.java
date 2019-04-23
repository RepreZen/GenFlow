package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.HOIST;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.INLINE;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.RETAIN;
import static com.reprezen.genflow.api.normal.openapi.Option.OptionType.RETENTION_SCOPE;

import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Options for normalizer behavior.
 * <p>
 * Trying something a little unconventional here as a way of expressing options,
 * trying get the simplicity of enums with the flexibility of option arguments.
 * <p>
 * Idea is to declare each option not as an ENUM member, but as one or more
 * static methods with constant-style names, along with, optionally, a static
 * final member that represents that option in its "default" configuration. It
 * turns out that a java class can have a member and a (possibly overloaded)
 * method with the same name.
 * <p>
 * So for an option named OPT one could write
 * <code>Option.OPT<code> to refer to the default case, e.g. useful for turning on a boolean option that is normally off; or one can use <code>Option.OPT(args)</code>
 * to customize the option, e.g. for an option that sets a string value.
 */
public class Option {

	private final OptionType type;
	private final Object data;

	protected Option(OptionType type) {
		this(type, true);
	}

	public Option(OptionType type, Object data) {
		this.type = type;
		this.data = data;
	}

	public OptionType getType() {
		return type;
	}

	public Object getData() {
		return data;
	}

	public static Options options(Integer modelVersion, Option... options) {
		return new Options(modelVersion, options);
	}

	public static final Set<ObjectType> ALL_OBJECTS = ImmutableSet.of( //
			ObjectType.PATH, //
			ObjectType.DEFINITION, //
			ObjectType.PARAMETER, //
			ObjectType.RESPONSE, //
			ObjectType.SCHEMA, //
			ObjectType.EXAMPLE, //
			ObjectType.REQUEST_BODY, //
			ObjectType.HEADER, //
			ObjectType.SECURITY_SCHEME, //
			ObjectType.LINK, //
			ObjectType.CALLBACK);

	public static final Set<ObjectType> COMPONENT_OBJECTS = ImmutableSet.of( //
			ObjectType.DEFINITION, //
			ObjectType.PARAMETER, //
			ObjectType.RESPONSE, //
			ObjectType.SCHEMA, //
			ObjectType.EXAMPLE, //
			ObjectType.REQUEST_BODY, //
			ObjectType.HEADER, //
			ObjectType.SECURITY_SCHEME, //
			ObjectType.LINK, //
			ObjectType.CALLBACK);

	public static final Set<ObjectType> NO_OBJECTS = ImmutableSet.of();

	// special marker - when used in an option, must be interpreted by code to mean
	// PATH if it's present, else the contained object types
	public static final Set<ObjectType> PATH_OR_COMPONENTS = ImmutableSet.of( //
			ObjectType.DEFINITION, //
			ObjectType.PARAMETER, //
			ObjectType.RESPONSE, //
			ObjectType.SCHEMA, //
			ObjectType.EXAMPLE, //
			ObjectType.REQUEST_BODY, //
			ObjectType.HEADER, //
			ObjectType.SECURITY_SCHEME, //
			ObjectType.LINK, //
			ObjectType.CALLBACK);

	public static final Option DO_NOT_NORMALIZE = new Option(OptionType.DO_NOT_NORMALIZE);

	public static final Option INLINE_ALL = new Option(INLINE, ALL_OBJECTS);
	// no INLINE_COMPONENTS because it's confusing, as it has the same effect as
	// INLINE_ALL, given that inlining is the
	// only possible treatment of path references
	public static final Option INLINE_PATHS = new Option(INLINE, ObjectType.PATH);
	public static final Option INLINE_DEFINITIONS = new Option(INLINE, ObjectType.DEFINITION);
	public static final Option INLINE_PARAMETERS = new Option(INLINE, ObjectType.PARAMETER);
	public static final Option INLINE_RESPONSES = new Option(INLINE, ObjectType.RESPONSE);
	public static final Option INLINE_SCHEMAS = new Option(INLINE, ObjectType.SCHEMA);
	public static final Option INLINE_NONE = new Option(INLINE, NO_OBJECTS);

	public static final Option RETAIN_ALL = new Option(RETAIN, ALL_OBJECTS);
	public static final Option RETAIN_COMPONENTS = new Option(RETAIN, COMPONENT_OBJECTS);
	public static final Option RETAIN_PATHS = new Option(RETAIN, ObjectType.PATH);
	public static final Option RETAIN_DEFINITIONS = new Option(RETAIN, ObjectType.DEFINITION);
	public static final Option RETAIN_PARAMETERS = new Option(RETAIN, ObjectType.PARAMETER);
	public static final Option RETAIN_RESPONSES = new Option(RETAIN, ObjectType.RESPONSE);
	public static final Option RETAIN_PATHS_OR_COMPONENTS = new Option(RETAIN, PATH_OR_COMPONENTS);

	public static final Option RETENTION_SCOPE_ROOTS = new Option(RETENTION_SCOPE, RetentionScopeType.ROOTS);
	public static final Option RETENTION_SCOPE_ALL = new Option(RETENTION_SCOPE, RetentionScopeType.ALL);

	public static Set<HoistType> ALL_HOIST_TYPES = ImmutableSet.of( //
			HoistType.MEDIA_TYPE, //
			HoistType.PARAMETER, //
			HoistType.SECURITY_REQUIREMENT);
	public static Set<HoistType> NO_HOIST_TYPES = ImmutableSet.of();

	public static final Option HOIST_ALL = new Option(HOIST, ALL_HOIST_TYPES);
	public static final Option HOIST_MEDIA_TYPES = new Option(HOIST, HoistType.MEDIA_TYPE);
	public static final Option HOIST_PARAMETERS = new Option(HOIST, HoistType.PARAMETER);
	public static final Option HOIST_SECURITY_REQUIREMENTS = new Option(HOIST, HoistType.SECURITY_REQUIREMENT);
	public static final Option HOIST_NONE = new Option(HOIST, NO_HOIST_TYPES);

	public static final Option REWRITE_SIMPLE_REFS = new Option(OptionType.REWRITE_SIMPLE_REFS);
	public static final Option CREATE_DEF_TITLES = new Option(OptionType.CREATE_DEF_TITLES);
	public static final Option INSTANTIATE_NULL_COLLECTIONS = new Option(OptionType.INSTANTIATE_NULL_COLLECTIONS);
	public static final Option FIX_MISSING_TYPES = new Option(OptionType.FIX_MISSING_TYPES);
	public static final Option ADD_JSON_POINTERS = new Option(OptionType.ADD_JSON_POINTERS);
	public static final Option FIX_X_EXAMPLES = new Option(OptionType.FIX_X_EXAMPLES);

	public static final Option ORDERING_AS_DECLARED = new Option(OptionType.ORDERING, OrderingScheme.AS_DECLARED);
	public static final Option ORDERING_SORTED = new Option(OptionType.ORDERING, OrderingScheme.SORTED);

	public static final Set<ExtensionData> ALL_EXTENSION_DATA = ImmutableSet.of( //
			ExtensionData.ORDERING, //
			ExtensionData.POINTER, //
			ExtensionData.FILE, //
			ExtensionData.TYPE_NAME, //
			ExtensionData.BAD_REF //
	);
	public static final Set<ExtensionData> NO_EXTENSION_DATA = ImmutableSet.of();

	public static final Option RETAIN_ALL_EXTENSION_DATA = new Option(OptionType.RETAIN_EXTENSION_DATA,
			ALL_EXTENSION_DATA);
	public static final Option RETAIN_NO_EXTENSION_DATA = new Option(OptionType.RETAIN_EXTENSION_DATA,
			NO_EXTENSION_DATA);

	public static final Option[] DO_NOT_NORMALIZE_OPTIONS = new Option[] { DO_NOT_NORMALIZE };

	public static final Option[] DOC_DEFAULT_OPTIONS = new Option[] { //
			new Option(INLINE, ImmutableSet.of(ObjectType.PARAMETER, ObjectType.RESPONSE)), //
			RETAIN_PATHS_OR_COMPONENTS, //
			RETENTION_SCOPE_ROOTS, //
			HOIST_ALL, //
			REWRITE_SIMPLE_REFS, //
			CREATE_DEF_TITLES, //
			INSTANTIATE_NULL_COLLECTIONS, //
			FIX_MISSING_TYPES, //
			ADD_JSON_POINTERS, //
			ORDERING_SORTED, //
			RETAIN_ALL_EXTENSION_DATA, //
			FIX_X_EXAMPLES //
	};

	public static final Option[] LIVE_DOC_DEFAULT_OPTIONS = new Option[] { //
			new Option(INLINE, ImmutableSet.of(ObjectType.PARAMETER, ObjectType.RESPONSE)), //
			RETAIN_PATHS_OR_COMPONENTS, //
			RETENTION_SCOPE_ROOTS, //
			HOIST_ALL, //
			REWRITE_SIMPLE_REFS, //
			CREATE_DEF_TITLES, //
			INSTANTIATE_NULL_COLLECTIONS, //
			FIX_MISSING_TYPES, //
			ADD_JSON_POINTERS, //
			ORDERING_AS_DECLARED, //
			RETAIN_ALL_EXTENSION_DATA, //
			FIX_X_EXAMPLES, //
	};

	public static final Option[] CODEGEN_DEFAULT_OPTIONS = new Option[] { //
			new Option(INLINE, ImmutableSet.of(ObjectType.PARAMETER, ObjectType.RESPONSE)), //
			RETAIN_ALL, //
			RETENTION_SCOPE_ROOTS, //
			HOIST_ALL, //
			REWRITE_SIMPLE_REFS, //
			INSTANTIATE_NULL_COLLECTIONS, //
			FIX_MISSING_TYPES, //
			ORDERING_AS_DECLARED, //
			RETAIN_NO_EXTENSION_DATA, //
			FIX_X_EXAMPLES //
	};

	public static final Option[] MINIMAL_OPTIONS = new Option[] { //
			INLINE_NONE, //
			RETAIN_ALL, //
			RETENTION_SCOPE_ROOTS, //
			HOIST_NONE, //
			ORDERING_AS_DECLARED, //
			RETAIN_NO_EXTENSION_DATA, //
			FIX_X_EXAMPLES, //
	};

	public enum OptionType {
		DO_NOT_NORMALIZE, // specifies that all normalization should be skipped; just return requested
							// type
		INLINE, // specifies what sorts of objects to inline
		RETAIN, // specifies what objects to retain
		RETENTION_SCOPE, // which files are covered by RETAIN option
		ADDITIONAL_FILES, // additional files/urls to treat as top-level
		HOIST, // specifies what to hoist
		REWRITE_SIMPLE_REFS, // rewrite "simple refs" to proper local refs
		CREATE_DEF_TITLES, // set missing schema titles from definition names
		INSTANTIATE_NULL_COLLECTIONS, // replace null lists & maps with empties
		FIX_MISSING_TYPES, // fill in missing "object" types
		ADD_JSON_POINTERS, // write JSON Pointers as a Vendor Extension property
		ORDERING, // order in which items appear in normalized output
		RETAIN_POSITION_VALUES, // whether to retain position values
		FIX_X_EXAMPLES, // whether to convert non-text response examples to text
		RETAIN_EXTENSION_DATA, // vendor-extension data to retain
		DEFER_EXTENSION_DATA_REMOVAL // whether normalizer itself removes non-retained extension data (alternative is
										// that post-processing will do this. This is the case, for example, with
										// swaggernorm gentemplate)
	}

	public enum RetentionScopeType {
		ROOTS, ALL
	}

	public enum HoistType {
		MEDIA_TYPE, PARAMETER, SECURITY_REQUIREMENT
	}

	public enum OrderingScheme {
		AS_DECLARED, SORTED
	}

	public enum ExtensionData {
		ORDERING, POINTER, FILE, TYPE_NAME, BAD_REF
	}

	@Override
	public String toString() {
		return type.name() + ": " + String.valueOf(data);
	}
}
