package com.reprezen.genflow.rapidml.csharp.generators

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.helpers.FileHelper
import com.reprezen.genflow.rapidml.csharp.helpers.FileRole
import com.reprezen.genflow.rapidml.csharp.helpers.NameHelper
import com.reprezen.genflow.rapidml.csharp.helpers.TypeHelper
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.ZenModel

import static com.reprezen.genflow.rapidml.csharp.helpers.UtilsHelper.*

import static extension com.reprezen.genflow.rapidml.csharp.helpers.DocHelper.*

class StructurePocoGenerator {
	val ZenModel model
	val IGenTemplateContext context
	val Config config
	extension NameHelper nameHelper
	extension TypeHelper typeHelper

	new(ZenModel model, IGenTemplateContext context, Config config) {
		this.model = model
		this.context = context
		this.config = config
		this.nameHelper = NameHelper.forModel(model)
		this.typeHelper = TypeHelper.forModel(model)
	}

	def generate() {
		for (dataModel : model.dataModels) {
			for (type : dataModel.ownedDataTypes.filter[it instanceof Structure].map[it as Structure]) {
				val extension fileHelper = FileHelper.of(type, FileRole.POCOS, context, config)
				type.generatePoco.writeFile(type.name.csharpFileName)
			}
		}
	}

	def private String generatePoco(Structure structure) {
		val name = structure.typePocoName
		'''
			«structure.simpleDoc»[DeserializeFrom(typeof(«structure.typeInterfaceName»))]
			«generatedAttr»
			public partial class «name» : I«name» {
			    «FOR Feature field : structure.ownedFeatures»
			    	«field.simpleDoc»«field.generateFieldProperty»
			    	
			    «ENDFOR»
			}
			
		'''
	}

	def private generateFieldProperty(Feature field) {
		val typeName = switch (field) {
			PrimitiveProperty: field.type.name.csharpType
			ReferenceProperty: "I" + field.dataType.name
		}
		field.generateFieldProperty(typeName)
	}

	def private generateFieldProperty(Feature field, String typeName) {
		val type = if (field.maxOccurs == -1) {
				'''IEnumerable<«typeName.maybeNullable»>'''
			} else {
				'''«typeName.maybeNullable»'''
			}
		'''public «type» «field.name.initialUpper» { get; set; }'''
	}

}
