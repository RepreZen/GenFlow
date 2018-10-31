package com.reprezen.genflow.rapidml.jsonschema.xtend;

import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Options {

    public static final String ALLOW_EMPTY_OBJECT = "allowEmptyObject";
    public static final String ALLOW_EMPTY_ARRAY = "allowEmptyArray";
    public static final String ALLOW_EMPTY_STRING = "allowEmptyString";
    public static final String INLINE_ARRAY_ITEMS = "inlineArrayItems";
    public static final String INLINE_REFERENCE_EMBEDS = "inlineReferenceEmbeds";
    public static final String LINKS_PROPERTY_NAME = "linksPropertyName";

    public static Options fromParams(Map<String, Object> params) throws IllegalArgumentException {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        try {
            return mapper.convertValue(params, Options.class);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Some of the JSON Schema Generator parameters have invalid type: " + e.getMessage(), e);
        }
    }

    private boolean allowEmptyString;
    private boolean allowEmptyArray;
    private boolean allowEmptyObject;
    private boolean inlineArrayItems;
    private boolean inlineReferenceEmbeds;
    private String linksPropertyName = "_links";

    public boolean isAllowEmptyString() {
        return allowEmptyString;
    }

    public boolean isAllowEmptyArray() {
        return allowEmptyArray;
    }

    public boolean isAllowEmptyObject() {
        return allowEmptyObject;
    }

    public boolean isInlineArrayItems() {
        return inlineArrayItems;
    }

    public boolean isInlineReferenceEmbeds() {
        return inlineReferenceEmbeds;
    }

    public String getLinksPropertyName() {
        return linksPropertyName;
    }
}
