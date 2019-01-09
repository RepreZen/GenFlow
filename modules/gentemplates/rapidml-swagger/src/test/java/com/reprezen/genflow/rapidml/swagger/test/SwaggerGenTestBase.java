package com.reprezen.genflow.rapidml.swagger.test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.junit.Assert;

import com.google.common.io.Files;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader;

public class SwaggerGenTestBase extends Assert {

	protected ZenModel loadModelAndNormalize(CharSequence modelText) throws IOException {
		File modelFile = File.createTempFile("rznswagtest", ".rapid");
		modelFile.deleteOnExit();
		Files.write(modelText.toString().getBytes(Charset.forName("UTF-8")), modelFile);
		ZenModel model = new ZenModelLoader().loadAndValidateModel(modelFile);
		new ZenModelNormalizer().normalize(model);
		return model;
	}
}
