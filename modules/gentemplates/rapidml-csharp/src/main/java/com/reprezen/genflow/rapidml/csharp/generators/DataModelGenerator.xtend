package com.reprezen.genflow.rapidml.csharp.generators

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.helpers.FileHelper
import com.reprezen.genflow.rapidml.csharp.helpers.FileRole
import com.reprezen.genflow.rapidml.csharp.helpers.NameHelper
import com.reprezen.genflow.rapidml.csharp.helpers.TypeHelper
import com.reprezen.rapidml.EnumConstant
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.Feature
import com.reprezen.rapidml.PrimitiveProperty
import com.reprezen.rapidml.ReferenceProperty
import com.reprezen.rapidml.Structure
import com.reprezen.rapidml.ZenModel

import static com.reprezen.genflow.rapidml.csharp.helpers.UtilsHelper.*

import static extension com.reprezen.genflow.rapidml.csharp.helpers.DocHelper.*

class DataModelGenerator {
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
			for (type : dataModel.ownedDataTypes.filter[it instanceof Enumeration].map[it as Enumeration]) {
				val extension fileHelper = FileHelper.of(type, FileRole.ENUMS, context, config)
				type.generate.writeFile(type.name.csharpFileName)
				type.enumerationName.noteEnumerationType
			}
			for (type : dataModel.ownedDataTypes.filter[it instanceof Structure].map[it as Structure]) {
				val extension fileHelper = FileHelper.of(type, FileRole.INTERFACE, context, config)
				type.generate.writeFile('''I«type.name»'''.csharpFileName)
			}
		}
	}

	def private String generate(Structure structure) {
		val name = structure.typeInterfaceName
		'''
			«structure.serializationAttributes»
			«generatedAttr»
			public interface «name» {
			    «FOR Feature field : structure.ownedFeatures»
			    	«field.simpleDoc»«field.serializationAttributes»«field.generate»
			    	
			    «ENDFOR»
			}
			
		'''
	}

	def private generate(Feature field) {
		val typeName = switch (field) {
			PrimitiveProperty: field.type.name.csharpType
			ReferenceProperty: "I" + field.dataType.name
		}
		field.generate(typeName)
	}

	def private generate(Feature field, String typeName) {
		val type = if (field.maxOccurs == -1) {
				'''IEnumerable<«typeName.maybeNullable»>'''
			} else {
				'''«typeName.maybeNullable»'''
			}
		'''«type» «field.name.initialUpper» { get; set; }'''
	}

	def private generate(Enumeration enumeration) {
		val name = enumeration.enumerationName
		val defaultInts = enumeration.canUseDefaultInts
		'''
			«enumeration.simpleDoc»«generatedAttr»
			public enum «name» {
			    «FOR constant : enumeration.enumConstants SEPARATOR ","»
			    	«constant.generate(defaultInts)»
			    «ENDFOR»
			}
			
			«IF enumeration.baseType.name == "string"»«enumeration.genValueExtension»«ENDIF»
		'''
	}

	def private generate(EnumConstant enumConstant, boolean defaultInts) {
		'''
			«enumConstant.simpleDoc»«enumConstant.serializationAttributes»
			«enumConstant.name»«IF !defaultInts» = «enumConstant.integerValue»«ENDIF»
		'''
	}

	def private canUseDefaultInts(Enumeration enumeration) {
		if (enumeration.baseType.name == "string") {
			return true
		}
		for (enumConstant : enumeration.enumConstants) {
			if (enumConstant.implicitIntegerValue != enumConstant.integerValue) {
				return false
			}
		}
		return true
	}

	def private genValueExtension(Enumeration enumeration) {
		val name = enumeration.enumerationName
		'''
			public static class «name»Extension {
			    public static string ToValue(this «name» enumConstant) {
			        switch (enumConstant) {
			            «FOR enumConstant : enumeration.enumConstants»
			            	case «name».«enumConstant.name»: 
			            	    return "«enumConstant.literalValue»";
			            «ENDFOR»
			            default:
			                throw new ArgumentException("Illegal value for enumeration '«enumeration.enumerationName»': " + ((int) enumConstant));
			        }
			    }
			}
		'''
	}

	def private getSerializationAttributes(Structure structure) {
		'''
			«IF config.isGenerateJsonSerialization»[JsonObject(MemberSerialization.OptIn)]«ENDIF»
		'''
	}

	def private getSerializationAttributes(Feature field) {
		'''
			«IF config.isGenerateJsonSerialization»
				[JsonProperty("«field.name»")]
				«IF field.isEnumField»[JsonConverter(typeof(StringEnumConverter))]«ENDIF»
			«ENDIF»
		'''
	}

	def private isEnumField(Feature field) {
		field instanceof PrimitiveProperty && (field as PrimitiveProperty).type instanceof Enumeration
	}

	def private getSerializationAttributes(EnumConstant enumConstant) {
		'''«IF config.isGenerateJsonSerialization»[EnumMember]«ENDIF»'''
	}

}
