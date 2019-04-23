package com.reprezen.genflow.api.normal.openapi;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;

/**
 * Manages model objects that registered as local objects in the final
 * constructed model spec.
 * <p>
 * Localization comprises two distinct treatments:
 * <ul>
 * <li>Registration: Makes a copy of the JSON tree corresponding to an object,
 * and creates a name for that object in the final tree. If the object's name in
 * its defining spec is not yet used for another object, that name will be used.
 * Otherwise it will be the basis for a disambiguated name.</li>
 * <li>Retaining: Marks a registered object as required in the final constructed
 * model spec. Only retained objects will be emitted in that spec. Depending on
 * a normalizer option, all top-level objects may be retained. If not, they and
 * all objects originating in component specs are retained only if they are the
 * target of a reference that is localized.</li>
 * </ul>
 * Note that paths are always inlined and retained. This logically follows from
 * the following observations:
 * <ul>
 * <li>A path reference MUST be an external reference (since a local path
 * reference would necessarily refer to the value of a member of the containing
 * spec's "paths" object, which would make that path already a path in the
 * containing spec).</li>
 * <li>Every property in the top-level spec's "paths" object defines a path in
 * the top-level spec. If the definition uses a path reference, that reference
 * must be inlined in order to fulfill this requirement in the constructed
 * single-file spec.</li>
 * </ul>
 * These observations are unique to paths, and do not apply to the other types
 * of named model objects, since the property values in the corresponding model
 * sections are not allowed to be references (though they may contained embedded
 * references).
 */

public class ContentLocalizer {

	private final Integer modelVersion;

	private final Map<Reference, LocalContent> localContentItems = Maps.newLinkedHashMap();
	private final Map<String, LocalContent> itemsByLocalName = Maps.newHashMap();
	private Deque<LocalContent> retainedObjectQueue = new ArrayDeque<LocalContent>();
	private int nextPositionValue = 0;

	public ContentLocalizer(Integer modelVersion) {
		this.modelVersion = modelVersion;
	}

	/**
	 * Register the given object tree under the given reference, and select a name
	 * for a local copy of that object if it ends up in the constructed spec.
	 */
	public LocalContent register(Reference ref, JsonNode node) {
		if (!localContentItems.containsKey(ref)) {
			register(new LocalContent(ref, node));
		}
		return localContentItems.get(ref);
	}

	private void register(LocalContent localContent) {
		localContentItems.put(localContent.getRef(), localContent);
	}

	/**
	 * Get a reference to the local copy of the referenced object in the constructed
	 * spec.
	 * <p>
	 * Obtaining a localized reference has the side-effect of retaining the object
	 * in the constructed spec.
	 */
	public Reference getLocalizedRef(Reference ref) {
		LocalContent localContent = localContentItems.get(ref);
		if (localContent != null) {
			localContent.retain();
			return new Reference(localContent.getLocalizedRefString(), modelVersion);
		} else {
			return ref.getBadRef();
		}
	}

	public JsonNode getLocalizedRefNode(Reference ref) {
		return getLocalizedRef(ref).getRefNode();
	}

	/**
	 * Obtain the localized object by its local section and object names
	 */
	public LocalContent getLocalContent(String sectionName, String objectName) {
		return itemsByLocalName.get(sectionName + ":" + objectName);
	}

	public Iterable<LocalContent> getLocalContentItems() {
		return localContentItems.values();
	}

	/**
	 * Pop the next object off the retained object queue
	 */
	public Optional<LocalContent> getAndRemoveRetainedModelObject() {
		return retainedObjectQueue.isEmpty() ? Optional.<LocalContent>empty()
				: Optional.of(retainedObjectQueue.remove());
	}

	public class LocalContent {

		private final Reference ref;
		private final JsonNode content;
		private final String name;
		private boolean isRetained = false;
		private int position;

		public LocalContent(Reference ref, JsonNode node) {
			this.ref = ref;
			this.content = node;
			this.name = chooseLocalName();
			this.position = ContentLocalizer.this.nextPositionValue++;
		}

		public Reference getRef() {
			return ref;
		}

		public JsonNode getContent() {
			return content;
		}

		public String getName() {
			return name;
		}

		public String getLocalizedRefString() {
			return "#" + getSectionPath() + "/" + name;
		}

		public String getSectionPath() {
			return ref.getSectionPath();
		}

		public ObjectType getSectionType() {
			return ref.getSection();
		}

		public int getPosition() {
			return position;
		}

		public void retain() {
			if (!isRetained) {
				retainedObjectQueue.addLast(this);
			}
			isRetained = true;
		}

		public boolean isRetained() {
			return isRetained;
		}

		private String chooseLocalName() {
			String name = ref.getObjectName();
			if (name == null) {
				name = "_ANON";
			}
			String key = getSectionPath() + ":" + name;
			if (!itemsByLocalName.containsKey(key)) {
				itemsByLocalName.put(key, this);
				return name;
			}
			for (int i = 1;; i++) {
				String disambiguatedKey = key + "_" + i;
				if (!itemsByLocalName.containsKey(disambiguatedKey)) {
					itemsByLocalName.put(disambiguatedKey, this);
					return name + "_" + i;
				}
			}
		}

		@Override
		public String toString() {
			return String.format("LocalContent[%s]\n", getLocalizedRefString());
		}
	}
}
