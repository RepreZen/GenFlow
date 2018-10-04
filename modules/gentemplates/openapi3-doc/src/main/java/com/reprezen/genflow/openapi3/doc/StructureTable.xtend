package com.reprezen.genflow.openapi3.doc

import com.reprezen.kaizen.oasparser.model3.Parameter
import com.reprezen.kaizen.oasparser.model3.Schema
import java.util.List
import org.apache.commons.lang3.StringUtils

abstract class StructureTable<T> {

	extension protected HtmlHelper = HelperHelper.htmlHelper
	extension protected AttributeHelper = HelperHelper.attributeHelper
	extension protected DocHelper = HelperHelper.docHelper
	extension protected RecursionHelper = HelperHelper.recursionHelper
	extension protected ArrayHelper = HelperHelper.arrayHelper
	extension protected RefHelper = HelperHelper.refHelper
	extension protected OptionHelper = HelperHelper.optionHelper
	extension protected KaiZenParserHelper = new KaiZenParserHelper

	protected val String[][] cols
	protected val T obj

	protected new(T obj, String[]... cols) {
		this.cols = cols
		this.obj = obj
	}

	/* Public method for rendering a table describing an object */
	def String render(String name) {
		'''
			<table class="table table-condensed">
			    «renderHeaderRow»
			    «renderObject(name, null, new Indentation())»
			</table>
		'''
	}

	def protected renderObject(String name, Object referrer, Indentation ind) {
		var Activation activation = null
		try {
			activation = obj.use
			render(name, referrer, ind)
		} catch (BadReferenceException e) {
			new ExceptionStructureTable(e, cols).render(name, referrer, ind)
		} catch (RecursiveRenderException e) {
			new ExceptionStructureTable(e, cols).render(name, referrer, ind)
		} catch (BadArrayException e) {
			new ExceptionStructureTable(e, cols).render(name, referrer, ind)
		} finally {
			activation?.close
		}
	}

	def protected String renderColumn(String colAttr, String name, Object referrer, Indentation ind, AttrDetails det) {
		val chosenName = obj.chooseName(name)
		switch colAttr {
			case "name": chosenName?.htmlEscape?.formatName(chosenName, obj, referrer)?.indentCode(ind)
			case "type": getTypeSpec(det)?.code
			case "doc": getDoc()
			case "details": det?.details(false)?.toString
			default: obj.getAttribute(colAttr).valueForDisplay
		}
	}

	def protected abstract String render(String name, Object referrer, Indentation ind);

	def protected abstract String getTypeSpec(AttrDetails det);

	def protected abstract String getDoc();

	public static class SchemaStructureTable extends StructureTable<Schema> {

		public new(Schema obj, String[]... cols) {
			super(obj, cols)
		}

		override protected render(String name, Object referrer, Indentation ind) {
			if (obj.type == "array") {
				val det = new AttrDetails(obj.elementType)
				val itemsTable = new SchemaStructureTable(obj.elementType, cols)
				return '''
					«defaultRender(name, referrer, ind, det)»
					«itemsTable.renderObject(null, null, ind.advance2)»
				'''
			}

			val det = new AttrDetails(obj)

			val additionalPropertiesSchema = try {
				// See https://github.com/RepreZen/KaiZen-OpenApi-Parser/issues/104 for why this can throw NPE
				obj.getAdditionalPropertiesSchema()?.asNullIfMissing
			} catch (NullPointerException e) {
				null
			}
			val allOfSchemas = obj.allOfSchemas // FIXME asNullIfMissing is always true
			val oneOfSchemas = obj.oneOfSchemas
			val anyOfSchemas = obj.anyOfSchemas

			ind.use2
			ind.advance2

//			if (obj.properties.empty && additionalPropertiesSchema === null && oneOfSchemas.empty) {
//				defaultRender(name, referrer, ind, det)
//			} else {
			val modelRow = if (name !== null)
					defaultRender(name, referrer, ind,
						det)
			'''
				«modelRow»«FOR prop : obj.properties.entrySet»«new SchemaStructureTable(prop.value, cols).renderObject("!" + prop.key, obj, ind.advance2)»«ENDFOR»
				«IF additionalPropertiesSchema !== null»
					«new SchemaStructureTable(additionalPropertiesSchema, cols).renderObject("![additional properties]", obj, ind.advance2)»
				«ENDIF»
				«IF !allOfSchemas.isEmpty»
					«FOR schema: allOfSchemas»
						«renderMemberModel(schema, "allOf", ind.advance2, det)»
					«ENDFOR»
				«ENDIF»
				«IF !oneOfSchemas.isEmpty»
					«FOR schema: oneOfSchemas»
						«renderMemberModel(schema, "oneOf", ind.advance2, det)»
					«ENDFOR»
				«ENDIF»
				«IF !anyOfSchemas.isEmpty»
					«FOR schema: anyOfSchemas»
						«renderMemberModel(schema, "anyOf", ind.advance2, det)»
					«ENDFOR»
				«ENDIF»
			'''
		// }
		}

