/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.generator;

import org.openapitools.codegen.CodegenConfig;
import org.openapitools.codegen.CodegenType;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.openapi.generator.OpenApiGeneratorModulesInfo.Info;
import com.reprezen.genflow.openapi.generator.OpenApiGeneratorModulesInfo.Parameter;

public class BuiltinOpenApiGeneratorGenTemplate extends OpenApiGeneratorGenTemplate {

    private final OpenApiGeneratorModulesInfo.Info info;

    public BuiltinOpenApiGeneratorGenTemplate(Class<? extends CodegenConfig> codegenClass,
            OpenApiGeneratorModulesInfo.Info info) {
        super(codegenClass, info);
        this.info = info;
    }

    @Override
    public String getName() {
        return getPreferredName(codegenClass, info);
    }

    @Override
    public String getId() {
        return getClass().getPackage().getName() + "." + codegenClass.getSimpleName();
    }

    @Override
    public void configure() throws GenerationException {
        if (info != null) {
            for (Parameter param : info.getParameters()) {
                define(parameter().named(param.getName()).required(param.isRequired())
                        .withDescription(param.getDescription()).withDefault(param.getDefaultValue()));
            }
        }
        super.configure();
    }

    public static String getPreferredName(Class<? extends CodegenConfig> codegenClass, Info info) {
        if (info != null) {
            if (info.getDisplayName() != null) {
                return info.getDisplayName().trim();
            } else if (info.getDerivedDisplayName() != null) {
                return info.getDerivedDisplayName().trim();
            }
            // metadata is deficient (shouldn't happen) ... do a discovery-like name generation
        }
        try {
            return getDerivedName(codegenClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            return "(unknown)";
        }
    }

    public static String getDerivedName(CodegenConfig instance) {
        if (instance == null) {
            return "(unknown)";
        }

        String name = instance.getClass().getSimpleName();
        name = trimFromEnd(name, "Generator");
        name = trimFromEnd(name, "Codegen");

        CodegenType type = instance != null ? instance.getTag() : null;
        if (type == null) {
            type = CodegenType.OTHER;
        }
        switch (type) {
        case CLIENT:
            name = trimFromEnd(name, "Client") + " Client";
            break;
        case SERVER:
            name = trimFromEnd(name, "Server") + " Server";
            break;
        case DOCUMENTATION:
            name = trimFromEnd(name, "Doc", "Documentation") + " Documentation";
            break;
        case CONFIG:
            name = trimFromEnd(name, "Config", "Configuration") + " Configuration";
            break;
        case OTHER:
        default:
            name = trimFromEnd(name, "Client", "Server", "Doc", "Documentation", "Config", "Confiuration");
            break;
        }
        name = camelToNatural(name);
        return name;
    }

    private static String trimFromEnd(String s, String... suffixes) {
        for (String suffix : suffixes) {
            s = s.endsWith(suffix) ? s.substring(0, s.length() - suffix.length()) : s;
        }
        return s.trim();
    }

    private static String camelToNatural(String s) {
        return s.replaceAll("(\\p{Lower})(\\p{Upper})", "$1 $2");
    }
}
