/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi3.doc

import com.reprezen.genflow.openapi3.doc.StructureTable.SchemaStructureTable
import com.reprezen.jsonoverlay.Overlay
import com.reprezen.kaizen.oasparser.model3.Schema

class SchemaHelper implements Helper {

    extension AttributeHelper attributeHelper
    extension ArrayHelper arrayHelper
	extension KaiZenParserHelper = new KaiZenParserHelper

    override init() {        
        attributeHelper = HelperHelper.attributeHelper
        arrayHelper = HelperHelper.arrayHelper
    }

    def renderSchema(Schema schema) {               
        switch schema {
        	case schema.primitive:
        		schema.renderPrimitiveSchema
        	case schema.type == 'array':
        		schema.renderArraySchema    	
        	default:
        		schema.renderSchemaTable
        }
    }

    def private renderSchemaTable(Schema schema) {
    	if (schema !== null && !Overlay.of(schema).toJson.empty) {
        	val table = new SchemaStructureTable(schema, #["name", "Name"], #["type", "Type"], #["doc", "Description"])
        	table.render(null)
        }
    }

    def renderArraySchema(Schema schema) {
        val typeSpec = schema.arrayTypeSpec        
        val Schema eltType = schema.elementType
        val details = new AttrDetails(eltType)
        '''
            <code>«typeSpec»</code>
            «IF eltType.primitive»«details.infoButton»«details.details(true)»«ELSE»«eltType.renderSchemaTable»«ENDIF»
        '''
    }

    def isPrimitive(Schema obj) {
    	#{"boolean", "integer", "null", "number", "string"}.contains(obj.type)
    }

    def renderPrimitiveSchema(Schema schema) {
        val details = new AttrDetails(schema)
        '''<code>«schema.type»</code>«details.infoButton»«details.details(true)»'''
    }

    def getSchemaTitle(Schema schema) {
        #[schema.getKaiZenSchemaName, schema.title, schema.rzveTypeName].filter[it !== null].last
    }
}
