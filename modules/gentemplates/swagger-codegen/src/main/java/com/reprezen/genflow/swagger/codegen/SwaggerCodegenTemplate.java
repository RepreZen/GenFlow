/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.swagger.codegen;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.swagger.SwaggerGenTemplate;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Swagger;

public abstract class SwaggerCodegenTemplate extends SwaggerGenTemplate {

    public static final String SWAGGER_CODEGEN_SYSTEM_PROPERTIES = "swaggerCodegenSystemProperties";
    public static final String SWAGGER_CODEGEN_CONFIG = "swaggerCodegenConfig";

    protected final Class<? extends CodegenConfig> codegenClass;

    public SwaggerCodegenTemplate(Class<? extends CodegenConfig> codegenClass) {
        this.codegenClass = codegenClass;
    }

    @Override
    public void init() throws GenerationException {
        super.init();
        define(parameter().named("swaggerCodegenConfig").optional().withDescription(
                "Contents of swagger codegen configuration file.",
                "This is the file that would be passed with --config option on swagger codegen command line.",
                "The JSON contents of that file should be the value of this parameter."));
        define(parameter().named("swaggerCodegenSystemProperties").optional().withDescription(
                "System properties to set, as in the -D option of swagger codegen command line.",
                "Each property should be a json object with a name/value pair for each property.",
                "Example: '-Dmodels -Dapis=User,Pets' becomes '{\"models\" : \"\", \"apis\" : \"User,Pets\"}'"));
    }

    @Override
    protected StaticGenerator<Swagger> getStaticGenerator() {
        return new Generator(this, context, codegenClass);
    }

    public static class Generator extends GenTemplate.StaticGenerator<Swagger> {
        private Class<? extends CodegenConfig> codegenClass;

        public Generator(GenTemplate<Swagger> genTemplate, GenTemplateContext context,
                Class<? extends CodegenConfig> codegenClass) {
            super(genTemplate, context);
            this.codegenClass = codegenClass;
        }

        @Override
        public void generate(Swagger model) throws GenerationException {
            CodegenConfig swaggerCodegen;
            try {
                swaggerCodegen = codegenClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new GenerationException("Failed to instantiate Swagger Codegen instance", e);
            }
            swaggerCodegen.setOutputDir(context.getOutputDirectory().getAbsolutePath());
            @SuppressWarnings("unchecked")
            Map<String, String> config = (Map<String, String>) context.getGenTargetParameters()
                    .get(SWAGGER_CODEGEN_CONFIG);
            ClientOptInput clientOptInput = new ClientOptInput();
            clientOptInput.setConfig(swaggerCodegen);
            ClientOpts clientOpts = new ClientOpts();
            clientOpts.setOutputDirectory(context.getOutputDirectory().getAbsolutePath());
            clientOpts.setProperties(config != null ? config : Maps.<String, String> newHashMap());
            clientOptInput.setOpts(clientOpts);
            clientOptInput.setSwagger(model);
            io.swagger.codegen.Generator generator = new DefaultGenerator();
            @SuppressWarnings("unchecked")
            Map<String, String> systemProperties = (Map<String, String>) context.getGenTargetParameters()
                    .get(SWAGGER_CODEGEN_SYSTEM_PROPERTIES);
            setSystemProperties(systemProperties);
            generator.opts(clientOptInput);
            List<File> result = generator.generate();
            for (File next : result) {
                System.out.println("File: " + next.getAbsolutePath());
            }
        }

        private void setSystemProperties(Map<String, String> properties) {
            if (properties != null) {
                for (String key : properties.keySet()) {
                    System.setProperty(key, properties.get(key));
                }
            }
        }

    }
}
