/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xtend

import com.reprezen.restapi.MessageParameter
import com.reprezen.restapi.Parameter
import com.reprezen.restapi.PrimitiveProperty
import com.reprezen.restapi.PropertyReference
import com.reprezen.restapi.ReferenceProperty
import com.reprezen.restapi.ReferenceTreatment
import com.reprezen.restapi.SourceReference
import com.reprezen.restapi.TypedMessage
import java.util.Collections
import java.util.List

/**
 * Helper methods for parameters.
 */
class XParameterHelper {

    def Iterable<MessageParameter> getPrimitiveParameters(TypedMessage aMessage) {
        aMessage.parameters.filter[p|isPrimitivePropertyParameter(p.sourceReference)]
    }

    def PrimitiveProperty getPrimitiveProperty(Parameter aParameter) {
        (aParameter.sourceReference as PropertyReference).conceptualFeature
    }

    def Iterable<? extends Parameter> getReferenceParameters(TypedMessage aMessage) {
        aMessage.parameters.filter[p|isReferencePropertyParameter(p.sourceReference)]
    }

    /**
     * @return parameter name depend on reference class
     */
    def dispatch String paramName(PropertyReference propertyReference) {
        propertyReference.conceptualFeature.name
    }

    def dispatch String paramName(SourceReference sourceReference) {
        ""
    }

    /**
     * @return parameter type depend on reference class
     */
    def String paramType(SourceReference propertyReference, XImportHelper importHelper) {
        importHelper.getQualifiedName(propertyReference.type)
    }

    /**
     * @return is source reference a primitive property
     */
    def dispatch boolean isPrimitivePropertyParameter(SourceReference sourceReference) {
        false
    }

    def dispatch boolean isPrimitivePropertyParameter(PropertyReference propertyReference) {
        propertyReference.conceptualFeature instanceof PrimitiveProperty
    }

    /**
     * @return is source reference a property reference
     */
    def dispatch boolean isReferencePropertyParameter(SourceReference sourceReference) {
        false
    }

    def dispatch boolean isReferencePropertyParameter(PropertyReference sourceReference) {
        sourceReference.conceptualFeature instanceof ReferenceProperty
    }

    /**
     * @return feature type depend on property type
     */
    def dispatch String featureType(ReferenceProperty feature, XImportHelper importHelper) {
        feature.type.name
    }

    def dispatch String featureType(PrimitiveProperty feature, XImportHelper importHelper) {
        feature.type.name
    }

    def List<ReferenceProperty> getContainmentReferences(ReferenceTreatment aReferenceLink) {
        Collections::emptyList
    }
}
