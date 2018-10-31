/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.xsd;

import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.template.GenTemplate;
import com.reprezen.genflow.api.template.GenTemplateContext;
import com.reprezen.genflow.api.template.GenTemplateProperty;
import com.reprezen.genflow.api.zenmodel.ZenModelGenTemplate;
import com.reprezen.genflow.common.xml.XmlFormatter;
import com.reprezen.rapidml.ZenModel;

/**
 * @author Konstantin Zaitsev
 * @date Jun 24, 2015
 */
public class XMLSchemaGenTemplate extends ZenModelGenTemplate {

    @Override
    public String getName() {
        return "XML Schema"; //$NON-NLS-1$
    }

    @Override
    public void configure() throws GenerationException {
        alsoKnownAs("com.modelsolv.reprezen.gentemplates.xsd.XMLSchemaGenTemplate");
        defineZenModelSource();
        define(parameter().named("valueForm") //
                .withDescription("One of ATTRIBUTE or ELEMENT, determining how single-valued primitive properties", //
                        "will be represented in generated types. Default is ATTRIBUTE.") //
                .withDefault(ValueForm.ATTRIBUTE.name()));
        define(parameter().named("listItemElementName") //
                .withDescription("Name for item elements within lists for multivalued properties.",
                        "This can be a fixed name, or can be a string containing '${property}'. In",
                        "that case, the relevent property name will replace '${property}' in the",
                        "specified value. The default is 'item'.") //
                .withDefault("item"));
        define(parameter().named("allowEmptyLists") //
                .withDescription("Generated schemas will allow empty lists for optional multivalued properties.",
                        "False (default) disallows this; a compliant document can omit an optional list, but if the",
                        "list is present it must not be empty.") //
                .withDefault(false));
        define(parameter().named("typeNamingMethod") //
                .withDescription("Choose the method to be used to name types in the generated schema. Options are:",
                        "* SIMPLE_NAME - (Default) Use the name of the associated resource, datatype, etc.",
                        "* FULLY_QUALIFIED_NAME - Use the fully qualifiied name of the associated resource, datatype, etc.")
                .withDefault(TypeNamingMethod.SIMPLE_NAME.name()));
        define(outputItem().named("ResourceAPI").using(ResourceApiSchemaGenerator.class)
                .writing("${org.eclipse.xtext.xbase.lib.StringExtensions.toFirstLower(resourceAPI.name)}.xsd"));
        define(outputItem().named("DataModel").using(DataModelSchemaGenerator.class));
        define(staticResource().copying("/resources").to("."));
        define(GenTemplateProperty.reprezenProvider());
    }

    @Override
    public StaticGenerator<ZenModel> getStaticGenerator() {
        return new Generator(this, context);
    }

    public static class Generator extends GenTemplate.StaticGenerator<ZenModel> {
        private final XmlFormatter formatter = new XmlFormatter();

        public Generator(GenTemplate<ZenModel> genTemplate, GenTemplateContext context) {
            super(genTemplate, context);
        }

        @Override
        protected String postProcessContent(String content) throws Exception {
            return formatter.format(content);
        }
    }

    static enum ValueForm {
        ATTRIBUTE, ELEMENT
    }

    static enum TypeNamingMethod {
        SIMPLE_NAME, FULLY_QUALIFIED_NAME
    }

    static class Config {
        private ValueForm valueForm = ValueForm.ATTRIBUTE;
        private String listItemElementName = "item";
        private boolean allowEmptyLists = false;
        private TypeNamingMethod typeNamingMethod = TypeNamingMethod.SIMPLE_NAME;

        public ValueForm getValueForm() {
            return valueForm;
        }

        public String getListItemElementName() {
            return listItemElementName;
        }

        public boolean isAllowEmptyLists() {
            return allowEmptyLists;
        }

        public TypeNamingMethod getTypeNamingMethod() {
            return typeNamingMethod;
        }
    }
}
