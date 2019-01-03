package com.reprezen.genflow.rapidml.doc;

public class LinkHelper {

    public static String sanitizeLink(String link) {
        if (link.startsWith("#")) {
            return "#" + sanitizeLink(link.substring(1));
        } else {
            return link.replaceAll("[^A-Za-z0-9_]", "_");
        }
    }
}
