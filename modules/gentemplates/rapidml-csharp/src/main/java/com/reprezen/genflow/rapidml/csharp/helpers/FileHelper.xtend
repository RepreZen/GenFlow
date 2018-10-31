package com.reprezen.genflow.rapidml.csharp.helpers

import com.reprezen.genflow.api.template.IGenTemplateContext
import com.reprezen.genflow.rapidml.csharp.Config
import com.reprezen.genflow.rapidml.csharp.helpers.UsingItem
import com.reprezen.rapidml.DataModel
import com.reprezen.rapidml.Enumeration
import com.reprezen.rapidml.ResourceAPI
import com.reprezen.rapidml.ResourceDefinition
import com.reprezen.rapidml.Structure
import java.io.File
import java.nio.file.Files
import java.util.List
import java.util.Set

class FileHelper {
	val IGenTemplateContext context
	val Config config
	var List<Object> namespaceComponents = newArrayList
	var List<Object> folderComponents = newArrayList
	var Set<Object> usings = newHashSet

	new(IGenTemplateContext context, Config config) {
		this.context = context
		this.config = config
	}

	def static of(Enumeration enumeration, FileRole role, IGenTemplateContext context, Config config) {
		val helper = new FileHelper(context, config)
		val dataModel = enumeration.eContainer as DataModel
		switch (role) {
			case ENUMS:
				helper //
				.inNamespace(NamespaceComponent.ROOT, dataModel) //
				.inFolder(FolderComponent.ROOT, FolderComponent.MODELS, dataModel) //
				.using(UsingItem.SYSTEM, UsingItem.JSON_CONVERTERS, UsingItem.GEN_ATTR, UsingItem.ENUM_SERIALIZATION)
			default: {
			}
		}
		helper
	}

	def static of(Structure structure, FileRole role, IGenTemplateContext context, Config config) {
		val helper = new FileHelper(context, config)
		val dataModel = structure.eContainer as DataModel
		switch (role) {
			case INTERFACE:
				helper //
				.inNamespace(NamespaceComponent.ROOT, dataModel) //
				.inFolder(FolderComponent.ROOT, FolderComponent.MODELS, dataModel) //
				.using(UsingItem.SYSTEM, UsingItem.JSON_CONVERTERS, UsingItem.GEN_ATTR, UsingItem.COLLECTIONS,
					UsingItem.JSON, UsingItem.JSON_SERIALIZATION)
			case POCOS:
				helper //
				.inNamespace(NamespaceComponent.ROOT, dataModel) //
				.inFolder(FolderComponent.ROOT, FolderComponent.MODELS, dataModel, FolderComponent.POCOS) //
				.using(UsingItem.SYSTEM, UsingItem.GEN_ATTR, UsingItem.COLLECTIONS, UsingItem.REPREZEN)
			default: {
			}
		}
		helper
	}

	def static of(ResourceDefinition resource, FileRole role, IGenTemplateContext context, Config config) {
		val helper = new FileHelper(context, config)
		val api = resource.eContainer as ResourceAPI
		switch (role) {
			case INTERFACE: {
				helper //
				.inNamespace(NamespaceComponent.ROOT, api) //
				.inFolder(FolderComponent.ROOT, FolderComponent.CONTROLLERS, api) //
				.using(UsingItem.GEN_ATTR, UsingItem.COLLECTIONS)
				switch (config.framework) {
					case ASP_DOTNET_CORE_2_0_MVC:
						helper.using(UsingItem.REPREZEN, UsingItem.CORE_MVC)
					case ASP_DOTNET_WEBAPI_2: {
					}
				}
			}
			case DELEGATE_CONTROLLER: {
				helper //
				.inNamespace(NamespaceComponent.ROOT, api, NamespaceComponent.DELEGATE) //
				.inFolder(FolderComponent.ROOT, FolderComponent.CONTROLLERS, api, FolderComponent.DELEGATE) //
				.using(UsingItem.SYSTEM, UsingItem.GEN_ATTR, UsingItem.COLLECTIONS)
				switch (config.framework) {
					case ASP_DOTNET_CORE_2_0_MVC:
						helper.using(UsingItem.CORE_MVC, UsingItem.REPREZEN)
					case ASP_DOTNET_WEBAPI_2:
						helper.using(UsingItem.ROUTING)
				}
			}
			case ABSTRACT_DELEGATE_HANDLER: {
				helper //
				.inNamespace(NamespaceComponent.ROOT, api, NamespaceComponent.DELEGATE) //
				.inFolder(FolderComponent.ROOT, FolderComponent.CONTROLLERS, api, FolderComponent.DELEGATE) //
				.using(UsingItem.SYSTEM, UsingItem.COLLECTIONS)
				switch (config.framework) {
					case ASP_DOTNET_CORE_2_0_MVC:
						helper.using(UsingItem.CORE_MVC, UsingItem.REPREZEN)
					case ASP_DOTNET_WEBAPI_2: {
					}
				}
			}
			default: {
			}
		}
		helper
	}