		override protected getTypeSpec(AttrDetails det) {
			if (obj.type == "array") {
				return '''«obj.arrayTypeSpec»«det.infoButton»'''
			}
			getTypeSpec(obj, det)
		}

		def protected getTypeSpec(Schema model, AttrDetails det) {
			val modelType = model.getDefaultTypeSpec()
			return '''«modelType»«det.infoButton»'''
//			if (model.properties.empty) {
//				val namedType = (if(modelType !== null && !modelType.empty) modelType + ": " else "") + model.type
//				return '''«namedType»«det.infoButton»'''
//			} else {
//				modelType
//			}
		}

		def protected String renderMemberModel(Schema memberModel, String label, Indentation ind, AttrDetails det) {
			'''
				«memberModel.renderMemberRow(label, ind.copy, det)»
				«new SchemaStructureTable(memberModel, cols).renderObject(null, null, ind.copy)»
			'''
		}

		def private String renderMemberRow(Schema member, String label, Indentation ind, AttrDetails det) {
			if (showComponentModels) {
				val text = '''<em>«label»: «member.getTypeSpec(det)?.htmlEscape?.samp»</em>'''
				#[text.toString.indentTextToCode(ind)].wrapHeaderRow
			}
		}

		override protected getDoc() {
			obj.description?.docHtml
		}

	}

	public static class ParameterStructureTable extends StructureTable<Parameter> {

		public new(Parameter obj, String[]... cols) {
			super(obj, cols)
		}

		override protected render(String name, Object referrer, Indentation ind) {
			val Schema detailType = if(obj.schema.type == "array") obj.schema.elementType else obj.schema
			val det = new AttrDetails(detailType)
			'''
				«defaultRender(name, referrer, ind, det)»
			'''
		}

		override protected getTypeSpec(AttrDetails det) {
			val param = obj
			if (param.schema.type == "array") {
				'''«param.schema.arrayTypeSpec»«det.infoButton»'''
			} else {
				'''«param.schema.getDefaultTypeSpec()»«det.infoButton»'''
			}
		}

		override protected getDoc() {
			obj.description?.docHtml
		}

	}

	public static class ParametersStructureTable extends StructureTable<List<Parameter>> {

		public new(List<Parameter> obj, String[]... cols) {
			super(obj, cols)
		}

		override protected render(String name, Object referrer, Indentation ind) {
			if (!obj.
				empty) {
				'''«FOR param : obj»«new ParameterStructureTable(param, cols).renderObject(null, referrer, ind.advance2)»«ENDFOR»'''
			}
		}

		override protected getTypeSpec(AttrDetails det) {
			throw new UnsupportedOperationException("TODO: auto-generated method stub")
		}

		override protected getDoc() {
			throw new UnsupportedOperationException("TODO: auto-generated method stub")
		}

	}

	public static class ExceptionStructureTable extends StructureTable<Exception> {

		protected new(Exception obj, String[]... cols) {
			super(obj, cols)
		}

		override protected render(String name, Object referrer, Indentation ind) {
			defaultRender(name, referrer, ind, null)
		}

		override protected getTypeSpec(AttrDetails det) {
			return getTypeSpec(obj, det)
		}

		/*****************
		 * Attempt to render nonsensical array
		 *****************/
		def private dispatch String getTypeSpec(BadArrayException e, AttrDetails det) {
			"???[]"
		}

		def private dispatch getTypeSpec(BadReferenceException e, AttrDetails det) {
			"ref"
		}

		def private dispatch getTypeSpec(RecursiveRenderException e, AttrDetails det) {
			"(recursive)"
		}
		
		override protected renderColumn(String colAttr, String name, Object referrer, Indentation ind,
			AttrDetails det) {
			if (obj instanceof RecursiveRenderException) {
				renderColumn(obj, colAttr, name, referrer, ind, det)
			} else {
				super.renderColumn(colAttr, name, referrer, ind, det)
			}
		}

		/*****************
		 * Recursive rendering attempt
		 *****************/
		def protected String renderColumn(RecursiveRenderException e, String colAttr, String name, Object referrer,
			Indentation ind, AttrDetails det) {
			val obj = e.object.
				safeResolve
			switch colAttr {
				case "name": {
					val tooltip = ' <a href="#" data-toggle="tooltip" title="Recursive reference to containing object type">&hellip;</a>'
					(obj.chooseName(name).htmlEscape + tooltip).indentCode(ind)
				}
				case "type":
					getTypeSpec(det)?.code
				default:
					obj.getAttribute(colAttr).valueForDisplay
			}
		}

