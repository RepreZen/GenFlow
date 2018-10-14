/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.normal.openapi;

import static com.reprezen.genflow.api.normal.openapi.ObjectType.OPENAPI3_MODEL_VERSION;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.normal.openapi.ContentLocalizer.LocalContent;
import com.reprezen.genflow.api.normal.openapi.RefVisitor.Visit;
import com.reprezen.genflow.api.normal.openapi.RefVisitor.VisitRecursionException;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;

/**
 * Process references in an OpenApi spec to produce an equivalent single-file spec.
 * <p>
 * This class operates at the level of a JsonNode structure. It searches the structure for reference nodes (object nodes
 * that contain a "$ref" string property). Every reference in the spec is transformed in one of four ways:
 * <ul>
 * <li>Inlined: The reference node is replaced by the JSON tree to which the reference refers. This is always done for
 * references that are not "model object" references (i.e. references with JSON Pointers that begin with one of
 * "/definitions/", "/parameters/", "/paths/", or "/responses/", for a Swagger model). Some model object refs are also 
 * inlined, depending on options.</li>
 * <li>Localized: The reference node is replaced by a "local" reference node that will resolve to a local model
 * object. The object is copied to the constructed single-file spec and, if necessary, renamed to avoid collisions with
 * objects from other specs.
 * <li>Localized Recursive: If a reference is encountered within the tree that replaced a prior occurrence of the same
 * reference (indicating a recursive reference structure), the second reference is localized rather than inlined.
 * <li>Unresolved localized: An unresolvable reference is replaced with a local reference that is guaranteed to be
 * unresolvable in the constructed spec. That reference's pointer (fragment) will have the original reference string as
 * a suffix. This treatment is to prevent tools from trying and failing to load the unresolvable references.
 * </ul>
 * <p>
 * As a special case, so-called "simple references" in a Swagger spec are optionally rewritten as local swagger references.
 * These are references that really should have "#/xxx/" prepended to be valid, where xxx names one of the standard containers,
 * but as a fairly ill-conceived convenience, they have been allowed in swagger specs, with container implied by
 * context. The Swagger specification has moved beyond these simple refs, but they continue to exist "in the wild," so
 * for the time being we recognize and process them. However, we only recognize such refs if the ref string consists of
 * a single "word" (see SimpleRefFixer.SIMPLE_REF_PAT). These have never been permitted in OpenApi 3, so they are not 
 * supported in OAS3 models. 
 * <p>
 */
/**
 *
 */
public class OpenApiReferenceProcessor {

	private final ContentManager contentManager;
	private final RefVisitor refVisitor = new RefVisitor();
	private static final ObjectMapper yamlMapper = Yaml.mapper();

	private JsonNode spec;
	private final Options options;
	private Integer modelVersion;

	public OpenApiReferenceProcessor(Integer modelVersion, Option... options) {
		this(Option.options(modelVersion, options));
	}

	public OpenApiReferenceProcessor(Options options) {
		this.options = options;
		this.modelVersion = options.getModelVersion();
		this.contentManager = new ContentManager(modelVersion);
	}

	public OpenApiReferenceProcessor of(JsonNode spec) {
		this.spec = spec;
		return this;
	}

	public OpenApiReferenceProcessor of(Swagger spec) {
		return of(yamlMapper.valueToTree(spec));
	}

	public OpenApiReferenceProcessor of(String spec) {
		return of(DocLoader.toJson(spec, options.getModelVersion()));
	}

	public JsonNode inline(URL base) throws GenerationException {
		List<Content> badRoots = Lists.newArrayList();
		if (options.isDoNotNormalize()) {
			return spec;
		}
		// assemble top-level spec, and fault-in all referenced specs
		Content topLevel = contentManager.load(base, spec, options.isRewriteSimpleRefs());
		if (topLevel.isValid()) {
			// reserve existing names for all top-level objects
			contentManager.localizeObjects(topLevel);
		} else {
			badRoots.add(topLevel);
		}
		// set up retention policy and retain whatever needs it
		RetentionPolicy retentionPolicy = new RetentionPolicy(topLevel, options);
		// ensure that all other retained files are loaded as well, even if
		// unreferenced,
		// and reserve names
		for (URL url : options.getAdditionalFileUrls()) {
			Content content = contentManager.load(url, null, options.isRewriteSimpleRefs());
			if (content.isValid()) {
				contentManager.localizeObjects(content);
			} else {
				badRoots.add(content);
			}
		}
		if (!badRoots.isEmpty()) {
			throw new GenerationException("One or more root-level files could not be loaded:\n" + badRoots.stream()
					.map(root -> String.format("%s: %s", root.getRefString(), root.getUnresolvedReason()))
					.collect(Collectors.joining("\n")));
		}
		// retain all objects that should be and were not retained by referencing
		contentManager.applyRetentionPolicy(retentionPolicy);
		// fill in titles for untitled definitions, using the definition name
		new DefinitionProcessor(contentManager).setTypeNames(options.isCreateDefTitles());

		// collect and process retained objects from all specs (including those faulted
		// in while processing others),
		// collecting them all in a new ObjectTree. The resulting tree will be complete,
		// in that every resolvable
		// reference contained in the tree will be a local reference to a model object
		// in the tree. (And even
		// unresolvable references will be local - i.e. pure JSON pointers)
		ObjectNode tree = buildObjectTree();

		// merge non-object content from the original top-level spec to newly built tree
		addNonObjects(tree, topLevel.getTree());

		// sort the tree according to requested ordering scheme
		sortTree(tree);

		// retain position information where we need it
		markPositions(tree);

		return tree;
	}

