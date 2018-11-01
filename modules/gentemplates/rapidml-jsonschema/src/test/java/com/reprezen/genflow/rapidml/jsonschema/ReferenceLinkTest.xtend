package com.reprezen.genflow.rapidml.jsonschema

import com.reprezen.genflow.common.jsonschema.builder.xchange.ContractJsonSchemaNodeFactory
import com.reprezen.rapidml.ZenModel
import javax.inject.Inject
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Map
import com.reprezen.genflow.common.jsonschema.Options
import com.reprezen.genflow.api.template.FakeGenTemplateContext

@RunWith(XtextRunner)
@InjectWith(RapidMLInjectorProvider)
class ReferenceLinkTest {
	
	@Inject
	ParseHelper<ZenModel> parseHelper;

	def model() '''
	rapidModel Realizations
		resourceAPI RealizationsAPI baseURI "http://my-namespace.com"
	
			objectResource DataType1Object type DataType1
				URI /uri/{id}
				referenceLink >ref1
					targetResource ReferencedResource
					targetProperties
						prop1
			
			objectResource ReferencedResource type DataType2
				URI /uri2
	
		dataModel RealizationsDataModel
			structure DataType1
				id : string
				prop2: string
				ref1: reference to DataType2
				
			structure DataType2
				prop1: string
				prop2: string
	'''

	@Test
	def void test() {
		val jsonSchemaGenerator = new XGenerateJsonSchema(new ContractJsonSchemaNodeFactory())
		val Map<String, Object> defaultParameters = 
				#{Options::ALLOW_EMPTY_OBJECT -> false, Options::ALLOW_EMPTY_ARRAY -> false,
					Options::ALLOW_EMPTY_STRING -> false}

		jsonSchemaGenerator.init(
			new FakeGenTemplateContext(defaultParameters))

		val spec = parseHelper.parse(model()) as ZenModel

		val result = jsonSchemaGenerator.generate(spec)
		println(result)
	}
}