package com.reprezen.genflow.openapi3.doc

import com.google.common.base.Strings
import com.reprezen.kaizen.oasparser.model3.Schema

class AttrDetails {

	extension HtmlHelper = HelperHelper.htmlHelper
	extension KaiZenParserHelper = new KaiZenParserHelper

	val private Schema obj
	val private String id

	new(Schema obj) {
		this.obj = obj
		this.id = this.htmlId
	}

	def details(boolean topLevel) {
		val details = detailRows
		if (!details.empty) {
			val detailsTable = details.detailsTable
			if (topLevel) {
				// wrapping in an outer table to make it look the same as in the case where there really is 
				// an outer table that contains this. Otherwise bootstrap css gives it an odd look
				'''<table class="table table-condensed" style="margin:0"><tr><td style="border-top: 0px">«detailsTable»</td></tr></table>'''
			} else {
				detailsTable
			}

		}
	}

	def private detailsTable(String rows) {
		'''
			<div id="«id»" class="well well-sm collapse" data-controller="#«id»-controller" style="margin:0">
			    <table class="table" style="margin:0">«rows»</table>
			</div>
		'''
	}

	def getInfoButton() {
		if(!detailRows.empty) getInfoButton(id)
	}

	def private getInfoButton(String id) {
		'''
			<a href="#«id»" data-toggle="collapse">
			    <span id="«id»-controller" data-toggle="tooltip" 
			        data-hidden-title="View Property Details" data-visible-title="Hide Property Details" 
			        class="glyphicon glyphicon-collapse-down">
			    </span>
			</a>
		'''
	}

	def private String getDetailRows() {
		'''
			«detailRow("Format")»
			«detailRow("Default")»
			«detailRow("Bounds")»
			«detailRow("Multiple Of")»
			«detailRow("Length")»
			«detailRow("Pattern")»
			«detailRow("Allowed Values")»
			«detailRow("Unique Values")»
			«detailRow("Example")»
		'''
	}

	def private detailRow(String detailName) {
		val String value = switch detailName {
			case "Format":
				obj.format
			case "Default":
				obj.^default?.toString
			case "Bounds":
				bounds
			case "Multiple Of":
				obj.multipleOf?.toString
			case "Length":
				lengthBounds
			case "Pattern":
				obj.pattern?.htmlEscape
			case "Allowed Values":
				enumList
			case "Unique Values":
				obj.getUniqueItems?.toString
			case "Example":
				obj.example?.toString
			default:
				throw new IllegalArgumentException("Internal error - unexpected detail label: " + detailName)
		}
		if (!Strings.isNullOrEmpty(value)) {
			'''<tr><th>«detailName»</th><td>«value»</td></tr>'''
		}
	}

	def private getBounds() {
		val minEq = if(obj.getExclusiveMinimum!=null && !obj.getExclusiveMinimum) "="
		val maxEq = if(obj.getExclusiveMaximum!=null && !obj.getExclusiveMaximum) "="
		val bounds = if (obj.minimum !== null) {
				if (obj.maximum !== null) {
					'''«obj.minimum» <«minEq» «obj.title» <«maxEq» «obj.maximum»'''
				}
			} else if (obj.minimum !== null) {
				'''«obj.title» >«minEq» «obj.minimum»'''
			} else if (obj.maximum !== null) {
				'''«obj.title» <«maxEq» «obj.maximum»'''
			}
		bounds?.htmlEscape
	}

	def private String getLengthBounds() {
		if ((obj.minLength!=null && obj.minLength > 0) && (obj.maxLength !=null && obj.maxLength < Integer::MAX_VALUE)) {
			'''«obj.minLength» &lt;= <em>length</em> &lt;= «obj.maxLength»'''
		} else if (obj.minLength !=null && obj.minLength > 0) {
			'''<em>length</em> &gt;= «obj.minLength»'''
		} else if (obj.maxLength!=null && obj.maxLength < Integer::MAX_VALUE) {
			'''<em>length</em> &lt;= «obj.maxLength»'''
		}
	}

	def private getEnumList() {
		obj.enums?.map[it.asNullIfMissing?.toString?.htmlEscape]?.join("<br>")
	}
	
}