	def static of(SpecialFile specialFile, IGenTemplateContext context, Config config) {
		val helper = new FileHelper(context, config)
		switch (specialFile) {
			case REPREZEN: {
				helper //
				.inNamespace(NamespaceComponent.REPREZEN) //
				.inFolder(FolderComponent.ROOT, FolderComponent.REPREZEN) //
				.using(UsingItem.SYSTEM, UsingItem.JSON_SERIALIZATION, UsingItem.COLLECTIONS, UsingItem.LINQ)
				switch (config.framework) {
					case ASP_DOTNET_CORE_2_0_MVC:
						helper.using(UsingItem.CORE_MVC, UsingItem.DI, UsingItem.TASKS)
					case ASP_DOTNET_WEBAPI_2:
						helper.using(UsingItem.ROUTING)
				}
			}
		}
		helper
	}

	def inNamespace(Object... components) {
		this.namespaceComponents.addAll(components)
		this
	}

	def inFolder(Object... components) {
		this.folderComponents.addAll(components)
		this
	}

	def using(Object... usings) {
		this.usings.addAll(usings)
		this
	}

	def String getCsharpFileName(CharSequence base) {
		'''«base».cs'''
	}

	def writeFile(CharSequence content, String fileName) {
		Files.write(new File(folder, fileName).toPath, content.wrap.getBytes())
	}

	def private String wrap(CharSequence content) {
		'''
			«usingSection»
			            
			namespace «namespace» {
			    
			    «content»
			}
		'''
	}

	def private getFolder() {
		val path = folderComponents.map[resolveForFolder].join("/")
		val dir = new File(path)
		dir.mkdirs
		dir
	}

	def private getNamespace() {
		namespaceComponents.namespace
	}

	def private getNamespace(Object... components) {
		components.map[resolveForNamespace].join(".")
	}

	def private getUsingSection() {
		val targets = usings.map[resolveForUsing].sort
		'''
			«FOR target : targets»
				using «target»;
			«ENDFOR»
		'''
	}

	def private resolveForFolder(Object component) {
		switch (component) {
			FolderComponent: {
				// apparently xtend's automatic cast of a switch variable in a type guard does not extend
				// to that variable appearing in another switch statement!
				val fc = component
				switch (fc) {
					case ROOT: return context.outputDirectory.absolutePath
					case MODELS: return config.modelsFolder
					case CONTROLLERS: return config.controllersFolder
					case POCOS: return "POCOs"
					default: return fc.formatted
				}
			}
			DataModel:
				return component.name
			ResourceAPI:
				return component.name
			String:
				return component
		}
		throw new IllegalArgumentException("Unsupported folder component: " + String.valueOf(component));
	}

	def private getFormatted(FolderComponent component) {
		switch (component) {
			case REPREZEN: "RepreZen"
			default: component.name.toLowerCase.toFirstUpper
		}
	}

	def private resolveForNamespace(Object component) {
		switch (component) {
			NamespaceComponent: {
				val nsc = component
				switch (nsc) {
					case ROOT: return config.rootNamespace
					default: return component.formatted
				}
			}
			DataModel:
				return component.name
			ResourceAPI:
				return component.name
			String:
				return component
		}
		throw new IllegalArgumentException("Unsupported namespace component: " + String.valueOf(component))
	}

	def private getFormatted(NamespaceComponent component) {
		switch (component) {
			case REPREZEN: "RepreZen"
			default: component.name.toLowerCase.toFirstUpper
		}
	}

	def private resolveForUsing(Object usingItem) {
		switch (usingItem) {
			UsingItem: {
				val item = usingItem
				switch (item) {
					case ROOT:
						return config.rootNamespace
					default:
						return item.namespace
				}
			}
			DataModel:
				return getNamespace(NamespaceComponent.ROOT, usingItem)
			ResourceAPI:
				return getNamespace(NamespaceComponent.ROOT, usingItem)
			String:
				return usingItem
			default:
				throw new IllegalArgumentException("Unsupported 'using' item: " + String.valueOf(usingItem))
		}
	}
}

enum FileRole {
	ENUMS,
	INTERFACE,
	POCOS,
	DELEGATE_CONTROLLER,
	ABSTRACT_DELEGATE_HANDLER
}

enum FolderComponent {
	ROOT,
	MODELS,
	CONTROLLERS,
	POCOS,
	DELEGATE,
	REPREZEN
}

enum NamespaceComponent {
	ROOT,
	INTERNAL,
	DELEGATE,
	REPREZEN
	;
}

enum SpecialFile {
	REPREZEN
}
