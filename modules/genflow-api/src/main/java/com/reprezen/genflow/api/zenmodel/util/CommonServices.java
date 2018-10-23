/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.zenmodel.util;

import static com.reprezen.restapi.xtext.loaders.ZenLibraries.PRIMITIVE_TYPES;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.util.Strings;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.reprezen.restapi.Feature;
import com.reprezen.restapi.MediaType;
import com.reprezen.restapi.Method;
import com.reprezen.restapi.PropertyRealization;
import com.reprezen.restapi.ResourceAPI;
import com.reprezen.restapi.SingleValueType;
import com.reprezen.restapi.Structure;
import com.reprezen.restapi.TypedMessage;
import com.reprezen.restapi.TypedRequest;
import com.reprezen.restapi.TypedResponse;
import com.reprezen.restapi.ZenModel;
import com.reprezen.restapi.datatypes.cardinality.Cardinality;
import com.reprezen.restapi.datatypes.cardinality.FeatureCardinalities;
import com.reprezen.restapi.datatypes.cardinality.OverrideCardinalities;
import com.reprezen.restapi.xtext.loaders.ModelLoaderUtils;
import com.reprezen.restapi.xtext.util.ZenModelHelper;

/**
 * Java utilities for the templates around the WADL generation.
 * 
 * @author jimleroyer
 * @since 2013/05/29
 */
public class CommonServices {
	private ZenModel zenModel;

	public CommonServices() {
	}

	/**
	 * Returns true if the given message has a resource type else false.
	 * 
	 * @param message {@link com.modelsolv.reprezen.restapi.TypedMessage} to check
	 * @return Boolean
	 */
	public Boolean hasResourceType(TypedMessage message) {
		return message.getResourceType() != null;
	}

	public Collection<MediaType> getMediaSuperTypes(MediaType mediaTypeChild) {
		Collection<MediaType> superTypes = new HashSet<MediaType>();
		superTypes.addAll(getMediaTypeDerivations(mediaTypeChild));
		return superTypes;
	}

	/**
	 * Gets the pretty printed multiplicity.
	 *
	 * @param feature the feature
	 * @return the pretty-printed multiplicity, shortcuts are used wherever possible
	 */
	public String getPrettyPrintedMultiplicity(Feature feature) {
		Cardinality cardinality = FeatureCardinalities.getFeatureCardinalities().getCardinality(feature);
		return cardinality.getLabel();
	}

	/**
	 * Gets the pretty printed cardinality.
	 *
	 * @param includedProperty the included property
	 * @return the pretty printed cardinality
	 */
	public String getPrettyPrintedCardinality(PropertyRealization includedProperty) {
		Cardinality cardinality;
		// cardinality override
		if (includedProperty.getCardinality() != null) {
			cardinality = OverrideCardinalities.getOverrideCardinalities().getCardinality(includedProperty);
		} else {
			// use the cardinality from the base property
			cardinality = FeatureCardinalities.getFeatureCardinalities()
					.getCardinality(includedProperty.getBaseProperty());
		}
		return cardinality.getLabel();
	}

	protected ZenModel getAndLoadZenModel() throws IOException {
		if (this.zenModel == null) {
			this.zenModel = ModelLoaderUtils.loadModel(PRIMITIVE_TYPES);
		}
		return this.zenModel;
	}

	/**
	 * Recursively gets all {@link MediaType#getDerivedFrom()} mediatypes.
	 * 
	 * @param mediaType the media type
	 * @return the media super types
	 */
	private Set<MediaType> getMediaTypeDerivations(MediaType mediaType) {
		Set<MediaType> mediaTypeDerivations = new HashSet<MediaType>();
		if (!mediaType.getDerivedFrom().isEmpty()) {
			mediaTypeDerivations.addAll(mediaType.getDerivedFrom());
			for (MediaType parent : mediaType.getDerivedFrom()) {
				mediaTypeDerivations.addAll(getMediaTypeDerivations(parent));
			}
		}
		return mediaTypeDerivations;
	}

	/**
	 * @deprecated use {@link CommonServices#getRequestTypeName(TypedRequest)} or
	 *             {@link CommonServices#getResponseTypeName(TypedResponse)} instead
	 * @param message typed message
	 * @return generated type name for XSD generator that avoid name clashes
	 */
	@Deprecated
	public static String getMessageTypeName(TypedMessage message) {
		Method method = (Method) message.eContainer();
		String methodName = method.getHttpMethod().getName().toLowerCase()
				+ method.getContainingResourceDefinition().getName();
		final Structure type = message.getActualType();
		ImmutableList<TypedMessage> list = ImmutableList.copyOf(Iterators.filter(
				Iterators.filter(method.getContainingResourceDefinition().eAllContents(), TypedMessage.class),
				new Predicate<TypedMessage>() {
					@Override
					public boolean apply(TypedMessage input) {
						return input.getActualType() == type;
					}
				}));
		String baseName = Strings.toFirstUpper(methodName) + '_' + message.getActualType().getName();
		if (list.size() > 1) {
			return baseName + '_' + (list.indexOf(message) + 1);
		}
		return baseName;
	}

	public static List<SingleValueType> getUsedSimpleTypes(ResourceAPI resourceAPI) {
		List<SingleValueType> types = new LinkedList<>();
		ZenModelHelper helper = new ZenModelHelper();
		types.addAll(helper.getUsedEnums(resourceAPI));
		types.addAll(helper.getUsedUserDefinedTypes(resourceAPI));

		return types;
	}

	public static List<SingleValueType> getUsedSimpleTypes(ZenModel zenModel) {
		List<SingleValueType> types = new LinkedList<>();
		ZenModelHelper helper = new ZenModelHelper();

		Iterables.addAll(types, helper.getUsedEnums(zenModel));
		Iterables.addAll(types, helper.getUsedUserDefinedTypes(zenModel));

		return types;
	}

	/**
	 * @param ele  element to search parent container
	 * @param type type of parent
	 * @return parent container in hierarchy with given type
	 */
	public static <T extends EObject> T getContainerOfType(EObject ele, Class<T> type) {
		for (EObject e = ele; e != null; e = e.eContainer()) {
			if (type.isInstance(e)) {
				return type.cast(e);
			}
		}
		return null;
	}

}
