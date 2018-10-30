package com.reprezen.genflow.swagger.doc

import io.swagger.models.ArrayModel
import io.swagger.models.ComposedModel
import io.swagger.models.Model
import io.swagger.models.ModelImpl
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.SerializableParameter
import io.swagger.models.properties.AbstractProperty
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.MapProperty
import io.swagger.models.properties.ObjectProperty
import io.swagger.models.properties.Property
import java.util.List
import org.apache.commons.lang3.StringUtils

class StructureTable {

	extension HtmlHelper = HelperHelper.htmlHelper
	extension AttributeHelper = HelperHelper.attributeHelper
	extension DocHelper = HelperHelper.docHelper
	extension RecursionHelper = HelperHelper.recursionHelper
	extension ArrayHelper = HelperHelper.arrayHelper
	extension RefHelper = HelperHelper.refHelper
	extension OptionHelper = HelperHelper.optionHelper

	val String[][] cols

	def static get(Swagger swagger, String[]... cols) {
		return new StructureTable(swagger, cols)
	}

	private new(Swagger swagger, String[]... cols) {
		this.cols = cols
	}

	/* Public method for rendering a table describing an object */
	def String render(Object obj, String name, Object referrer) {
		'''
			<table class="table table-condensed">
			    «obj.renderHeaderRow»
			    «obj.renderObject(name, referrer, new Indentation())»
			</table>
		'''
	}

	def private renderObject(Object obj, String name, Object referrer, Indentation ind) {
		val resolved = obj.resolve
		var Activation activation = null
		try {
			activation = resolved.use
			resolved.render(name, referrer, ind, new AttrDetails(resolved))
		} catch (BadReferenceException e) {
			e.render(name, referrer, ind, null)
		} catch (RecursiveRenderException e) {
			e.render(name, referrer, ind, null)
		} catch (BadArrayException e) {
			e.render(name, referrer, ind, null)
		} finally {
			activation?.close
		}
	}

	def private dispatch String render(Object obj, String name, Object referrer, Indentation ind, AttrDetails det) {
		obj.defaultRender(name, referrer, ind, det)
	}

	def private String renderHeaderRow(Object obj) {
		cols.map[col|obj.renderHeaderColumn(col.get(1))].wrapHeaderRow
	}

	def private String renderHeaderColumn(Object obj, String text) {
		text
	}

	def private String defaultRender(Object obj, String name, Object referrer, Indentation ind, AttrDetails det) {
		'''
			«cols.map[col|obj.renderColumn(col.get(0), name, referrer, ind, det)].wrapRow»
			«obj.renderAttrDetails(name,  referrer, ind, det)?.wrapRow(true)»
		'''
	}

	def private String renderAttrDetails(Object obj, String name, Object referrer, Indentation ind, AttrDetails det) {
		obj.renderColumn("details", name, referrer, ind, det)
	}

	def private dispatch String renderColumn(Object obj, String colAttr, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		val chosenName = obj.chooseName(name)
		switch colAttr {
			case "name": chosenName?.htmlEscape?.formatName(chosenName, obj, referrer)?.indentCode(ind)
			case "type": obj.getTypeSpec(det)?.code
			case "doc": obj.getDoc
			case "details": det?.details(false)?.toString
			default: obj.getAttribute(colAttr).valueForDisplay
		}
	}

