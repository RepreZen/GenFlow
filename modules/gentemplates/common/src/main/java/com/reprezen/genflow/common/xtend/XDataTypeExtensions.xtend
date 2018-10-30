/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.PropertyRealization
import com.reprezen.rapidml.ReferenceEmbed
import com.reprezen.rapidml.ReferenceLink
import com.reprezen.rapidml.ReferenceTreatment
import com.reprezen.rapidml.datatypes.cardinality.Cardinality
import com.reprezen.rapidml.datatypes.cardinality.FeatureCardinalities
import com.reprezen.rapidml.datatypes.cardinality.OverrideCardinalities
import java.util.List

/**
 * Extension for datatype
 */
class XDataTypeExtensions {
	/**
	 * Currently {@link ReferenceTreatment} does not have its own cardinality so we need to implement 
	 * business logic (how different link realizations are shown at different places) using external adjustments. 
	 * <p/>
	 * It would be more natural to explicitly set the computed cardinality from the same code that computes implicit 
	 * realizations. This should be revisited after implementing ZEN-737. 
	 */
	def static getCardinalityLabel(ReferenceTreatment ref, List<PropertyRealization> includedProperties) {
		val prop = includedProperties.findFirst[e|e.baseProperty == ref.referenceElement]
		if (prop !== null) {
			val Cardinality cardinality = OverrideCardinalities.overrideCardinalities.getCardinality(prop)
			return asteriskIfNeeded(ref, cardinality)
		}
		val Cardinality cardinality = FeatureCardinalities.featureCardinalities.getCardinality(
			ref.referenceElement)
		return asteriskIfNeeded(ref, cardinality)
	}

	private static dispatch def asteriskIfNeeded(ReferenceLink refLink, Cardinality cardinality) {

		// see ZEN-1931: Single link to a collectionResource should not show an asterisk
		if(refLink.targetResource instanceof CollectionResource) "" else labelIfMany(cardinality)
	}

	private static dispatch def asteriskIfNeeded(ReferenceEmbed refEmbed, Cardinality cardinality) {
		return labelIfMany(cardinality)
	}

	private static def labelIfMany(Cardinality cardinality) {
		if(cardinality.upper == -1) cardinality.label else ""
	}
}
