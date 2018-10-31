package com.reprezen.genflow.rapidml.csharp.generators

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.helpers.FileHelper
import com.reprezen.genflow.rapidml.csharp.helpers.FileRole
import com.reprezen.genflow.rapidml.csharp.helpers.NameHelper
import com.reprezen.genflow.rapidml.csharp.helpers.SnippetHelper
import com.reprezen.genflow.rapidml.csharp.helpers.TypeHelper
import com.reprezen.rapidml.CollectionResource
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Method
import com.reprezen.rapidml.ObjectResource
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.ServiceDataResource
import com.reprezen.rapidml.TypedMessage
import com.reprezen.rapidml.ZenModel
import java.net.URI
import java.util.List
import java.util.Set

import static com.reprezen.genflow.rapidml.csharp.helpers.UtilsHelper.*

import static extension com.reprezen.genflow.rapidml.csharp.helpers.DocHelper.*

class ResourceAPIGenerator {

	val ZenModel model
	val IGenTemplateContext context
	val Config config
	extension NameHelper nameHelper
	extension TypeHelper typeHelper
	extension SnippetHelper snippetHelper

	// outline of what's generated for a resource:
	// * Controller interface: defines what all the methods a controller should implement
	//
	// The rest are only if the gentarget calls for a handler-based controller
	// * Controller impl that defines and invokes three static delegates for each request
	// - A "PreProcess" delegate (validation of input etc.)
	// - A "Process" delegate (actual implementation)
	// - A PostPorocess" delegate (validation of response, realization trimming, etc.)
	// Handlers can use exceptions for either abnormal responses or alternate successful responses
	// as defined in the model
	//
	// * An abstract class for implementation of handlers for the delegates. Constructor
	// can be used to wire handler methods to delegates in the controller (this can be
	// managed with DI)
	// - Handler need not subscribe to all events; generated controller thows not-implemented
	// for any Process delegate that has no handler and does nothing for Pre and Post
	//
	// Developers can either implement the controller interface, or extend the abstract handler
	// in order to add business logic.
	new(ZenModel model, IGenTemplateContext context, Config config) {
		this.model = model
		this.context = context
		this.config = config
		this.nameHelper = NameHelper.forModel(model)
		this.typeHelper = TypeHelper.forModel(model)
		this.snippetHelper = new SnippetHelper(config)
	}

	def generate() {
		for (api : model.resourceAPIs) {
			for (resource : api.ownedResourceDefinitions) {
				resource.generateInterface
				if (config.isGenerateDelegateController) {
					resource.generateDelegateController
					resource.generateAbstractDelegateHandler
				}
			}
		}
	}

	def private generateInterface(ResourceDefinition resource) {
		val name = resource.resourceName
		val extension fileHelper = FileHelper.of(resource, FileRole.INTERFACE, context, config)
		fileHelper.useDataModels(resource)
		val content = '''
			«resource.simpleDoc»«generatedAttr»
			public interface I«name»Controller {
			    «FOR method : resource.methods»
			    	«method.returnType» «method.name.initialUpper»(«method.signature»);
			    «ENDFOR»
			}
		'''
		content.writeFile('''I«name»Controller'''.csharpFileName)
	}

	def private generateDelegateController(ResourceDefinition resource) {
		val name = resource.resourceName
		val api = resource.eContainer as ResourceAPI
		val extension fileHelper = FileHelper.of(resource, FileRole.DELEGATE_CONTROLLER, context, config)
		fileHelper.useDataModels(resource)
		val content = '''
			«resource.simpleDoc»«api.routingAttrs»«generatedAttr»
			public partial class «name»DelegateController : «apiController», I«name»Controller {
			    «FOR method : resource.methods»
			    	«val methName = method.name.initialUpper»
			    	
			    	public delegate void «methName»_PreProcessDelegate(«method.signature»);
			    	public static «methName»_PreProcessDelegate «methName»_PreProcess;
			    	public delegate «method.delegateReturnType» «methName»_ProcessDelegate(«method.signature»);
			    	public static «methName»_ProcessDelegate «methName»_Process;
			    	public delegate void «methName»_PostProcessDelegate(«method.getSignature(true)»);
			    	public static «methName»_PostProcessDelegate «methName»_PostProcess;
			    «ENDFOR»
			
			    «FOR method : resource.methods»
			    	«val methName = method.name.initialUpper»
			    	«val notVoid = method.returnType != "void"»
			    	
			    	«method.routingAttrs»
			    	public «method.returnType» «methName»(«method.getSignature(false, true)») {
			    	    «method.responses.head.valueType.delegateVarDecl»
			    	    if («methName»_PreProcess != null) {
			    	        «methName»_PreProcess(«method.args»);
			    	    }
			    	    if («methName»_Process != null) {
			    	        «IF notVoid»«delegateVar» = «ENDIF»«methName»_Process(«method.args»);
			    	    } else {
			    	        throw new NotImplementedException();
			    	    }
			    	    if («methName»_PostProcess != null«postProcessExtraCond») {
			    	        «methName»_PostProcess(«method.getArgs(true)»);
			    	    }
			    	    «delegateReturn(notVoid)»
			    	}
			    «ENDFOR»
			}
		'''
		content.writeFile('''«name»DelegateController'''.csharpFileName)
	}

