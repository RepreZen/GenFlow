/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.api.zenmodel.ZenModelExtractOutputItem
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.EnumConstant
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.SingleValueType
import com.reprezen.rapidml.UserDefinedType
import com.reprezen.rapidml.ZenModel
import java.io.File

class DataModelSchemaGenerator extends ZenModelExtractOutputItem<DataModel> {
	extension XMLSchemaHelper xmlSchemaHelper

	override init(IGenTemplateContext context) {
		val model = context.primarySource.load as ZenModel
		val helpers = new Helpers(context, model)
		this.xmlSchemaHelper = helpers.xmlSchemaHelper
	}

	override File getOutputFile(ZenModel zenModel, DataModel dataModel) {
		return new File(dataModel.xsdFileName(zenModel))
	}

	override String generate(ZenModel zenModel, DataModel dataModel) {
		return '''
			<xs:schema
				targetNamespace="«dataModel.namespace»"
				elementFormDefault="qualified"
				xmlns="«dataModel.namespace»"
				xmlns:tns="«dataModel.namespace»"
				xmlns:xs="http://www.w3.org/2001/XMLSchema"
				xmlns:xml="http://www.w3.org/XML/1998/namespace"
				«dataModel.generateNamespaceAdditions»
			>
			«dataModel.generateXSDDoc»
			«FOR dataType : dataModel.ownedDataTypes SEPARATOR ""»
				«IF (dataType instanceof Enumeration) || (dataType instanceof UserDefinedType)»
					«generate(dataType as SingleValueType)»
				«ENDIF»
			«ENDFOR»
			</xs:schema>
		'''
	}

	def private generateNamespaceAdditions(DataModel dataModel) {
		'''
			xmlns:jaxb="http://java.sun.com/xml/ns/jaxb"
			jaxb:version="2.0"
		'''
	}

	def private dispatch generate(Enumeration en) {
		'''
			<xs:simpleType name="«en.typeName»">
			«en.generateEnumAnnotation»
				<xs:restriction base="xs:«en.baseType.name»">
					«FOR enumConstant : en.enumConstants SEPARATOR ""»
						«enumConstant.generateEnumConstant»
					«ENDFOR»
				</xs:restriction>
			</xs:simpleType>
		'''
	}

	def private dispatch generate(UserDefinedType userDefinedType) {
		'''
			<xs:simpleType name="«userDefinedType.typeName»">
				«generateRestriction(userDefinedType.baseTypeName, userDefinedType.allConstraints)»
			</xs:simpleType>
		'''
	}

	def private generateEnumAnnotation(Enumeration en) {
		'''
			<xs:annotation>
				<xs:appinfo>
					<jaxb:typesafeEnumClass />
				</xs:appinfo>
			</xs:annotation>
		'''
	}

	def private generateEnumConstant(EnumConstant enumConstant) {
		'''
			<xs:enumeration value="«enumConstant.literalValue ?: enumConstant.integerValue»">
				«enumConstant.generateEnumConstantAnnotation»
			</xs:enumeration>
		'''
	}

	def private generateEnumConstantAnnotation(EnumConstant enumConstant) {
		'''
			<xs:annotation>
				<xs:appinfo>
					<jaxb:typesafeEnumMember name="«enumConstant.name»" />
				</xs:appinfo>
			</xs:annotation>
		'''
	}
}
