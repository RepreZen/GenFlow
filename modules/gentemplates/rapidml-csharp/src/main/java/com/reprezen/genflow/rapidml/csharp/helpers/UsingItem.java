package com.reprezen.genflow.rapidml.csharp.helpers;

// implemented in Java because Xtend only does simple enums

public enum UsingItem {
	ROOT, //
	SYSTEM("System"), //
	GEN_ATTR("System.CodeDom.Compiler"), //
	ENUM_SERIALIZATION("System.Runtime.Serialization"), //
	JSON("Newtonsoft.Json"), //
	JSON_SERIALIZATION("Newtonsoft.Json.Serialization"), //
	COLLECTIONS("System.Collections.Generic"), //
	ROUTING("System.Web.Http"), REPREZEN("RepreZen"), JSON_CONVERTERS("Newtonsoft.Json.Converters"),
	CORE_MVC("Microsoft.AspNetCore.Mvc"), TASKS("System.Threading.Tasks"),
	DI("Microsoft.Extensions.DependencyInjection"), LINQ("System.Linq");

	private String namespace;

	UsingItem() {
		this(null);
	}

	UsingItem(String namespace) {
		this.namespace = namespace;
	}

	public String getNamespace() {
		return namespace;
	}
}
