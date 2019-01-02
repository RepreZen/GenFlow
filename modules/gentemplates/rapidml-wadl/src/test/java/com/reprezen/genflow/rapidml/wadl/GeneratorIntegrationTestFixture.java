/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.wadl;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.eclipse.emf.common.util.Monitor;
import org.junit.runner.Description;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.base.CaseFormat;
import com.google.common.io.Closeables;
import com.google.common.io.Resources;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.FakeGenTemplateContext;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;
import com.reprezen.genflow.api.zenmodel.ZenModelLocator;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.api.zenmodel.util.CommonServices;
import com.reprezen.genflow.rapidml.wadl.xtend.XGenerateWadl;
import com.reprezen.genflow.rapidml.xsd.DataModelSchemaGenerator;
import com.reprezen.genflow.rapidml.xsd.ResourceApiSchemaGenerator;
import com.reprezen.genflow.test.common.GeneratorTestFixture;
import com.reprezen.rapidml.DataModel;
import com.reprezen.rapidml.ResourceAPI;
import com.reprezen.rapidml.SingleValueType;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.implicit.ZenModelNormalizer;

public abstract class GeneratorIntegrationTestFixture extends GeneratorTestFixture {

	private static SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

	public String xmlSamplesFolder = "test-resources/xml/";

	public GeneratorIntegrationTestFixture(String extension) {
		super(extension);
	}

	private FakeGenTemplateContext context = null;

	@Override
	protected Map<String, String> doGenerate(ZenModel zenModel, File dir, Monitor progressMonitor)
			throws IOException, GenerationException {
		new ZenModelNormalizer().normalize(zenModel);
		context = new FakeGenTemplateContext(new ZenModelSource(new File("dummy.rapid")) {
			@Override
			public ZenModel load(File inFile) throws GenerationException {
				return zenModel;
			}
		});
		Map<String, String> generated = new HashMap<>();
		context.setTraces(new GenTemplateTraces());
		context.setTraceBuilder(new GenTemplateTraceBuilder(null));

		ZenModelLocator locator = new ZenModelLocator(zenModel);

		for (ResourceAPI resourceAPI : zenModel.getResourceAPIs()) {
			generateXsdResourceAPI(zenModel, resourceAPI, locator, generated);
		}

		Set<DataModel> dataModels = new HashSet<>();
		for (SingleValueType singleValueType : CommonServices.getUsedSimpleTypes(zenModel)) {
			dataModels.add(CommonServices.getContainerOfType(singleValueType, DataModel.class));
		}
		for (DataModel dataModel : dataModels) {
			generateXsdDataModel(zenModel, dataModel, locator, generated);
		}

		context.getTraces().addTrace(new File("xsdGenerator"), context.getTraceBuilder().build());
		context.setTraceBuilder(new GenTemplateTraceBuilder(null));

		for (ResourceAPI resourceAPI : zenModel.getResourceAPIs()) {
			XGenerateWadl genWadl = new XGenerateWadl();
			context.addTraceItem("file").withOutputFile(new File(resourceAPI.getName() + ".wadl"))
					.withPrimarySourceItem(new ZenModelLocator(zenModel).locate(resourceAPI));
			genWadl.init(context);
			generated.put(resourceAPI.getName() + ".wadl", genWadl.generate(zenModel, resourceAPI));
		}
		return generated;
	}

	private void generateXsdResourceAPI(ZenModel zenModel, ResourceAPI resourceAPI, ZenModelLocator locator,
			Map<String, String> generated) throws GenerationException {
		ResourceApiSchemaGenerator genXsd = new ResourceApiSchemaGenerator();
		genXsd.init(context);
		String xsdFileName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, resourceAPI.getName()) + ".xsd";
		generated.put(xsdFileName, genXsd.generate(zenModel, resourceAPI));
		context.addTraceItem("file").withOutputFile(new File(xsdFileName))
				.withPrimarySourceItem(locator.locate(resourceAPI));
	}

	private void generateXsdDataModel(ZenModel zenModel, DataModel dataModel, ZenModelLocator locator,
			Map<String, String> generated) throws GenerationException {
		DataModelSchemaGenerator genXsd = new DataModelSchemaGenerator();
		genXsd.init(context);
		File xsdFile = genXsd.getOutputFile(zenModel, dataModel);
		generated.put(xsdFile.toString(), genXsd.generate(zenModel, dataModel));
		context.addTraceItem("file").withOutputFile(xsdFile).withPrimarySourceItem(locator.locate(dataModel));
	}

	@Override
	protected String getSampleRestName(Description description) {
		Class<?> class_;
		try {
			// A workaround for java.lang.ClassNotFoundException thrown by
			// description.getMethodClass
			class_ = Class.forName(description.getClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		SampleRestFile result = class_.getAnnotation(SampleRestFile.class);
		return result.value();
	}

	protected static Schema loadAndValidateXsdSchema(File schemaFile) {
		try {
			return schemaFactory.newSchema(schemaFile);
		} catch (SAXException e) {
			fail(e.getMessage());
		}
		return null;
	}

	protected static Schema loadAndValidateXsdSchema(InputStream schemaFile) {
		try {
			return schemaFactory.newSchema(new StreamSource(schemaFile));
		} catch (SAXException e) {
			fail(e.getMessage());
		} finally {
			Closeables.closeQuietly(schemaFile);
		}
		return null;
	}

	protected void validateXmlAgainstXsd(Schema schema, File xmlFle) {
		Source xmlFile = new StreamSource(xmlFle);
		Validator validator = schema.newValidator();

		try {
			validator.validate(xmlFile);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}

	public File getGeneratedFile() {
		return generatedFile;
	}

	protected File getSampleXmlFile(String sampleXmlFileName) throws URISyntaxException {
		return Paths.get(Resources.getResource(xmlSamplesFolder + sampleXmlFileName).toURI()).toFile();
	}

	public Object query(String query, QName returnType) throws Exception {
		return query(generatedFile, query, returnType);
	}

	protected Object query(File file, String query, QName returnType) throws Exception {
		// Standard of reading a XML file
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(file);
		// Create a XPathFactory
		XPathFactory xFactory = XPathFactory.newInstance();

		// Create a XPath object
		XPath xpath = xFactory.newXPath();

		// Compile the XPath expression
		XPathExpression expr = xpath.compile(query);
		// Run the query
		Object result = expr.evaluate(doc, returnType);
		return result;
	}

}
