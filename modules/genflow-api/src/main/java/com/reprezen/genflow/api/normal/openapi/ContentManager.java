package com.reprezen.genflow.api.normal.openapi;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.normal.openapi.ContentLocalizer.LocalContent;

/**
 * Loads and manages all the documents that contribute to a given model spec.
 * <p>
 * Here, "document" means a Json tree loaded from a single file.
 */
public class ContentManager {

	private final Integer modelVersion;
	private final Map<String, Content> contentItems = Maps.newLinkedHashMap();
	private final ContentLocalizer localizer;
	private final Set<String> toBeLoaded = Sets.newLinkedHashSet();

	// following lets us avoid using URLs as set items or map keys, because
	// URL#equals is potentially very expensive
	// (involving network operations). And it gives us an exception-free way to
	// translate from URL strings we know to be
	// cached into the corresponding URLs.
	private final Map<String, URL> urlCache = Maps.newHashMap();

	public ContentManager(Integer modelVersion) {
		this.modelVersion = modelVersion;
		this.localizer = new ContentLocalizer(modelVersion);
	}

	public Integer getModelVersion() {
		return modelVersion;
	}

	/**
	 * Public load for the top-level model document.
	 * <p>
	 * This method may provide the preloaded tree corresponding to the specified
	 * URL. E.g. this would generally be the case when a model spec in an open
	 * editor is normalized. In such cases, the URL is still required so that
	 * relative references appearing in that tree can be resolved.
	 */
	public Content load(URL url, JsonNode tree, boolean rewriteSimpleRefs) {
		Content content = loadInternal(url, tree, rewriteSimpleRefs);
		loadRequestedDocuments(rewriteSimpleRefs);
		return content;
	}

	/**
	 * This private load method is used to load documents directly or indirectly
	 * referenced by the top-level document. A preloaded tree is never provided in
	 * this case.
	 */
	private Content loadRequestedDoc(URL url, boolean rewriteSimpleRefs) {
		return loadInternal(url, null, rewriteSimpleRefs);
	}

	/**
	 * Load a JSON tree from the given URL (unless preloaded tree is provided),
	 * register the content, and maybe rewrite "simple" refs to local object refs
	 */
	private Content loadInternal(URL url, JsonNode tree, boolean rewriteSimpleRefs) {
		String urlString = cacheUrl(url);
		Content content = contentItems.get(urlString);
		if (content == null) {
			content = new ContentLoader(this).load(url, tree, rewriteSimpleRefs);
			contentItems.put(urlString, content);
		}
		return content;
	}

	/**
	 * Request that a document be loaded and registered with the content manager.
	 * <p>
	 * This will be called for every document (i.e. a reference minus its fragment)
	 * referenced in any loaded document.
	 */

	public void requestDocument(URL url) {
		toBeLoaded.add(cacheUrl(url));
	}

	/**
	 * Load requested documents until there are no more newly requested documents.
	 * <p>
	 * During this method, more documents may be requested.
	 */
	private void loadRequestedDocuments(boolean rewriteSimpleRefs) {
		while (!toBeLoaded.isEmpty()) {
			String urlString = toBeLoaded.iterator().next();
			toBeLoaded.remove(urlString);
			if (!contentItems.containsKey(urlString)) {
				loadRequestedDoc(urlCache.get(urlString), rewriteSimpleRefs);
			}
		}
	}

	public Iterable<Content> contentItems() {
		return contentItems.values();
	}

	public Optional<JsonNode> getModelObject(Reference ref) {
		if (ref.isModelObjectRef()) {
			String urlString = cacheUrl(ref.getUrl());
			Content content = contentItems.get(urlString);
			if (content != null) {
				return content.getObject(ref.getSection(), ref.getObjectName());
			}
		}
		return Optional.empty();
	}

	public Reference getLocalizedRef(Reference ref) {
		Optional<JsonNode> node = getModelObject(ref);
		if (node.isPresent()) {
			localizer.register(ref, node.get());
			return localizer.getLocalizedRef(ref);
		} else {
			return ref.getBadRef();
		}
	}

	public JsonNode getLocalizedRefNode(Reference ref) {
		Optional<JsonNode> node = getModelObject(ref);
		if (node.isPresent()) {
			localizer.register(ref, node.get());
			return localizer.getLocalizedRefNode(ref);
		} else {
			return ref.getBadRefNode();
		}

	}

	/**
	 * Localize all the model objects in the given content item
	 * <p>
	 * This is used when to ensure that all model objects appearing in the top-level
	 * spec will retain their original names in the final spec. Model objects
	 * appearing in other specs may have their names changed for disambiguation.
	 */
	public void localizeObjects(Content content) {
		for (ObjectType section : ObjectType.getTypesForVersion(modelVersion, Option.COMPONENT_OBJECTS)) {
			localizeObjectsInSection(content, section);
		}
		localizeObjectsInSection(content, ObjectType.PATH);
	}

	public void localizeObjectsInSection(Content content, ObjectType section) {
		for (String objectName : content.getObjectNames(section)) {
			Reference ref = content.getObjectReference(section, objectName);
			JsonNode obj = content.getObject(section, objectName).get();
			localizer.register(ref, obj);
		}
	}

	public ContentLocalizer getLocalizer() {
		return localizer;
	}

	public void applyRetentionPolicy(RetentionPolicy policy) {
		for (Entry<String, Content> e : contentItems.entrySet()) {
			Content content = e.getValue();
			for (ObjectType section : ObjectType.getTypesForVersion(modelVersion)) {
				if (policy.shouldRetain(urlCache.get(e.getKey()), section)) {
					retainObjectsInSection(content, section);
				}
			}
		}
	}

	private void retainObjectsInSection(Content content, ObjectType section) {
		for (String objectName : content.getObjectNames(section)) {
			Reference ref = content.getObjectReference(section, objectName);
			JsonNode obj = content.getObject(section, objectName).get();
			localizer.register(ref, obj).retain();
		}
	}

	public void retainImplicitlyReferencedObjects() {
		for (LocalContent item : localizer.getLocalContentItems()) {
			if (item.isRetained()) {
				if (item.getSectionType() == ObjectType.PATH && item.isRetained()) {
					for (LocalContent requiredScheme : getRequiredSecuritySchemes(item)) {
						requiredScheme.retain();
					}
				}
			}
		}
	}

	private List<LocalContent> getRequiredSecuritySchemes(LocalContent path) {
		JsonNode tree = path.getContent();
		Set<String> reqs = new HashSet<>();
		for (String meth : Arrays.asList("get", "put", "post", "delete", "options", "head", "patch", "trace")) {
			if (tree.has(meth)) {
				for (Iterator<JsonNode> reqIter = tree.get(meth).path("security").elements(); reqIter.hasNext();) {
					for (Iterator<String> nameIter = reqIter.next().fieldNames(); nameIter.hasNext();) {
						reqs.add(nameIter.next());
					}
				}
			}
		}
		return reqs.stream().map(name -> localizer.getLocalContent("/components/securitySchemes", name))
				.filter(lc -> lc != null).collect(Collectors.toList());
	}

	public Optional<LocalContent> getAndRemoveRetainedModelObject() {
		return localizer.getAndRemoveRetainedModelObject();
	}

	private String cacheUrl(URL url) {
		String key = url.toString();
		urlCache.put(key, url);
		return key;
	}
}
