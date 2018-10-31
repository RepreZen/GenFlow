/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.diagram.xtend

import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PropertyReference
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.SourceReference

/**
 * Feature related extensions
 */
class XFeatureExtensions {
    def featureId(ResourceDefinition aResource, Feature aFeature) {
        aResource.name + '.' + aFeature.containingDataType.name + '.' + aFeature.name    
    } 
    
    def dispatch referenceFeatureId(ResourceDefinition aResource, SourceReference sourceReference) {
        '<undefined>'    
    } 

    def dispatch referenceFeatureId(ResourceDefinition aResource, PropertyReference sourceReference) {
        aResource.featureId(sourceReference.conceptualFeature)    
    } 
}
