# GenFlow
[![Build Status](https://travis-ci.org/RepreZen/GenFlow.svg?branch=master)](https://travis-ci.org/RepreZen/GenFlow)
![](https://img.shields.io/maven-central/v/com.reprezen.genflow/genflow-common.svg)

Code Generation framework used in [RepreZen API
Studio](https://www.reprezen.com/swagger-openapi-code-generation-api-first-microservices-enterprise-development).

# Coming Soon...

&nbsp;<img src="https://www.reprezen.com/hubfs/RepreZen_WIP_Ninja.png" width="200" />

RepreZen API Studio supports API design-first and <a href="http://RZen.io/APICodeFlow" rel="noreferrer noopener" target="_blank">API
    CodeFlow</a> with a highly-evolved, extensible code generation engine. These capabilities are being repackaged as
<em>GenFlow</em>, soon to be announced.</p>

# An Open Framework for API Code Generation

GenFlow is a comprehensive API code generation framework that supports a wide variety of code generation scenarios.
Here are some of its essential features:

* **Generates API client libraries, services, documentation** and other essential components, using pre-built or custom generators.<br><br>
  &nbsp;&nbsp;&nbsp;**`Learn More:`**
  
    * [What code generators are included in RepreZen API Studio?&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](https://support.reprezen.com/support/solutions/articles/24000018770-what-code-generators-are-included-in-reprezen-api-studio-)

* **Works with OpenAPI v2 (Swagger), OpenAPI v3, and RAPID-ML**, with an open architecture to support other API specification formats as input.<br><br>
  &nbsp;&nbsp;&nbsp;**`Learn More:`**
  
    * [OpenAPI 2.0 Specification&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md)
    * [OpenAPI 3.0 Specification&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](http://rzen.io/OAS3Spec)
    * [RAPID-ML Homepage&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](http://rapid-api.org)

* **Provides convenient access to Swagger Codegen and OpenAPI-Generator** along with other popular open source code generators.<br><br>
  &nbsp;&nbsp;&nbsp;**`Learn More:`**
  
    * [Swagger Codegen&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](https://github.com/swagger-api/swagger-codegen)
    * [OpenAPI Generator&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](https://github.com/OpenAPITools/openapi-generator)
      
* **Supports custom code generators** written in Java, and provides a powerful, template-driven framework built on Xtend.<br><br>
  &nbsp;&nbsp;&nbsp;**`Learn More:`**
  
     * [Xtend Homepage&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](https://www.eclipse.org/xtend/)
     * [Custom Code Generation in RepreZen API Studio&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](http://RZen.io/CGDocs)

* **Integrates KaiZen OpenAPI Normalizer** as a preprocessor or standalone code generator. KaiZen Normalizer consolidates multi-file OpenAPI projects to a single document by resolving references, applying defaults, and expanding cascading properties to ensure reliable processing by downstream code generators and API documentation formats.<br><br>
&nbsp;&nbsp;&nbsp;**`Learn More:`**

    * [KaiZen OpenAPI Normalizer&nbsp;<img src="https://www.reprezen.com/hubfs/external-link.png" height="12" width="12" alt="external link">](http://rzen.io/normalizer)

* **Runs from the command line, CI/CD platforms, and RepreZen API Studio** through Maven and Gradle integration, with all dependencies hosted on Maven Central.

* **Easy configuration** through a convenient YAML-formatted .gen file, with embedded documentation for each parameter.

* **Enables multi-step chaining** of code generators to create advanced API modeling and codegen solutions.