	private ObjectNode buildObjectTree() {
		ObjectNode tree = JsonNodeFactory.instance.objectNode();

		while (true) {
			Optional<LocalContent> optItem = contentManager.getAndRemoveRetainedModelObject();
			if (optItem.isPresent()) {
				LocalContent item = optItem.get();
				JsonNode object = Util.safeDeepCopy(item.getContent());
				object = inline(object, item.getRef());
				OpenApiMarkers.markPosition(object, item.getPosition());
				addToTree(tree, object, item.getSectionType(), item.getName());
				addJsonPointers(object, item.getRef());
			} else {
				break;
			}
		}
		return tree;
	}

	private void addToTree(ObjectNode tree, JsonNode object, ObjectType sectionType, String objectName) {
		if (sectionType.getFromNode(tree, modelVersion).isMissingNode()) {
			sectionType.setInNode(tree, tree.objectNode(), modelVersion);
		}
		ObjectNode section = (ObjectNode) sectionType.getFromNode(tree, modelVersion);
		section.set(objectName, object);
	}

	private void addNonObjects(ObjectNode objectTree, JsonNode spec) {
		for (Entry<String, JsonNode> field : Util.iterable(spec.fields())) {
			String fieldName = field.getKey();
			// Note: the following test assumes that the top-level property on the path to
			// any container of model
			// objects is itself an object container or is a JSON object that only contains
			// object containers. This
			// avoids what would be a much trickier test before copying a value from the
			// top-level source model to the
			// result model.
			if (!ObjectType.isObjectContainerPrefix(fieldName, modelVersion)) {
				objectTree.set(fieldName, Util.safeDeepCopy(field.getValue()));
			}
			//
			// That assumption is NOT valid for v3 models, because the `components` property
			// can contain vendor
			// extensions. We fudge it by separately copying vendor extensions appearing in
			// that property.
			if (modelVersion == OPENAPI3_MODEL_VERSION && fieldName.equals("components")) {
				addComponentExtensions(objectTree, spec);
			}
		}
		// TODO V2?
		if (ObjectType.PATH.getFromNode(objectTree, modelVersion).isMissingNode()) {
			ObjectType.PATH.setInNode(objectTree, JsonNodeFactory.instance.objectNode(), modelVersion);
		}
	}

	private static final String COMPONENTS_FIELD = "components";

	private void addComponentExtensions(ObjectNode tree, JsonNode spec) {
		for (Entry<String, JsonNode> field : Util.iterable(spec.get(COMPONENTS_FIELD).fields())) {
			String fieldName = field.getKey();
			if (fieldName.startsWith("x-")) {
				if (!tree.has(COMPONENTS_FIELD)) {
					tree.set(COMPONENTS_FIELD, JsonNodeFactory.instance.objectNode());
				}
				ObjectNode components = (ObjectNode) tree.get(COMPONENTS_FIELD);
				components.set(fieldName, Util.safeDeepCopy(field.getValue()));
			}
		}
	}

	private void addJsonPointers(JsonNode object, Reference ref) {
		if (options.isAddJsonPointers()) {
			OpenApiMarkers.markJsonPointer(object, ref.getFragment());
			if (ObjectType.PATH == ref.getSection()) {
				Iterator<String> fields = object.fieldNames();
				while (fields.hasNext()) {
					String fieldName = fields.next();
					if (Util.swaggerMethodOrder.contains(fieldName)) {
						// was not set by external reference processor
						if (RepreZenVendorExtension.get(object.get(fieldName)).getPointer() == null) {
							OpenApiMarkers.markJsonPointer(object.get(fieldName), ref.getFragment() + "/" + fieldName);
						}

					}
				}
			}
		}
	}

