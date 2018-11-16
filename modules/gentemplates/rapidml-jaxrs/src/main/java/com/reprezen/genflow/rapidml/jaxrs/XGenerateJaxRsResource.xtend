/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jaxrs

import com.google.common.collect.Collections2
import com.google.common.collect.HashBasedTable
import com.google.common.collect.Iterables
import com.google.common.collect.Table
import com.reprezen.genflow.api.trace.GenTemplateTrace
import com.reprezen.genflow.api.trace.GenTemplateTraceItem
import com.reprezen.genflow.api.trace.GenTemplateTraceSourceItem
import com.reprezen.genflow.api.zenmodel.ZenModelLocator
import com.reprezen.rapidml.HTTPMethods
import com.reprezen.rapidml.HttpMessageParameterLocation
import com.reprezen.rapidml.MatrixParameter
import com.reprezen.rapidml.MessageParameter
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.Parameter
import com.reprezen.rapidml.PrimitiveType
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.SourceReference
import com.reprezen.rapidml.TemplateParameter
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.TypedRequest
import com.reprezen.rapidml.TypedResponse
import com.reprezen.rapidml.ZenModel
import com.reprezen.rapidml.util.PrimitiveTypes
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Collection
import java.util.Date
import java.util.List
import java.util.Map
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.GET
import javax.ws.rs.HEAD
import javax.ws.rs.HeaderParam
import javax.ws.rs.MatrixParam
import javax.ws.rs.OPTIONS
import javax.ws.rs.POST
import javax.ws.rs.PUT
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.xml.datatype.Duration
import javax.xml.datatype.XMLGregorianCalendar
import javax.xml.namespace.QName
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource

import static com.reprezen.genflow.api.trace.GenTemplateTraceUtils.*

class XGenerateJaxRsResource extends JavaXtendTemplate<ServiceDataResource> {

	Map<HTTPMethods, Class<?>> httpMethods = newHashMap( //
		HTTPMethods.GET -> typeof(GET), //
		HTTPMethods.POST -> typeof(POST), //
		HTTPMethods.PUT -> typeof(PUT), //
		HTTPMethods.DELETE -> typeof(DELETE), //
		HTTPMethods.OPTIONS -> typeof(OPTIONS), //
		HTTPMethods.HEAD -> typeof(HEAD) //
	)

	Table<EObject, String, String> zenElement2ClassQName = HashBasedTable::create();

	override generate(ZenModel model, ServiceDataResource serviceDataResource) {
		initZenElement2ClassName(model, serviceDataResource.eResource)
		val packageDeclaration = '''package «getPackageName(serviceDataResource)»;'''
		val body = '''@«getTypeName(typeof(Path))»("«serviceDataResource.URI»")
public interface «getClassName(serviceDataResource)» {
			
	«FOR method : serviceDataResource.methods»
		«generateMethod(method)»
		
	«ENDFOR»
		
}'''
		val String imports = generateImports()

		return '''
			«packageDeclaration»
			
			«imports»
			
			«body»
		'''

	}

	protected def generateMethod(Method method) {
		'''
			«generateHttpMethodAnnotation(method)»
			«generateConsumesAnnotation(method)»
			«generateProducesAnnotation(method)»
			«getResponseType(getOKResponse(method))» «getJavaMethodName(method)»(«generateRequestParameters(method).join(", ")»);
		'''
	}

	protected def generateHttpMethodAnnotation(Method method) {
		'''@«getTypeName(httpMethods.get(method.httpMethod))»'''
	}

	protected def generateRequestParameters(Method method) {
		val List<String> parameters = newArrayList()
		for (uriParam : method.containingResourceDefinition.URI?.uriParameters) {
			val paramType = getPrimitiveType(getSourceReferenceType(uriParam.sourceReference))
			parameters.add(
				'''@«getTypeName(getParameterType(uriParam))»("«uriParam.name»") «getTypeName(paramType)» «escapeJavaKeywords(
					uriParam.name)»''')
		}
		val requestType = getRequestType(method.request)
		if (requestType !== null) {
			val requestInput = '''«requestType» «escapeJavaKeywords(requestType.toFirstLower)»'''
			parameters.add(requestInput)
		}
		return parameters
	}

	protected def PrimitiveType getSourceReferenceType(SourceReference propertyReference) {
		propertyReference.primitiveType
	}

	protected def getParameterType(Parameter parameter) {
		if (parameter instanceof TemplateParameter) {
			return typeof(PathParam)
		}
		if (parameter instanceof MatrixParameter) {
			return typeof(MatrixParam)
		}
		if (parameter instanceof MessageParameter) {
			switch (parameter as MessageParameter) {
				case HttpMessageParameterLocation::QUERY:
					return typeof(QueryParam)
				case HttpMessageParameterLocation::HEADER:
					return typeof(HeaderParam)
			}
		}
	}