	def private generateAbstractDelegateHandler(ResourceDefinition resource) {
		val name = resource.resourceName
		val extension fileHelper = FileHelper.of(resource, FileRole.ABSTRACT_DELEGATE_HANDLER, context, config)
		fileHelper.using(resource.eContainer as ResourceAPI)
		fileHelper.useDataModels(resource)
		val content = '''
			public abstract class Abstract«name»Handlers {
			
			    /// <summary>
			    /// Bind handlers to delegates here. For example:
			    ///    «name»DelegateController.MethodName_Process += MethodName;
			    /// Unbound handlers will not be used. Handlers that are not overridden act as follows:
			    /// * PreProcess handlers do nothing
			    /// * Process handlers throw NotImplementedException
			    /// * PostProcess handlers do nothing
			    /// </summary>
			    public Abstract«name»Handlers() {}
			    
			    «FOR method : resource.methods»
			    	«val methName = method.name.initialUpper»
			    	
			    	protected virtual void «methName»_PreProcess(«method.signature») { }
			    	protected virtual «method.delegateReturnType» «methName»(«method.signature») {
			    	    throw new NotImplementedException();
			    	}
			    	protected virtual void «methName»_PostProcess(«method.getSignature(true)») { }
			    «ENDFOR»
			}
		'''
		content.writeFile('''Abstract«name»DelegateHandler'''.csharpFileName)
	}

	def private useDataModels(FileHelper helper, ResourceDefinition resource) {
		val Set<DataModel> usedDataModels = newHashSet
		for (method : resource.methods) {
			usedDataModels.add(method.request?.resourceType?.structure?.eContainer as DataModel)
			for (response : method.responses) {
				usedDataModels.add(response.resourceType?.structure?.eContainer as DataModel)
			}
		}
		helper.using(usedDataModels.filter[it !== null].toList.toArray)
	}

	def private isVoidReturn(Method method) {
		method.responses.head.underlyingStructure === null
	}

	def private getReturnType(Method method) {
		method.responses.head.messageType ?: voidMethodResponseType
	}

	def private getDelegateReturnType(Method method) {
		method.responses.head.delegateType ?: voidDelegateType
	}

	def private getRequestType(Method method) {
		method.request?.valueType
	}

	def private getMessageType(TypedMessage msg) {
		msg.valueType?.methodResponseType
	}

	def private getDelegateType(TypedMessage msg) {
		msg.valueType?.delegateType
	}

	def private String getValueType(TypedMessage msg) {
		val typeName = msg.underlyingStructure?.name?.initialUpper
		if (typeName !== null) {
			if (msg.resourceType instanceof CollectionResource) '''IEnumerable<I«typeName»>''' else "I" + typeName
		}
	}

	def private getUnderlyingStructure(TypedMessage msg) {
		msg.dataType ?: msg.msgResourceDataType ?: msg.containingResourceDataType
	}

	def private getMsgResourceDataType(TypedMessage msg) {
		msg.resourceType?.resourceDataType
	}

	def private getContainingResourceDataType(TypedMessage msg) {
		if (msg.isUseParentTypeReference) {
			(msg.eContainer as Method).containingResourceDefinition.resourceDataType
		}
	}

	def private getResourceDataType(ResourceDefinition resource) {
		switch (resource) {
			ServiceDataResource: resource.dataType
		}
	}

	def private getStructure(ResourceDefinition resource) {
		switch (resource) {
			ObjectResource: resource.dataType
			CollectionResource: resource.dataType
		}
	}

	def private getSignature(Method method) {
		method.getSignature(false)
	}

	def private getSignature(Method method, boolean includeResponse) {
		method.getSignature(includeResponse, false)
	}

	def private getSignature(Method method, boolean includeResponse, boolean includeBodyAttr) {
		val List<String> params = newArrayList();
		for (param : method.containingResourceDefinition.URI.uriParameters ?: #[]) {
			params.add('''«param.type.name.csharpType» «param.name»''')
		}
		for (param : method.request.parameters) {
			params.add('''«param.type.name.csharpType» «param.name»''')
		}
		if (method.requestType !== null) {
			params.add('''«IF includeBodyAttr»[FromBody] «ENDIF»«method.requestType» requestPayload''')
		}
		if (includeResponse && !method.isVoidReturn) {
			params.add('''«method.responses.head.valueType» responsePayload''')
		}

		params.join(", ")
	}

	def private getArgs(Method method) {
		method.getArgs(false)
	}

	def private getArgs(Method method, boolean includeResponse) {
		val List<String> args = newArrayList();
		for (param : method.containingResourceDefinition.URI.uriParameters ?: #[]) {
			args.add(param.name)
		}
		for (param : method.request.parameters) {
			args.add(param.name)
		}
		if (method.requestType !== null) {
			args.add("requestPayload")
		}
		if (includeResponse && !method.isVoidReturn) {
			args.add(responseValue);
		}

		args.join(", ")
	}

	def private getRoutingAttrs(ResourceAPI api) {
		var prefix = new URI(api.baseURI).path
		if (prefix.startsWith("/")) {
			prefix = prefix.substring(1)
		}
		if (!prefix.empty)
			'''[«routePrefix»("«prefix»")]
			'''
	}

	def private getRoutingAttrs(Method method) {
		val List<String> attrs = newArrayList
		val methodAttr = switch (method.httpMethod) {
			case GET: "HttpGet"
			case HEAD: "HttpHead"
			case POST: "HttpPost"
			case PUT: "HttpPut"
			case OPTIONS: "HttpOptions"
			case DELETE: "HttpDelete"
			case CONNECT: "HttpConnect"
			case PATCH: "HttpPatch"
			case TRACE: "HttpTrace"
		};
		if (methodAttr !== null) {
			attrs.add(methodAttr);
		}
		val uri = method.containingResourceDefinition.URI.segments.join("/");
		attrs.add('''Route("«uri»")''')
		attrs.join(", ")
		attrs
	}
}