	def private dispatch String chooseName(Object obj, String offeredName) {
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
			Property: // all Property instances are inlined and so know whether they're required
				return obj.required
			Parameter: // same for parameters
				return obj.required
			Model: // all other named things are references to models, and the referrer determines requiredness
				return referrer.requiredProperties.contains(name)
			default:
				throw new IllegalArgumentException(
					"Named item is represented by neither a Property, a Parameter, nor a Model")
		}
	}

	def private List<String> getRequiredProperties(Object referrer) {
		if (referrer !== null) {
			switch (referrer) {
				ModelImpl:
					return referrer.required
				ObjectProperty:
					return referrer.requiredProperties
			}
		}
		return #[]
	}

	def private dispatch String getTypeSpec(Object obj, AttrDetails det) {
		obj.getDefaultTypeSpec(det)
	}

	def private String getDefaultTypeSpec(Object obj, AttrDetails det) {
		#[obj.type, obj.rzveTypeName].filter[it !== null].last
	}

	def private dispatch String getDoc(Object obj) {
		obj.description?.docHtml
	}

	/*****************
	 * ModelImpl covers both object schemas and primitive type schemas
	 *****************/
	def private dispatch String render(ModelImpl model, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		val apSchema = model.additionalProperties?.safeResolve
		if (model.properties.empty && apSchema === null) {
			model.defaultRender(name, referrer, ind, det)
		} else {
			val modelRow = if (name !== null)
					model.defaultRender(name, referrer, ind, det)
			'''
				«modelRow»«FOR prop : model.properties.entrySet»«prop.value.renderObject("!" + prop.key, model, ind.advance2)»«ENDFOR»
				«apSchema?.renderObject("![additional properties]", model, ind.advance2)»
			'''
		}
	}

	def private dispatch getTypeSpec(ModelImpl model, AttrDetails det) {
		if (model.properties.empty) {
			val modelType = model.getDefaultTypeSpec(det)
			val namedType = (if(!modelType.empty) modelType + ": ") + model.type
			return '''«namedType»«det.infoButton»'''
		} else {
			model.getDefaultTypeSpec(det)
		}
	}

	/*****************
	 * ArrayModel covers array schemas
	 *****************/
	def private dispatch String render(ArrayModel model, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		det.setObject(model.elementType)
		'''
			«model.defaultRender(name, referrer, ind, det)»
			«model.elementType.renderObject(null, null, ind.advance2)»
		'''
	}

	def private dispatch String getTypeSpec(ArrayModel model, AttrDetails det) {
		'''«model.arrayTypeSpec»«det.infoButton»'''
	}

	/*****************
	 * ComposedModel covers allOf schemas
	 *****************/
	def private dispatch String render(ComposedModel model, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		ind.use2
		val componentInd = ind.advance2
		'''
			«model.defaultRender(name, referrer, ind, det)»
			«FOR member : model.allOf»«member.safeResolve.renderMemberModel(componentInd, det)»«ENDFOR»
		'''
	}

	def private String renderMemberModel(Object member, Indentation ind, AttrDetails det) {
		val memberModel = member as Model
		// next test guards against a swagger-parser bug: an allOf model with only one component model (which is 
		// allowed per specifications) ends up with two members, one of which is a a completely empty ModelImpl
		// (show    s up as "{}" if the overall spec is rendered as YAML)
		if (memberModel instanceof ModelImpl) {
			var mi = memberModel as ModelImpl
			if (mi.type == "object" && mi.properties.empty && mi.additionalProperties === null) {
				return null;
			}
		}
		'''
			«memberModel.renderMemberRow(ind.copy, det)»
			«memberModel.renderObject(null, null, ind.copy)»
		'''
	}

	def private String renderMemberRow(Model member, Indentation ind, AttrDetails det) {
		if (showComponentModels) {
			val text = '''<em>component model: «member.getTypeSpec(det)?.htmlEscape?.samp»</em>'''
			#[text.toString.indentTextToCode(ind)].wrapHeaderRow
		}
	}

	/*****************
	 * ObjectProperty
	 *****************/
	def private dispatch String render(ObjectProperty prop, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		'''
			«IF name !== null && !name.empty»«prop.defaultRender(name, referrer, ind, det)»«ENDIF»
			«FOR field : prop.properties.entrySet»«field.value.renderObject("!" + field.key, prop, ind.advance2)»«ENDFOR»
		'''
	}

	/*****************
	 * ArrayProperty
	 *****************/
	def private dispatch String render(ArrayProperty prop, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		det.setObject(prop.elementType)
		'''
			«prop.defaultRender(name, referrer, ind, det)»
			«prop.elementType.renderObject(null, null, ind.advance2)»
		'''
	}

	def private dispatch String getTypeSpec(ArrayProperty prop, AttrDetails det) {
		'''«prop.arrayTypeSpec»«det.infoButton»'''
	}

	/*****************
	 * MapProperty
	 *****************/
	def private dispatch String render(MapProperty prop, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		val apSchema = prop.additionalProperties?.safeResolve
		'''
			«prop.defaultRender(name, referrer, ind, det)»
			«apSchema?.renderObject("![additional properties]", referrer, ind.advance2)»
		'''
	}

	/*****************
	 * All other property types (properties with primitive types)
	 *****************/
	def private dispatch String render(AbstractProperty prop, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		'''
			«IF name !== null»«prop.defaultRender(name, referrer, ind, det)»«ENDIF»
		'''
	}

	def private dispatch String getTypeSpec(AbstractProperty prop, AttrDetails det) {
		'''«prop.getDefaultTypeSpec(det)»«det.infoButton»'''
	}

	/*****************
	 * List<Parameter> - list of operation parameters
	 *****************/
	def private dispatch String render(List<Parameter> params, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		if (!params.empty) {
			'''«FOR param : params»«param.renderObject(null, referrer, ind.advance2)»«ENDFOR»'''
		}
	}

	/*****************
	 * SerializableParameter 
	 *****************/
	def private dispatch String render(SerializableParameter param, String name, Object referrer, Indentation ind,
		AttrDetails det) {
		val detailType = if(param.type == "array") param.elementType else param
		det.setObject(detailType)
		'''
			«param.defaultRender(name, referrer, ind, det)»
		'''
	}

	def private dispatch String chooseName(SerializableParameter param, String offeredName) {
		param.name
	}

	def private dispatch String getTypeSpec(SerializableParameter param, AttrDetails det) {
		if (param.type == "array") {
			'''«param.arrayTypeSpec»«det.infoButton»'''
		} else {
			'''«param.getDefaultTypeSpec(det)»«det.infoButton»'''
		}
	}

	/*****************
	 * Unresolvable Ref
	 *****************/
	def private dispatch chooseName(BadReferenceException e, String offeredName) {
		if(offeredName !== null && offeredName.startsWith("!")) offeredName.substring(1) else offeredName
	}

	def private dispatch getTypeSpec(BadReferenceException e, AttrDetails det) {
		"ref"
	}

	def private dispatch String getDoc(BadReferenceException e) {
		val refString = e.refString.replaceAll("#/_UNRESOLVABLE/", "")
		'''Invalid Reference: <code>«refString.htmlEscape»</code>'''
	}

	/*****************
	 * Recursive rendering attempt
	 *****************/
	def private dispatch String renderColumn(RecursiveRenderException e, String colAttr, String name, Object referrer,
		Indentation ind, AttrDetails det) {
		val obj = e.object.safeResolve
		switch colAttr {
			case "name": {
				val tooltip = ' <a href="#" data-toggle="tooltip" title="Recursive reference to containing object type">&hellip;</a>'
				(obj.chooseName(name).htmlEscape + tooltip).indentCode(ind)
			}
			case "type":
				obj.getTypeSpec(det)?.code
			default:
				obj.getAttribute(colAttr).valueForDisplay
		}
	}

	/*****************
	 * Attempt to render nonsensical array
	 *****************/
	def private dispatch String getTypeSpec(BadArrayException e, AttrDetails det) {
		"???[]"
	}

	def private dispatch String getDoc(BadArrayException e) {
		e.message
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

	def private String wrapHeaderRow(List<String> values) {
		'''<tr>«FOR value : values»<th«values.colSpan»>«value»</th>«ENDFOR»</tr>'''
	}

	def getColSpan(List<String> values) {
		if (values.size == 1 && cols.size > 1) {
			''' colspan="«cols.size»"'''
		}
	}

	def private String indentTextToCode(String s, Indentation ind) {
		ind.use1
		getIndentation(ind.n1 + ind.n2).samp + s
	}

	def private String indentCode(String s, Indentation ind) {
		ind.use1
		ind.use2
		ind.n1.indentation.samp + (ind.n2.indentation + s).code
	}

	def private getIndentation(int n) {
		StringUtils::repeat("&nbsp;&nbsp;", n)
	}

	def private String getValueForDisplay(Object o) {
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

	val int n1
	val int n2
	var boolean used1 = false
	var boolean used2 = false

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