	private void addRefFileUrls(JsonNode object, Reference ref) {
		if (options.isAddJsonPointers()) {
			OpenApiMarkers.markFile(object, ref.getCanonicalFileRefString());
			if (ObjectType.PATH == ref.getSection()) {
				Iterator<String> fields = object.fieldNames();
				String pathFileRef = ref.getCanonicalFileRefString();
				while (fields.hasNext()) {
					String fieldName = fields.next();
					if (Util.swaggerMethodOrder.contains(fieldName)) {
						if (pathFileRef != null) {
							OpenApiMarkers.markFile(object.get(fieldName), pathFileRef);
							OpenApiMarkers.markJsonPointer(object.get(fieldName), ref.getFragment() + "/" + fieldName);
						}
					}
				}
			}
		}
	}

	private void sortTree(ObjectNode tree) {
		if (options.isAsDeclaredOrdering()) {
			// nothing to do
		} else if (options.isSortedOrdering()) {
			sort(tree);
		}
	}

	private void sort(ObjectNode tree) {

		for (ObjectType sectionType : ObjectType.getTypesForVersion(modelVersion)) {
			JsonNode section = sectionType.getFromNode(tree, modelVersion);
			if (!section.isMissingNode()) {
				sortSection(section);
				if (sectionType == ObjectType.PATH) {
					for (Entry<String, JsonNode> path : Util.iterable(section.fields())) {
						sortInPath(path.getValue());
					}
				}
			}
		}
	}

	private static Pattern namePat = Pattern.compile("(.+)_(\\d+)");

