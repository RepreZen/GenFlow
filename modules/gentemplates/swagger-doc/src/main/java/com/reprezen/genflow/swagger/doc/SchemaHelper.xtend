/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.doc

import io.swagger.models.ArrayModel
import io.swagger.models.Model
import io.swagger.models.ModelImpl
import io.swagger.models.Swagger
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.ObjectProperty
import io.swagger.models.properties.Property

class SchemaHelper implements Helper {

    var Swagger swagger
    extension RefHelper refHelper
    extension AttributeHelper attributeHelper
    extension ArrayHelper arrayHelper

    override init() {
        swagger = HelperHelper.swagger
        refHelper = HelperHelper.refHelper
        attributeHelper = HelperHelper.attributeHelper
        arrayHelper = HelperHelper.arrayHelper
    }

    def renderSchema(Object schema) {
        val resolved = schema.safeResolve
        switch (resolved) {
            ObjectProperty:
                resolved.renderSchemaTable
            ArrayProperty:
                resolved.renderArraySchema
            Property:
                resolved.renderPrimitiveSchema
            ArrayModel:
                resolved.renderArraySchema
            ModelImpl case resolved.properties.empty && resolved.additionalProperties?.safeResolve == null:
                resolved.renderPrimitiveSchema
            Model:
                resolved.renderSchemaTable
        }
    }

    def private renderSchemaTable(Object schema) {
        val table = StructureTable::get(swagger, #["name", "Name"], #["type", "Type"], #["doc", "Description"])
        table.render(schema, null, null)
    }

    def renderArraySchema(Object schema) {
        val typeSpec = schema.arrayTypeSpec
        val eltType = schema.elementType
        val details = new AttrDetails(eltType)
        '''
            <code>«typeSpec»</code>
            «IF eltType.primitive»«details.infoButton»«details.details(true)»«ELSE»«eltType.renderSchemaTable»«ENDIF»
        '''
    }

    def isPrimitive(Object obj) {
        switch (obj) {
            ObjectProperty: false
            ArrayProperty: false
            Property: true
            ModelImpl case obj.properties.empty && obj.additionalProperties?.safeResolve == null: true
            Model: false
        }
    }

    def renderPrimitiveSchema(Object schema) {
        val details = new AttrDetails(schema)
        '''<code>«schema.type»</code>«details.infoButton»«details.details(true)»'''
    }

    def getSchemaTitle(Object schema) {
        val Object resolved = schema.safeResolve
        #[resolved.title, resolved.rzveTypeName].filter[it != null].last
    }
}
