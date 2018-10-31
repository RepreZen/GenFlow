/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.jaxb;

import static com.google.common.collect.Collections2.filter;
import static com.reprezen.genflow.api.trace.GenTemplateTraceUtils.getTraceItemsOfType;
import static com.reprezen.genflow.api.trace.GenTemplateTraceUtils.hasFile;
import static com.reprezen.genflow.api.trace.GenTemplateTraceUtils.hasSourceRole;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraceItem;
import com.reprezen.genflow.api.trace.GenTemplateTraceSourceItem;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.api.zenmodel.ZenModelSource;
import com.reprezen.genflow.rapidml.xsd.XMLSchemaGenTemplate;
import com.reprezen.rapidml.ZenModel;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.writer.FileCodeWriter;
import com.sun.tools.xjc.api.ErrorListener;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.SchemaCompiler;
import com.sun.tools.xjc.api.XJC;
import com.sun.xml.bind.api.impl.NameConverter;

public class JaxbGenTemplate extends ZenModelGenTemplate {

    public static final String JAVA_XML_BINDINGS_GENERATOR_NAME = "Java XML Bindings";
    public static final String JAVA_XML_BINDINGS_GENERATOR_ID = "com.modelsolv.reprezen.gentemplates.jaxb";
    public static final String XSD_DEPENDENCY = "xsdGenerator";
    private static final String TRACE_TYPE_JAXB_CLASS = "jaxbClass";
    private static final String TRACE_PROP_JAXB_CLASS_NAME = "jaxbClassName";
    private static final String TRACE_PROP_JAXB_PACKAGE_NAME = "jaxbPackageName";

    @Override
    public String getName() {
        return JAVA_XML_BINDINGS_GENERATOR_NAME;
    }

    @Override
    public String getId() {
        return JAVA_XML_BINDINGS_GENERATOR_ID;
    }

    @Override
    public void configure() throws GenerationException {
        alsoKnownAs("com.modelsolv.reprezen.gentemplates.jaxb");
        define(prerequisite().named(XSD_DEPENDENCY).on(XMLSchemaGenTemplate.class));
        define(primarySource().ofType(ZenModelSource.class));
        define(GenTemplateProperty.reprezenProvider());
    }

    @Override
    protected StaticGenerator<ZenModel> getStaticGenerator() {
        return new Generator(this, context);
    }

    public static class Generator extends GenTemplate.StaticGenerator<ZenModel> {
        private GenTemplateTraceBuilder traceBuilder;

        public Generator(GenTemplate<ZenModel> genTemplate, GenTemplateContext context) {
            super(genTemplate, context);
        }

        @Override
        public void generate(ZenModel model) throws GenerationException {
            traceBuilder = context.getTraceBuilder();
            ensureOutputDirectory(context.getOutputDirectory());
            generateXsdDataBindings(context.getControllingGenTarget(), model);
        }

        private void ensureOutputDirectory(File outputDirectory) {
            if (!outputDirectory.exists()) {
                outputDirectory.mkdirs();
            }
        }

        protected void generateXsdDataBindings(GenTarget target, ZenModel model) throws GenerationException {
            GenTemplateTrace xsdTraces = context.getPrerequisiteTrace(XSD_DEPENDENCY);
            List<GenTemplateTraceItem> fileTraceItems = getTraceItemsOfType(xsdTraces, "file");
            Set<File> xsdFiles = Sets
                    .newHashSet(Lists.transform(fileTraceItems, new Function<GenTemplateTraceItem, File>() {

                        @Override
                        public File apply(GenTemplateTraceItem traceItem) {
                            return traceItem.getOutputFile();
                        }
                    }));

            for (File xsdFile : xsdFiles) {
                generateXsdDataBinding(xsdFile, target.getOutputDir(), xsdTraces);
            }
        }

        protected JCodeModel generateXsdDataBinding(File schemaFile, File outputDir, GenTemplateTrace xsdTraces)
                throws GenerationException {
            SchemaCompiler schemaCompliler = XJC.createSchemaCompiler();
            // Attempt to fix ZEN-1657 "Unhandled event loop exception when trying to generate Java XML Bindings"
            // schemaCompliler.getOptions().disableXmlSecurity = true;
            // schemaCompliler.getOptions().entityResolver = new org.xml.sax.EntityResolver() {
            //
            // @Override
            // public InputSource resolveEntity(String publicId, String systemId)
            // throws SAXException, IOException {
            // System.out.println("Ignoring " + publicId + ", " + systemId);
            // return new InputSource(new StringReader(""));
            // }
            // };
            InputSource inputSource;
            try {
                inputSource = new InputSource(new FileReader(schemaFile));
            } catch (FileNotFoundException e) {
                throw new GenerationException("Cannot read a schema file", e);
            }
            inputSource.setSystemId(schemaFile.toURI().toString());
            schemaCompliler.setErrorListener(new ErrorListener() {

                @Override
                public void error(SAXParseException e) {
                    throw new RuntimeException("The XML schema has syntax errors", e);
                }

                @Override
                public void fatalError(SAXParseException e) {
                    throw new RuntimeException("The XML schema has syntax errors", e);
                }

                @Override
                public void info(SAXParseException arg0) {
                }

                @Override
                public void warning(SAXParseException arg0) {
                }
            });
            schemaCompliler.parseSchema(inputSource);
            S2JJAXBModel jaxbModel = schemaCompliler.bind();
            JCodeModel codeModel = jaxbModel.generateCode(null, null);
            try {
                codeModel.build(new FileCodeWriter(outputDir));
                createTraces(xsdTraces, schemaFile);
                return codeModel;
            } catch (IOException e) {
                throw new GenerationException("Error during JAXB file generation", e);
            }
        }

        protected void createTraces(GenTemplateTrace xsdTraces, File xsdFile) {
            Collection<GenTemplateTraceItem> complexTypeItems = Collections2
                    .filter(getTraceItemsOfType(xsdTraces, "complexType"), hasFile(xsdFile));
            for (GenTemplateTraceItem item : complexTypeItems) {
                String complexTypeName = item.getProperties().get("complexType");
                String packageName = toNamespace(item.getProperties().get("namespace"));

                Collection<GenTemplateTraceSourceItem> sourceItems = filter(item.getSources(),
                        hasSourceRole(GenTemplateTrace.SOURCE_DATA_ROLE));
                for (GenTemplateTraceSourceItem sourceItem : sourceItems) {
                    String locator = sourceItem.getLocator();
                    createTrace(complexTypeName, packageName, locator);
                }
            }
        }

        protected void createTrace(String className, String packageName, String locator) {
            traceBuilder.newItem(TRACE_TYPE_JAXB_CLASS).withProperty(TRACE_PROP_JAXB_PACKAGE_NAME, packageName)
                    .withProperty(TRACE_PROP_JAXB_CLASS_NAME, className).withPrimarySourceItem(locator);
        }

        protected String toNamespace(String urlString) {
            NameConverter nameConverter = new NameConverter.Standard();
            return nameConverter.toPackageName(urlString);
        }
    }
}
