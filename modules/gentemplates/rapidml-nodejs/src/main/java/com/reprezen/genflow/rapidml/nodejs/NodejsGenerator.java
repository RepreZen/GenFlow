package com.reprezen.genflow.rapidml.nodejs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.zenmodel.ZenModelDynamicGenerator;
import com.reprezen.rapidml.ResourceAPI;
import com.reprezen.rapidml.ResourceDefinition;
import com.reprezen.rapidml.ServiceDataResource;
import com.reprezen.rapidml.ZenModel;

public class NodejsGenerator extends ZenModelDynamicGenerator {
	private static ObjectNode emptyObject = JsonNodeFactory.instance.objectNode();

	@Override
	public void generate(ZenModel model) throws GenerationException {
		String metadataFile = String.format("%s_metadata.yaml", model.getName());
		generateFile(metadataFile, new MetadataGenerator(model));
		if (context.getGenTargetParameters().get("genMetadataSkeleton") == Boolean.TRUE) {
			String skeletonFile = String.format("%s_metadata-skel.yaml", model.getName());
			Object structSkel = context.getGenTargetParameters().get("structMetadataSkeleton");
			Object primSkel = context.getGenTargetParameters().get("primFieldMetadataSkeleton");
			Object refSkel = context.getGenTargetParameters().get("refFieldMetadataSkeleton");
			if (!(context.getGenTargetParameters().get("omitFieldsWithoutSkeletons") == Boolean.TRUE)) {
				primSkel = primSkel == null ? emptyObject : primSkel;
				refSkel = primSkel == null ? emptyObject : refSkel;
			}
			generateFile(skeletonFile, new MetadataSkeletonGenerator(model, structSkel, primSkel, refSkel));
		}
		for (ResourceAPI api : model.getResourceAPIs()) {
			for (ResourceDefinition resource : api.getOwnedResourceDefinitions()) {
				if (resource instanceof ServiceDataResource) {
					String resourcePath = String.format("handlers/%s/%s/%s.js", model.getName(), api.getName(),
							resource.getName());
					generateFile(resourcePath, new ResourceStubGenerator((ServiceDataResource) resource));
					String customPath = String.format("custom/%s/%s/%sImpl.js", model.getName(), api.getName(),
							resource.getName());
					generateFile(customPath, new CustomCodeGenerator((ServiceDataResource) resource), false);
				}
			}
		}

		generateFile("app.js", new AppGenerator(metadataFile, context));
	}

	private void generateFile(String path, Generator generator) throws GenerationException {
		generateFile(path, generator, true);
	}

	private void generateFile(String path, Generator generator, boolean overwriteExisting) throws GenerationException {
		File file = context.resolveOutputPath(new File(path));
		if (file.exists() && !overwriteExisting) {
			return;
		}
		try {
			Files.createDirectories(file.getParentFile().toPath());
			// Safe to use, exists in v2.2:
			// https://commons.apache.org/proper/commons-io/javadocs/api-2.2/org/apache/commons/io/FileUtils.html#forceMkdir(java.io.File)
			// To eliminate unneeded dependencies, consider using
			// `Files.createDirectories(file.getParentFile().toPath())` instead
			FileUtils.forceMkdir(file.getParentFile());
			Files.write(file.toPath(), generator.generate().getBytes());
		} catch (IOException e) {
			throw new GenerationException(String.format("Failed to generate %s", file), e);
		}
	}

	public interface Generator {
		String generate() throws GenerationException;
	}
}