	protected def Class<?> getPrimitiveType(PrimitiveType zenType) {

		// see https://docs.oracle.com/cd/E19316-01/819-3669/bnazf/index.html
		switch (PrimitiveTypes::getByTypeName(zenType.name)) {
			case PrimitiveTypes::STRING:
				return typeof(String)
			case PrimitiveTypes::INTEGER:
				return typeof(BigInteger)
			case PrimitiveTypes::INT:
				return typeof(int)
			case PrimitiveTypes::BOOLEAN:
				return typeof(boolean)
			case PrimitiveTypes::DATE:
				return typeof(Date)
			case PrimitiveTypes::DATETIME:
				return typeof(XMLGregorianCalendar)
			case PrimitiveTypes::DECIMAL:
				return typeof(BigDecimal)
			case PrimitiveTypes::DURATION:
				return typeof(Duration)
			case PrimitiveTypes::LONG:
				return typeof(long)
			case PrimitiveTypes::NCNAME:
				return typeof(int)
			case PrimitiveTypes::QNAME:
				return typeof(QName)
			case PrimitiveTypes::TIME:
				return typeof(Date)
			case PrimitiveTypes::FLOAT:
				return typeof(float)
			case PrimitiveTypes::DOUBLE:
				return typeof(double)
			case PrimitiveTypes::ANYURI:
				return typeof(String)
			case PrimitiveTypes::BASE64BINARY:
				return typeof(byte[])
			case PrimitiveTypes::GDAY:
				return typeof(XMLGregorianCalendar)
			case PrimitiveTypes::GMONTH:
				return typeof(XMLGregorianCalendar)
			case PrimitiveTypes::GMONTHDAY:
				return typeof(XMLGregorianCalendar)
			case PrimitiveTypes::GYEAR:
				return typeof(XMLGregorianCalendar)
			case PrimitiveTypes::GYEARMONTH:
				return typeof(XMLGregorianCalendar)
		}
		return null
	}

	protected def String generateConsumesAnnotation(Method method) {
		'''«IF method.request !== null && !method.request.mediaTypes.empty && hasDataType(method.request)»
			@«getTypeName(typeof(Consumes))»(«toAnnotationParameter(method.request.mediaTypes.map[it.name])»)
		«ENDIF»'''
	}

	protected def String generateProducesAnnotation(Method method) {
		val response = getOKResponse(method)
		'''«IF response !== null && !response.mediaTypes.empty && hasDataType(response)»
			@«getTypeName(typeof(Produces))»(«toAnnotationParameter(response.mediaTypes.map[it.name])»)
		«ENDIF»'''
	}

	protected def initZenElement2ClassName(ZenModel model, Resource resource) {
		val GenTemplateTrace trace = context.getPrerequisiteTrace(JaxRsGenTemplate::JAXB_DEPENDENCY);
		val Collection<GenTemplateTraceItem> jaxbClassItems = getTraceItemsOfType(trace, "jaxbClass");
		val ZenModelLocator zenModelLocator = new ZenModelLocator(model);
		for (GenTemplateTraceItem item : jaxbClassItems) {
			val String jaxbClassName = item.getProperties().get("jaxbClassName");
			val String jaxbPackageName = item.getProperties().get("jaxbPackageName");
			val Collection<GenTemplateTraceSourceItem> sourceItems = Collections2.filter(item.getSources(),
				hasSourceRole("sourceData"));
			val GenTemplateTraceSourceItem sourceItem = Iterables.getFirst(sourceItems, null);
			if (sourceItem !== null) {
				val String locator = sourceItem.getLocator();
				val EObject zenElement = zenModelLocator.dereferenceEObject(locator);
				zenElement2ClassQName.put(zenElement, "jaxbClassName", jaxbClassName);
				zenElement2ClassQName.put(zenElement, "jaxbPackageName", jaxbPackageName);
			}
		}
	}

	protected def String getResponseType(TypedResponse response) {
		return getMessageType(response, typeof(Response), "void")
	}

	protected def String getRequestType(TypedRequest response) {
		return getMessageType(response, null, null)
	}

	protected def String getMessageType(TypedMessage message, Class<?> defaultValue, String nullValue) {
		if (message === null) {
			return nullValue
		}
		val Object zenMessageType = if(message.resourceType !== null) message.resourceType else message.dataType
		if (zenMessageType !== null) {
			if (!zenElement2ClassQName.containsRow(zenMessageType)) {

				// TODO report error
				return getTypeName(defaultValue)
			}
			val className = zenElement2ClassQName.get(zenMessageType, "jaxbClassName")
			addImport(zenElement2ClassQName.get(zenMessageType, "jaxbPackageName") + "." + className)
			return className
		}
		return getTypeName(defaultValue)
	}

	protected def String toAnnotationParameter(Collection<String> elements) {
		val opening = if(elements.size > 0) "{" else ""
		val closing = if(elements.size > 0) "}" else ""
		'''«opening»«elements.map["\"" + it + "\""].join(", ")»«closing»'''
	}

	protected def getOKResponse(Method method) {
		method.responses.findFirst[it.statusCode >= 200 && it.statusCode < 300]
	}

	protected def getJavaMethodName(Method method) {
		method.getName()
	}

	def private hasDataType(TypedMessage message) {
		message.actualType !== null || message.resourceType !== null
	}

	def static String getPackageName(ServiceDataResource inputElement) {
		'''com.modelsolv.reprezen.resources.«(inputElement.eContainer.eContainer as ZenModel).name.toLowerCase»'''
	}

	def static String getClassName(ServiceDataResource inputElement) {
		'''«inputElement.name»Resource'''
	}

	def static getFilePath(ServiceDataResource inputElement) {
		getPackageName(inputElement).replaceAll("\\.", "/") + "/" + getClassName(inputElement) + ".java"
	}
}