		override protected getDoc() {
			getDoc(obj)
		}

		def private dispatch String getDoc(BadReferenceException e) {
			val refString = e.refString.replaceAll("#/_UNRESOLVABLE/", "")
			'''Invalid Reference: <code>«refString.htmlEscape»</code>'''
		}

		def private dispatch String getDoc(BadArrayException e) {
			e.message
		}

	}

	def protected String renderHeaderRow() {
		cols.map[it.get(1)].wrapHeaderRow
	}

	def protected String defaultRender(String name, Object referrer, Indentation ind, AttrDetails det) {
		'''
			«cols.map[col|renderColumn(col.get(0), name, referrer, ind, det)].wrapRow»
			«renderAttrDetails(name,  referrer, ind, det)?.wrapRow(true)»
		'''
	}

	def private String renderAttrDetails(String name, Object referrer, Indentation ind, AttrDetails det) {
		renderColumn("details", name, referrer, ind, det)
	}

	def protected dispatch String chooseName(Object obj, String offeredName) {
		if (offeredName !== null && offeredName.startsWith("!")) {
			offeredName.substring(1)
		} else {
			#[offeredName, obj.rzveTypeName, obj.name].filter[it !== null].last
		}
	}

	def private String formatName(String formattedName, String rawName, Object obj, Object referrer) {
		if (isRequired(obj, rawName, referrer)) {
			'''<strong>«formattedName»</strong>'''
		} else {
			'''<em>«formattedName»</em>'''
		}
	}

	def private isRequired(Object obj, String name, Object referrer) {
		// obj = value of named property or parameter in referrer
		switch (obj) {
			Parameter: // same for parameters
				return obj.isRequired
			Schema: // all other named things are references to models, and the referrer determines requiredness
				return referrer.requiredProperties.contains(name)
			default:
				throw new IllegalArgumentException(
					"Named item is represented by neither a Property, a Parameter, nor a Model")
		}
	}

	def private List<String> getRequiredProperties(Object referrer) {
		if (referrer !== null) {
			switch (referrer) {
				Schema:
					return referrer.requiredFields.toList
			}
		}
		return #[]
	}

	def protected String getDefaultTypeSpec(Schema obj) {
		#[obj.kaiZenSchemaName, obj.type, obj.rzveTypeName].filter[it !== null].last
	}

	/*****************
	 * ArrayModel covers array schemas
	 *****************/
// FIXME adapt to OAS3
//
//	/*****************
//	 * ComposedModel covers allOf schemas
//	 *****************/
//	def private dispatch String render(ComposedModel model, String name, Object referrer, Indentation ind,
//		AttrDetails det) {
//		ind.use2
//		val componentInd = ind.advance2
//		'''
//			«model.defaultRender(name, referrer, ind, det)»
//			«FOR member : model.allOf»«member.safeResolve.renderMemberModel(componentInd, det)»«ENDFOR»
//		'''
//	}
//
//	/*****************
//	 * ObjectProperty
//	 *****************/
//	def private dispatch String render(ObjectProperty prop, String name, Object referrer, Indentation ind,
//		AttrDetails det) {
//		'''
//			«IF name !== null && !name.empty»«prop.defaultRender(name, referrer, ind, det)»«ENDIF»
//			«FOR field : prop.properties.entrySet»«field.value.renderObject("!" + field.key, prop, ind.advance2)»«ENDFOR»
//		'''
//	}
	/*****************
	 * ArrayProperty
	 *****************/
//	def private dispatch String render(ArrayProperty prop, String name, Object referrer, Indentation ind,
//		AttrDetails det) {
//		det.setObject(prop.elementType)
//		'''
//			«prop.defaultRender(name, referrer, ind, det)»
//			«prop.elementType.renderObject(null, null, ind.advance2)»
//		'''
//	}
//
//	def private dispatch String getTypeSpec(ArrayProperty prop, AttrDetails det) {
//		'''«prop.arrayTypeSpec»«det.infoButton»'''
//	}
//
//	/*****************
//	 * MapProperty
//	 *****************/
//	def private dispatch String render(MapProperty prop, String name, Object referrer, Indentation ind,
//		AttrDetails det) {
//		val apSchema = prop.additionalProperties?.safeResolve
//		'''
//			«prop.defaultRender(name, referrer, ind, det)»
//			«apSchema?.renderObject("![additional properties]", referrer, ind.advance2)»
//		'''
//	}
	/*****************
	 * All other property types (properties with primitive types)
	 *****************/
//	def private dispatch String render(AbstractProperty prop, String name, Object referrer, Indentation ind,
//		AttrDetails det) {
//		'''
//			«IF name !== null»«prop.defaultRender(name, referrer, ind, det)»«ENDIF»
//		'''
//	}
//
//	def private dispatch String getTypeSpec(AbstractProperty prop, AttrDetails det) {
//		'''«prop.getDefaultTypeSpec(det)»«det.infoButton»'''
//	}
	def protected dispatch String chooseName(Parameter param, String offeredName) {
		param.name
	}

