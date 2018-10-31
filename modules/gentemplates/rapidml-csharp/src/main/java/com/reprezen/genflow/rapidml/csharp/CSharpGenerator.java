package com.reprezen.genflow.rapidml.csharp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.IGenTemplateContext;
import com.reprezen.genflow.api.zenmodel.ZenModelDynamicGenerator;
import com.reprezen.genflow.rapidml.csharp.generators.DataModelGenerator;
import com.reprezen.genflow.rapidml.csharp.generators.RepreZenClassGenerator;
import com.reprezen.genflow.rapidml.csharp.generators.ResourceAPIGenerator;
import com.reprezen.genflow.rapidml.csharp.generators.StructurePocoGenerator;
import com.reprezen.rapidml.ZenModel;

public class CSharpGenerator extends ZenModelDynamicGenerator {

	private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

	private Config config;

	@Override
	public void init(IGenTemplateContext context) throws GenerationException {
		super.init(context);
		try {
			this.config = mapper.convertValue(context.getGenTargetParameters(), Config.class);
		} catch (Exception e) {
			throw new GenerationException("Failed to load GenTarget parameters", e);
		}
	}

	@Override
	public void generate(ZenModel model) {
		config.validate(model);
		// note that DMG must go first, since it's where we discover the names of all
		// the enumerations defined by the
		// model. They must be made nullable throughout the generated code
		new DataModelGenerator(model, context, config).generate();
		if (config.isGenerateModelPocos()) {
			new StructurePocoGenerator(model, context, config).generate();
		}
		new ResourceAPIGenerator(model, context, config).generate();
		new RepreZenClassGenerator(context, config).generate();
	}
}
