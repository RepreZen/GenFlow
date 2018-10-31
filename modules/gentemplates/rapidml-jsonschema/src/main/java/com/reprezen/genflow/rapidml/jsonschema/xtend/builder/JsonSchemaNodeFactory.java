package com.reprezen.genflow.rapidml.jsonschema.xtend.builder;

import java.util.List;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.reprezen.genflow.common.xtend.XImportHelper;
import com.reprezen.genflow.rapidml.jsonschema.xtend.JsonSchemaFormat;
import com.reprezen.genflow.rapidml.jsonschema.xtend.Options;
import com.reprezen.genflow.rapidml.jsonschema.xtend.xchange.XChangeLinksMapNode;
import com.reprezen.rapidml.CollectionResource;
import com.reprezen.rapidml.Constraint;
import com.reprezen.rapidml.Enumeration;
import com.reprezen.rapidml.Feature;
import com.reprezen.rapidml.PrimitiveType;
import com.reprezen.rapidml.PropertyRealization;
import com.reprezen.rapidml.RealizationContainer;
import com.reprezen.rapidml.ReferenceEmbed;
import com.reprezen.rapidml.ReferenceLink;
import com.reprezen.rapidml.ReferenceTreatment;
import com.reprezen.rapidml.ResourceDefinition;
import com.reprezen.rapidml.ServiceDataResource;
import com.reprezen.rapidml.Structure;
import com.reprezen.rapidml.TypedMessage;
import com.reprezen.rapidml.UserDefinedType;
import com.reprezen.rapidml.ZenModel;
import com.reprezen.rapidml.util.ResourceFinder;

public class JsonSchemaNodeFactory {
    protected final JsonSchemaFormat schemaFormat;
    private XImportHelper importHelper = new XImportHelper();
    private XChangeLinksMapNode rapidLinkNode = new XChangeLinksMapNode(this, null);

    private ResourceFinder resourceFinder;
    private DefinitionsNode definitionsNode;
    private Options options;

    public JsonSchemaNodeFactory(JsonSchemaFormat schemaFormat) {
        this.schemaFormat = schemaFormat;
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    protected Options getOptions() {
        return options;
    }

    public ObjectNode generateDefinitionsNode(ZenModel model) {
        importHelper.init(model);
        resourceFinder = new ResourceFinder(model.getResourceAPIs().isEmpty() ? null : model.getResourceAPIs().get(0));
        definitionsNode = new DefinitionsNode(this, model);
        return definitionsNode.write(null);
    }

    public DefinitionsNode getDefinitionsNode() {
        return definitionsNode;
    }

    public NamedSchemaNode<Void> createRapidLinkNode() {
        return rapidLinkNode;
    }

    public NamedSchemaNode<Structure> createStructureNode(Structure element) {
        return new StructureNode(this, element);
    }

    public NamedSchemaNode<TypedMessage> createTypedMessageNode(TypedMessage element) {
        return new TypedMessageNode(this, element);
    }

    public NamedSchemaNode<?> createDefaultRealizationNode(RealizationContainer element) {
        return createStructureNode(element.getActualType());
    }

    public NamedSchemaNode<?> createDefaultLinkNode(ReferenceLink element) {
        return createResourceLinkNode(element.getTargetResource());
    }

    public JsonSchemaNode<Constraint> createConstraintNode(Constraint element) {
        return new ConstraintNode(this, element);
    }

    public JsonSchemaNode<UserDefinedType> createUserDefinedTypeNode(UserDefinedType element) {
        return new UserDefinedTypeNode(this, element, importHelper);
    }

    public JsonSchemaNode<PrimitiveType> createPrimitiveTypeNode(PrimitiveType element) {
        boolean useSwaggerFormat = schemaFormat.useSwaggerStyleBase64Binary;
        if (useSwaggerFormat) {
            return new SwaggerPrimitiveTypeNode(this, element);
        }
        return new PrimitiveTypeNode(this, element);
    }

    public <T extends ServiceDataResource> NamedSchemaNode<ServiceDataResource> createResourceNode(T element) {
        if (element instanceof CollectionResource) {
            return createCollectionResourceNode(element);
        }
        return createObjectResourceNode(element);
    }

    public NamedSchemaNode<ResourceDefinition> createResourceLinkNode(ResourceDefinition element) {
        return new ResourceLinkNode(this, element);
    }

    public PropertyNode<Feature> createFeatureNode(Feature element) {
        return new FeatureNode(this, element);
    }

    public <T extends ServiceDataResource> NamedSchemaNode<ServiceDataResource> createCollectionResourceNode(
            T element) {
        return new CollectionResourceNode(this, element);
    }

    public <T extends ServiceDataResource> NamedSchemaNode<ServiceDataResource> createObjectResourceNode(T element) {
        return new ObjectResourceNode(this, element);
    }

    public NamedSchemaNode<Enumeration> createEnumerationNode(Enumeration element) {
        return new EnumerationNode(this, element, importHelper);
    }

    public PropertyNode<PropertyRealization> createPropertyRealizationNode(PropertyRealization element) {
        return new PropertyRealizationNode(this, element);
    }

    public <T extends ReferenceTreatment> ReferenceTreatmentNode<? extends ReferenceTreatment> createReferenceTreatmentNode(
            T element) {
        if (element instanceof ReferenceLink) {
            return new ReferenceLinkNode(this, (ReferenceLink) element);
        }
        return new ReferenceEmbedNode(this, (ReferenceEmbed) element);

    }

    public ServiceDataResource getDefaultResource(Structure structure, boolean isMultiValued) {
        if (isMultiValued) {
            // ZEN-3978 no collection resource available -> try an array of object resources
            // We cannot reuse the value set by normalizer for ReferenceLinks because the
            // RAPID-XChange-Interop
            // generator operates with structures which don't have realizations
            return resourceFinder.tryFindCollectionThenObjectResource(structure);
        }
        return resourceFinder.findResource(structure, !isMultiValued);
    }

    public String getRapidLinkPropertyName() {
        return options.getLinksPropertyName();
    }

    public ObjectNode addRapidLink(ObjectNode propertiesNode, List<String> requiredProperties,
            ReferenceLink referenceLink) {
        if (ReferenceLinkNode.isLinkToCollectionWithItemBasedProperties(referenceLink)) {
            // ZEN-3978 - don't generate a link to a collection resource with item-level
            // properties
            return null;
        }
        ObjectNode rapidLink = propertiesNode.putObject(getRapidLinkPropertyName());
        NamedSchemaNode<Void> rapidLinkNode = createRapidLinkNode();
        getDefinitionsNode().addReferenceToDefinition(rapidLink, rapidLinkNode);
        return rapidLink;
    }

}
