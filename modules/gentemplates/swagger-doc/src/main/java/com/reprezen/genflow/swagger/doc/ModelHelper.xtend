package com.reprezen.genflow.swagger.doc

import io.swagger.models.AbstractModel
import io.swagger.models.ArrayModel
import io.swagger.models.ComposedModel
import io.swagger.models.Model
import io.swagger.models.ModelImpl
import io.swagger.models.RefModel

class ModelHelper implements Helper {

    override init() {}

    /*
     * See comments at top of PropertyHelper - everything here is analogous
     */
    def Object getAttribute(Model model, String attr) {
        switch model {
            // leaf (height 1) types
            ModelImpl:
                getAttribute(model, attr)
            ComposedModel:
                getAttribute(model, attr)
            ArrayModel:
                getAttribute(model, attr)
            // height 2 types
            AbstractModel:
                getAttribute(model, attr)
            RefModel:
                getAttribute(model, attr)
        }
    }

    def private getAttribute(ModelImpl model, String attr) {
        val value = switch attr {
            case "additionalProperties": model.additionalProperties
            case "defaultValue": model.defaultValue
            case "description": model.description
            case "discriminator": model.discriminator
            case "enum": model.enum
            case "example": model.example
            case "format": model.format
            case "name": model.name
            case "properties": model.properties
            case "required": model.required
            case "type": model.type
            case "xml": model.xml
        }
        value ?: (model as AbstractModel).getAttribute(attr)
    }

    def private getAttribute(ComposedModel model, String attr) {

        val value = switch attr {
            case "allOf": model.allOf
            case "child": model.child
            case "description": model.description
            case "example": model.example
            case "interfaces": model.interfaces
            case "parent": model.parent
            case "properties": model.properties
            case "type": "allOf" // ComposedModel has no type field
        }
        value ?: (model as AbstractModel).getAttribute(attr)
    }

    def private getAttribute(ArrayModel model, String attr) {

        val value = switch attr {
            case "description": model.description
            case "example": model.example
            case "items": model.items
            case "properties": model.properties
            case "type": model.type
        }
        value ?: (model as AbstractModel).getAttribute(attr)
    }

    def private getAttribute(AbstractModel model, String attr) {
        switch attr {
            case "description": model.description
            case "example": model.example
            case "externalDocs": model.externalDocs
            case "properties": model.properties
            case "title": model.title
        }
    }

    def private getAttribute(RefModel model, String attr) {
        switch attr {
            case "$ref": model.$ref
        }
    }
}
