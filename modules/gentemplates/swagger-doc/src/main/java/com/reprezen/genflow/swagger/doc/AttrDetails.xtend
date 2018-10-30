package com.reprezen.genflow.swagger.doc

class AttrDetails {

	extension AttributeHelper = HelperHelper.attributeHelper
	extension HtmlHelper = HelperHelper.htmlHelper

	var Object obj
	val String id

	new(Object obj) {
		this.obj = obj
		this.id = this.htmlId
	}

	def setObject(Object obj) {
		this.obj = obj
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
				obj.defaultValue?.toString
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
				obj.uniqueItems?.toString
			case "Example":
				obj.example?.toString
			default:
				throw new IllegalArgumentException("Internal error - unexpected detail label: " + detailName)
		}
		if (value !== null) {
			'''<tr><th>«detailName»</th><td>«value»</td></tr>'''
		}
	}

	def private getBounds() {
		val minEq = if(!obj.exclusiveMinimum) "="
		val maxEq = if(!obj.exclusiveMaximum) "="
		val bounds = if (obj.minimum !== null) {
				if (obj.maximum !== null) {
					'''«obj.minimum» <«minEq» «obj.name» <«maxEq» «obj.maximum»'''
				}
			} else if (obj.minimum !== null) {
				'''«obj.name» >«minEq» «obj.minimum»'''
			} else if (obj.maximum !== null) {
				'''«obj.name» <«maxEq» «obj.maximum»'''
			}
		bounds?.htmlEscape
	}

	def private String getLengthBounds() {
		if (obj.minLength > 0 && obj.maxLength < Integer::MAX_VALUE) {
			'''«obj.minLength» &lt;= <em>length</em> &lt;= «obj.maxLength»'''
		} else if (obj.minLength > 0) {
			'''<em>length</em> &gt;= «obj.minLength»'''
		} else if (obj.maxLength < Integer::MAX_VALUE) {
			'''<em>length</em> &lt;= «obj.maxLength»'''
		}
	}

	def private getEnumList() {
		obj.enums?.map[it?.toString?.htmlEscape]?.join("<br>")
	}
}
