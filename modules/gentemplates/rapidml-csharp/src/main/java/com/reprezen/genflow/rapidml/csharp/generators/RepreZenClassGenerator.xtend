package com.reprezen.genflow.rapidml.csharp.generators

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.Config.Framework
import com.reprezen.genflow.rapidml.csharp.helpers.FileHelper
import com.reprezen.genflow.rapidml.csharp.helpers.SpecialFile

class RepreZenClassGenerator {

	val Config config
	val extension FileHelper fileHelper

	new(IGenTemplateContext context, Config config) {
		this.config = config
		this.fileHelper = FileHelper.of(SpecialFile.REPREZEN, context, config)
	}

	def generate() {
		content.writeFile("RepreZen".csharpFileName)
	}

	def getContent() {
		'''
			
			«configClass»
			
			[AttributeUsage(AttributeTargets.Class, Inherited = false)]
			public class DeserializeFromAttribute : Attribute
			{
			    private Type target;
			
			    public DeserializeFromAttribute(Type target)
			    {
			        this.target = target;
			    }
			
			    public Type Target { get { return target; } }
			}
			
			public class RepreZenContractResolver : IContractResolver
			{
			    private static Dictionary<Type, Type> targetTypeMap = new Dictionary<Type, Type>();
			    private static Type deserializeFrom = typeof(DeserializeFromAttribute);
			
			    public static void ScanForTargets()
			    {
			        targetTypeMap.Clear();
			        var types = AppDomain.CurrentDomain.GetAssemblies()
			            .SelectMany(a => a.GetTypes())
			            .Where(t => t.GetCustomAttributes(deserializeFrom, false).Length > 0);
			        foreach (Type type in types)
			        {
			            DeserializeFromAttribute attr = type.GetCustomAttributes(deserializeFrom, false)
			                .First() as DeserializeFromAttribute;
			            targetTypeMap[attr.Target] = type;
			        }
			    }
			
			    private IContractResolver chainedResolver { get; set; }
			
			    public RepreZenContractResolver(IContractResolver chainedResolver)
			    {
			        this.chainedResolver = chainedResolver;
			    }
			
			    public JsonContract ResolveContract(Type type)
			    {
			        if (targetTypeMap.ContainsKey(type))
			        {
			            type = targetTypeMap[type];
			        }
			        return chainedResolver.ResolveContract(type);
			    }
			}
			
			«IF config.framework == Framework.ASP_DOTNET_CORE_2_0_MVC»
				«responderClasses»
				
					«resultClasses»
			«ENDIF»
			
		'''
	}

	def getConfigClass() {
		switch (config.framework) {
			case ASP_DOTNET_CORE_2_0_MVC: {
				'''
					public static class RepreZenConfiguration
					{
					    public static IMvcBuilder ConfigureRepreZen(this IMvcBuilder builder)
					    {
					        RepreZenContractResolver.ScanForTargets();
					        builder.AddJsonOptions(opt =>
					         {
					             var resolver = opt.SerializerSettings.ContractResolver;
					             if (resolver != null)
					             {
					                 var repreZenResolver = new RepreZenContractResolver(resolver);
					                 opt.SerializerSettings.ContractResolver = repreZenResolver;
					             }
					         });
					        return builder;
					    }
					}
				'''
			}
			case ASP_DOTNET_WEBAPI_2: {
				'''
					public class RepreZenConfiguration 
					{
						public static void configure(HttpConfiguration config)
						{
						    config.Formatters.Remove(config.Formatters.XmlFormatter);
						    var currentResolver = config.Formatters.JsonFormatter.SerializerSettings.ContractResolver;
						    RepreZenContractResolver.ScanForTargets();
						    var repreZenResolver = new RepreZenContractResolver(currentResolver);
						    config.Formatters.JsonFormatter.SerializerSettings = new Newtonsoft.Json.JsonSerializerSettings {
						        ContractResolver = repreZenResolver
						    };
						    config.Formatters.JsonFormatter.UseDataContractJsonSerializer = false;
						}
					}
				'''
			}
		}
	}

	def getResponderClasses() {
		'''
			public interface IResponder
			{
			    bool IsNormal();
			
			    void RespondWith(IActionResult result);
			}
			
			public class Responder<NormalType> : IResponder
			{
			    private Result<NormalType> result;
			
			    public void RespondWith(NormalType value)
			    {
			        this.result = new Result<NormalType>(value);
			    }
			
			    public void RespondWith(IActionResult result)
			    {
			        this.result = new Result<NormalType>(result);
			    }
			
			    public bool IsNormal()
			    {
			        return result != null && result.IsNormal();
			    }
			
			    public Result<NormalType> GetResult()
			    {
			        return result;
			    }
			}
			
			public class VoidResponder : IResponder
			{
			    private VoidResult result = new VoidResult();
			
			    public void RespondWith(IActionResult result)
			    {
			        this.result = new VoidResult(result);
			    }
			
			    public bool IsNormal()
			    {
			        return result.IsNormal();
			    }
			
			    public VoidResult GetResult()
			    {
			        return result;
			    }
			}
		'''
	}

	def getResultClasses() {
		'''
			public class Result<NormalType> : IActionResult
			{
			    private IActionResult wrapped;
			
			    public Result(NormalType value) {
			        this.wrapped = new OkObjectResult(value);
			    }
			
			    public Result(IActionResult wrapped)
			    {
			        this.wrapped = wrapped;
			    }
			
			    public bool IsNormal()
			    {
			        return wrapped is OkObjectResult && ((OkObjectResult)wrapped).Value is NormalType;
			    }
			
			    public NormalType GetValue()
			    {
			        return (NormalType) ((OkObjectResult)wrapped).Value;
			    }
			
			    public Task ExecuteResultAsync(ActionContext context)
			    {
			        return wrapped.ExecuteResultAsync(context);
			    }
			}
			
			public class VoidResult : IActionResult
			{
			    private IActionResult wrapped;
			
			    public VoidResult()
			    {
			        this.wrapped = new OkResult();
			    }
			    
			    public VoidResult(IActionResult wrapped)
			    {
			        this.wrapped = wrapped;
			    }
			
			    public bool IsNormal()
			    {
			        return wrapped is OkResult;
			    }
			
			    public Task ExecuteResultAsync(ActionContext context)
			    {
			        return wrapped.ExecuteResultAsync(context);
			    }
			}
		'''
	}

}
