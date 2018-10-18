package com.reprezen.genflow.swagger.doc

import com.reprezen.genflow.swagger.doc.Helper
import io.swagger.models.properties.AbstractNumericProperty
import io.swagger.models.properties.AbstractProperty
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.BaseIntegerProperty
import io.swagger.models.properties.BinaryProperty
import io.swagger.models.properties.BooleanProperty
import io.swagger.models.properties.ByteArrayProperty
import io.swagger.models.properties.DateProperty
import io.swagger.models.properties.DateTimeProperty
import io.swagger.models.properties.EmailProperty
import io.swagger.models.properties.FileProperty
import io.swagger.models.properties.IntegerProperty
import io.swagger.models.properties.LongProperty
import io.swagger.models.properties.ObjectProperty
import io.swagger.models.properties.PasswordProperty
import io.swagger.models.properties.Property
import io.swagger.models.properties.StringProperty
import io.swagger.models.properties.UUIDProperty

class PropertyHelper implements Helper {
    override init() {}

    /**
     * getAttribtue attempts to obtain a named attribute from a Property object.
     * <p>
     * These attributes are scattered through the Property type hierarchy and so are not accessible without casting to the
     * appropriate type. Here we provide a Property-level method that returns a requested property from wherever in the type
     * hierarchy it is defined for the given property, or null if it is not available (or actually has the value null).
     * <p> 
     * getAttribute is defined as an overloaded method, and there is an implementation for every class in the Property type hierarchy.
     * The overload for Property type implements dynamic dispatch based on the actual type of the supplied property and is the only 
     * non-private overload. Dynamic dispatch is not suitable for other overloads because they use the same overloads to delegate up the 
     * supertype chain to find inherited attributes. 
     * <p>
     * Each overload directly supports all values for which its dispatch class defines a getter; for other values, each method delegates 
     * to its supertype overload.
     * <p>
     * Xtend's native dynamic dispatch is unsuitable for this class, though it might appear to be exactly what's needed. The problem
     * is that we need, at the end of most of the overloads, to call up through the overloads for supertypes, looking for one that can
     * supply the requested attribute. Were these all "dispatch" methods, the supertype calls would be impossible - all attempts to do
     * so would remain stuck at the actual type of the property being interrogated, and infinite recursion would result.
     */
    def Object getAttribute(Property prop, String attr) {

        // Order of cases is important, as first matching case wins. We need to dispatch to the overload
        // corresponding to the actual type of prop, not to an of its supertypes.
        // Recall that xtend automatically casts a switch value in type-guarded cases, so the dispatched
        // calls below will got to the correct overloads without explicit casts.
        switch prop {
            // leaf types (height 1) must come first
            BinaryProperty:
                prop.getAttribute(attr)
            BooleanProperty:
                prop.getAttribute(attr)
            ByteArrayProperty:
                prop.getAttribute(attr)
            DateProperty:
                prop.getAttribute(attr)
            DateTimeProperty:
                prop.getAttribute(attr)
            EmailProperty:
                prop.getAttribute(attr)
            FileProperty:
                prop.getAttribute(attr)
            IntegerProperty:
                prop.getAttribute(attr)
            LongProperty:
                prop.getAttribute(attr)
            PasswordProperty:
                prop.getAttribute(attr)
            UUIDProperty:
                prop.getAttribute(attr)
            // then supertypes at height 2
            BaseIntegerProperty:
                prop.getAttribute(attr)
            StringProperty:
                prop.getAttribute(attr)
            // height 3
            AbstractNumericProperty:
                prop.getAttribute(attr)
            // height 4
            ArrayProperty:
                prop.getAttribute(attr)
            ObjectProperty:
                prop.getAttribute(attr)
            // height 5
            AbstractProperty:
                prop.getAttribute(attr)
            default:
                throw new IllegalArgumentException("Unhandled Property type")
        }
    }

    // note that in the following overload definitions it would be natural to delegate to the supertype overload in a "default"
    // case of the switch expression. But xtend compiler generates bogus java code with compilation errors in that case, hence the 
    // alternative approach used here
    def private getAttribute(AbstractProperty prop, String attr) {
        val value = switch attr {
            case "description": prop.description
            case "example": prop.example
            case "format": prop.format
            case "name": prop.name
            case "position": prop.position
            case "readOnly": prop.getReadOnly
            case "required": prop.required
            case "title": prop.title
            case "type": prop.type
            case "xml": prop.xml
        }
        value
    }

    def private getAttribute(ArrayProperty prop, String attr) {
        val value = switch attr {
            case "items": prop.items
            case "minItems": prop.minItems
            case "maxItems": prop.maxItems
            case "uniqueItems": prop.getUniqueItems
        }
        value ?: (prop as AbstractProperty).getAttribute(attr)
    }

    def private getAttribute(ObjectProperty prop, String attr) {
        val value = switch attr {
            case "properties": prop.properties
            case "requiredProperties": prop.requiredProperties
        }
        value ?: (prop as AbstractProperty).getAttribute(attr)
    }

    def private getAttribute(AbstractNumericProperty prop, String attr) {
        val value = switch attr {
            case "minimum": prop.minimum
            case "maximum": prop.maximum
            case "exclusiveMinimum": prop.exclusiveMinimum
            case "exclusiveMaximum": prop.exclusiveMaximum
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(BaseIntegerProperty prop, String attr) {
        (prop as AbstractNumericProperty).getAttribute(attr)
    }

    def private getAttribute(IntegerProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
        }
        (value ?: (prop as BaseIntegerProperty).getAttribute(attr))
    }

    def private getAttribute(LongProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
        }
        (value ?: (prop as BaseIntegerProperty).getAttribute(attr))
    }

    def private getAttribute(BinaryProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
            case "minLength": prop.minLength
            case "maxLength": prop.maxLength
            case "pattern": prop.pattern
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(BooleanProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(ByteArrayProperty prop, String attr) {
        (prop as AbstractProperty).getAttribute(attr)
    }

    def private getAttribute(DateProperty prop, String attr) {
        val value = switch attr {
            case "enum": prop.enum
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(DateTimeProperty prop, String attr) {
        val value = switch attr {
            case "enum": prop.enum
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(FileProperty prop, String attr) {
        (prop as AbstractProperty).getAttribute(attr)
    }

    def private getAttribute(PasswordProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
            case "minLength": prop.minLength
            case "maxLength": prop.maxLength
            case "pattern": prop.pattern
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(StringProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
            case "minLength": prop.minLength
            case "maxLength": prop.maxLength
            case "pattern": prop.pattern
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

    def private getAttribute(EmailProperty prop, String attr) {
        (prop as StringProperty).getAttribute(attr)
    }

    def private getAttribute(UUIDProperty prop, String attr) {
        val value = switch attr {
            case "default": prop.^default
            case "enum": prop.enum
            case "minLength": prop.minLength
            case "maxLength": prop.maxLength
            case "pattern": prop.pattern
        }
        (value ?: (prop as AbstractProperty).getAttribute(attr))
    }

}