	/*****************
	 * Unresolvable Ref
	 *****************/
	def protected dispatch chooseName(BadReferenceException e, String offeredName) {
		if(offeredName !== null && offeredName.startsWith("!")) offeredName.substring(1) else offeredName
	}

	/*****************
	 * Utility methods
	 *****************/
	def private String wrapRow(String value, boolean noBorder) {
		#[value].wrapRow(noBorder)
	}

	def private String wrapRow(List<String> values) {
		values.wrapRow(false)
	}

	def private String wrapRow(List<String> values, boolean noBorder) {
		val style = if (noBorder) {
				' style="border-top:0px"'
			}
		'''<tr>«FOR value : values»<td«values.colSpan»«style»>«value»</td>«ENDFOR»</tr>'''
	}

	def protected String wrapHeaderRow(List<String> values) {
		'''<tr>«FOR value : values»<th«values.colSpan»>«value»</th>«ENDFOR»</tr>'''
	}

	def getColSpan(List<String> values) {
		if (values.size == 1 && cols.size > 1) {
			''' colspan="«cols.size»"'''
		}
	}

	def protected String indentTextToCode(String s, Indentation ind) {
		ind.use1
		getIndentation(ind.n1 + ind.n2).samp + s
	}

	def protected String indentCode(String s, Indentation ind) {
		ind.use1
		ind.use2
		ind.n1.indentation.samp + (ind.n2.indentation + s).code
	}

	def private getIndentation(int n) {
		StringUtils::repeat("&nbsp;&nbsp;", n)
	}

	def protected String getValueForDisplay(Object o) {
		o?.formatValue
	}

	def private dispatch String formatValue(String s) {
		s?.htmlEscape
	}

	def private dispatch String formatValue(List<?> list) {
		'''«FOR item : list SEPARATOR "<br>"»item?.toString?.htmlEscape«ENDFOR»'''
	}
}

/**
 * Surprisingly, indentation was one of the hardest things to get right in this module! Here's how it works.
 * <p>
 * There are two sorts of indented texts used in the tables:
 * <ul>
 * <li>code items: blank indentation followed by shaded indentation followed by shaded monospaced text. Blank
 * indentation is in a "samp" element to produce monospaced text. Second indentation and text are joined within
 * a "code" element, which provides monospacing and shading.
 * <li>text item: blank indentation followed by text. Indentation is in a "samp" block for monospacing, text is
 * left as-is.
 * <ul>
 * The Indentation class maintains two indentation levels, called n1 and n2, which control the width of the 
 * plain and shaded indentations. For a new table, both start out at zero.
 * <p>
 * Certain structures call for advancing these indentation levels. In all cases, this results in a new
 * Indentation object which is passsed in nested calls, so when those calls unwind the prevailing indentation
 * object is unchanged.
 * <p>
 * The tricky part turned out to be knowing whether to ignore indentation changes. The answer is keeping
 * track of whether anything's actually been output using the current indentation levels. If so, the advance
 * is performed, and either way, a new Indentation object is created. So, for example, the properties of a
 * top-level ObjectProperty will be at indentation level (0,0) even though its rendering method calls for 
 * advancing the n2 value for the nested properties.
 * <p>
 * Advancing the n1 value really means setting n1 to n1+n2 and n2 to zero. The n1 value is only used when 
 * outputing the names of schemas that contribut to an allOf schema.
 */
package class Indentation {

	val private int n1
	val private int n2
	var private boolean used1 = false
	var private boolean used2 = false

	new() {
		this(0, 0)
	}

	new(int n1, int n2) {
		this.n1 = n1
		this.n2 = n2
	}

	new(Indentation ind) {
		this.n1 = ind.n1
		this.n2 = ind.n2
		this.used1 = ind.used1
		this.used2 = ind.used2
	}

	def copy() {
		new Indentation(this)
	}

	def advance1() {
		if(used1 || used2) new Indentation(n1 + n2 + 1, 0) else new Indentation(this)
	}

	def advance2() {
		if (used2) {
			val adv = new Indentation(n1, n2 + 1)
			if (used1) {
				adv.use1
			}
			adv
		} else {
			copy
		}
	}

	def getN1() {
		n1
	}

	def getN2() {
		n2
	}

	def use1() {
		used1 = true
	}

	def use2() {
		used2 = true
	}
}