	private void sortSection(JsonNode section) {
		List<String> names = Lists.newArrayList(section.fieldNames());
		Collections.sort(names, new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				Matcher m1 = namePat.matcher(s1);
				Matcher m2 = namePat.matcher(s2);
				// if prefix matches case-insenstively, sort primarily by prefix,
				// case-sensitively, then by suffix,
				// numerically. This way, e.g. FOO_xxx names will not be intermingled with
				// Foo_xxx names.
				if (m1.matches() && m2.matches() && m1.group(1).equalsIgnoreCase(m2.group(1))) {
					return ComparisonChain.start() //
							.compare(m1.group(1), m2.group(1)) //
							.compare(Integer.valueOf(m1.group(2)), Integer.valueOf(m2.group(2))) //
							.result();
				} else if (m1.matches() && !m2.matches() && m1.group(1).equalsIgnoreCase(s2)) {
					// This case and the next ensure that Foo immediatly precedes Foo_xxx, and
					// likewise FOO immediately
					// precedes FOO_xxx
					return m1.group(1).compareTo(s2);
				} else if (!m1.matches() && m2.matches() && s1.equalsIgnoreCase(m2.group(1))) {
					return s1.compareTo(m2.group(1));
				} else
					return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
			}
		});

		ObjectNode sectionObj = (ObjectNode) section;
		for (String name : names) {
			if (!isVendorExtension(name)) {
				JsonNode obj = sectionObj.remove(name);
				sectionObj.set(name, obj);
			}
		}
	}

	private void sortInPath(JsonNode path) {
		ObjectNode pathObj = (ObjectNode) path;
		for (String methodName : Util.swaggerMethodOrder) {
			if (pathObj.has(methodName)) {
				ObjectNode methodObj = (ObjectNode) pathObj.remove(methodName);
				pathObj.set(methodName, methodObj);
				sortResponsesInMethod(methodObj);
			}
		}

	}

	private Pattern numberPat = Pattern.compile("\\d+");

	private void sortResponsesInMethod(ObjectNode method) {
		ObjectNode responses = (ObjectNode) method.get("responses");
		if (responses != null) {
			List<String> names = Lists.newArrayList(responses.fieldNames());
			// numeric responses first, followed by named responses
			Collections.sort(names, new Comparator<String>() {
				@Override
				public int compare(String s1, String s2) {
					if (numberPat.matcher(s1).matches()) {
						if (numberPat.matcher(s2).matches()) {
							return Integer.valueOf(s1) - Integer.valueOf(s2);
						} else {
							return -1;
						}
					} else if (numberPat.matcher(s2).matches()) {
						return 1;
					} else {
						return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
					}
				}
			});
			for (String name : names) {
				if (!isVendorExtension(name)) {
					JsonNode obj = responses.remove(name);
					responses.set(name, obj);
				}
			}
		}
	}

	private void markPositions(ObjectNode tree) {
		for (ObjectType sectionType : ObjectType.getTypesForVersion(modelVersion)) {
			JsonNode section = sectionType.getFromNode(tree, modelVersion);
			if (section != null) {
				markPositionsInSection(section);
				if (sectionType == ObjectType.PATH) {
					for (Entry<String, JsonNode> path : Util.iterable(section.fields())) {
						markPositionsInPath(path.getValue());
					}
				}
			}
		}
	}

	private void markPositionsInSection(JsonNode section) {
		int position = 0;
		for (Entry<String, JsonNode> field : Util.iterable(section.fields())) {
			if (!isVendorExtension(field.getKey())) {
				OpenApiMarkers.markPosition(field.getValue(), position++);
			}
		}
	}

	private void markPositionsInPath(JsonNode path) {
		int position = 0;
		for (Entry<String, JsonNode> method : Util.iterable(path.fields())) {
			if (!isVendorExtension(method.getKey())) {
				OpenApiMarkers.markPosition(method.getValue(), position++);
				JsonNode responses = method.getValue().get("responses");
				if (responses != null) {
					int respPosition = 0;
					for (Entry<String, JsonNode> response : Util.iterable(responses.fields())) {
						if (!isVendorExtension(response.getKey())) {
							OpenApiMarkers.markPosition(response.getValue(), respPosition++);
						}
					}
				}
			}
		}
	}

	/**
	 * Peform inline processing on a single top-level model object in the final
	 * constructed spec.
	 */
	private JsonNode inline(JsonNode tree, Reference ref) {
		try (Visit v = refVisitor.visit(ref)) {
			return maybeInline(tree, ref);
		} catch (VisitRecursionException e) {
			// this exception is actually impossible here, since this method is invoked only
			// for top-level swagger
			// objects in the final constructed specs. Thus, on entry, the "visited refs"
			// list in RefVisitor class must
			// be empty. Of course, Java compiler can't figure that out so we need to do
			// something here.
			return tree;
		}
	}

	/**
	 * Main workhorse of this class.
	 * <p>
	 * This method recursively visits the entire tree in a depth-first manner,
	 * processing every reference node encountered, in one of the four manners
	 * described in the class javadoc. The depth-first strategy applies to inlined
	 * content, which is processed immediately after insertion into the tree, before
	 * continuing with whatever node would otherwise have followed the replaced ref
	 * node.
	 */
	private JsonNode maybeInline(JsonNode node, Reference base) {
		if (Util.isRef(node)) {
			Reference ref = new Reference(Util.getRefString(node).get(), base, modelVersion);
			if (ref.isModelObjectRef()) {
				ObjectType section = ref.getSection();
				if (options.isInlined(section)) {
					// try to inline
					Optional<JsonNode> replacement = contentManager.getModelObject(ref);
					if (replacement.isPresent()) {
						// guard against recursion
						try (Visit v = refVisitor.visit(ref)) {
							// good replacement, non-recursive... perform the reference
							JsonNode result = maybeInline(Util.safeDeepCopy(replacement.get()), ref);
							addRefFileUrls(result, ref);
							if (ref.isDefinitionRef()) {
								// schemas turn into embedded objects and therefore lose their "definition"
								// names - we
								// mark them with a vendor extension for use in generated documentation
								OpenApiMarkers.markTypeName(result, ref.getObjectName());
							}
							return result;
						} catch (VisitRecursionException e) {
							// this was a recursive reference - localize instead
							return contentManager.getLocalizedRefNode(ref);
						}
					} else {
						// could not obtain replacement tree from reference
						return ref.getBadRefNode();
					}
				} else {
					// !inline => localize
					return contentManager.getLocalizedRefNode(ref);
				}
			}
			// a ref, but not a model object ref - we shouldn't have those following
			// assembly by ContentManager
			return ref.getBadRefNode();

		} else {
			// not a reference node - search contained structures for embedded refs
			maybeInlineObjectFields(node, base);
			maybeInlineArrayElements(node, base);
			return node;
		}
	}

	private void maybeInlineObjectFields(JsonNode node, Reference base) {
		if (node instanceof ObjectNode) {
			ObjectNode objNode = (ObjectNode) node;
			for (Entry<String, JsonNode> field : Util.iterable(objNode.fields())) {
				field.setValue(maybeInline(field.getValue(), base));
			}
		}
	}

	private void maybeInlineArrayElements(JsonNode node, Reference base) {
		if (node instanceof ArrayNode) {
			ArrayNode arrayNode = (ArrayNode) node;
			for (int i = 0; i < arrayNode.size(); i++) {
				arrayNode.set(i, maybeInline(arrayNode.get(i), base));
			}
		}
	}

	private boolean isVendorExtension(String propName) {
		return propName.startsWith("x-");
	}
}
