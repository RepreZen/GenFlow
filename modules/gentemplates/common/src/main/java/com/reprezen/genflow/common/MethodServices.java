/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of Java helpers for queries. *<br>
 * This class was moved from the gentemplates.d3js project because of ZEN-458 exceptions in java services in built
 * product
 * 
 * @author Tatiana Fesenko <tatiana.fesenko@modelsolv.com>
 * 
 */
public class MethodServices {
    private final static Map<Integer, String> STATUS_CODES = new HashMap<>();

    static {
        // 2xx codes
        STATUS_CODES.put(200, "OK");
        STATUS_CODES.put(201, "Created");
        STATUS_CODES.put(202, "Accepted");
        STATUS_CODES.put(203, "Non-Authoritative Information");
        STATUS_CODES.put(204, "No Content");
        STATUS_CODES.put(205, "Reset Content");
        STATUS_CODES.put(206, "Partial Content");
        STATUS_CODES.put(207, "Multi-Status");
        STATUS_CODES.put(208, "Already Reported");
        STATUS_CODES.put(226, "IM Used");
        // 3xx codes
        STATUS_CODES.put(300, "Multiple Choices");
        STATUS_CODES.put(301, "Moved Permanently");
        STATUS_CODES.put(302, "Found");
        STATUS_CODES.put(303, "See Other");
        STATUS_CODES.put(304, "Not Modified");
        STATUS_CODES.put(305, "Use Proxy");
        STATUS_CODES.put(306, "Switch Proxy");
        STATUS_CODES.put(307, "Temporary Redirect");
        STATUS_CODES.put(308, "Permanent Redirect");
        // 4xx codes
        STATUS_CODES.put(400, "Bad Request");
        STATUS_CODES.put(401, "Unauthorized");
        STATUS_CODES.put(402, "Payment Required");
        STATUS_CODES.put(403, "Forbidden");
        STATUS_CODES.put(404, "Not Found");
        STATUS_CODES.put(405, "Method Not Allowed");
        STATUS_CODES.put(406, "Not Acceptable");
        STATUS_CODES.put(407, "Proxy Authentication Required");
        STATUS_CODES.put(408, "Request Timeout");
        STATUS_CODES.put(409, "Conflict");

        STATUS_CODES.put(410, "Gone");
        STATUS_CODES.put(411, "Length Required");
        STATUS_CODES.put(412, "Precondition Failed");
        STATUS_CODES.put(413, "Request Entity Too Large");
        STATUS_CODES.put(414, "Request-URI Too Long");
        STATUS_CODES.put(415, "Unsupported Media Type");
        STATUS_CODES.put(416, "Requested Range Not Satisfiable");
        STATUS_CODES.put(417, "Expectation Failed");
        STATUS_CODES.put(418, "I'm a teapot");
        STATUS_CODES.put(419, "Authentication Timeout");

        STATUS_CODES.put(422, "Unprocessable Entity");
        STATUS_CODES.put(423, "Locked");
        STATUS_CODES.put(424, "Failed Dependency");
        STATUS_CODES.put(425, "Unordered Collection");
        STATUS_CODES.put(426, "Upgrade Required");
        STATUS_CODES.put(428, "Precondition Required");
        STATUS_CODES.put(429, "Too Many Requests");

        STATUS_CODES.put(431, "Request Header Fields Too Large");

        STATUS_CODES.put(440, "Login Timeout");
        STATUS_CODES.put(444, "No Response");
        STATUS_CODES.put(449, "Retry With");

        STATUS_CODES.put(450, "Blocked by Windows Parental Controls");
        STATUS_CODES.put(451, "Unavailable For Legal Reasons");

        STATUS_CODES.put(494, "Request Header Too Large");
        STATUS_CODES.put(495, "Cert Error");
        STATUS_CODES.put(496, "No Cert");
        STATUS_CODES.put(497, "HTTP to HTTPS");
        STATUS_CODES.put(499, "Client Closed Request");

        // 5xx codes
        STATUS_CODES.put(500, "Internal Server Error");
        STATUS_CODES.put(501, "Not Implemented");
        STATUS_CODES.put(502, "Bad Gateway");
        STATUS_CODES.put(503, "Service Unavailable");
        STATUS_CODES.put(504, "Gateway Timeout");
        STATUS_CODES.put(505, "HTTP Version Not Supported");
        STATUS_CODES.put(506, "Variant Also Negotiates");
        STATUS_CODES.put(507, "Insufficient Storage");
        STATUS_CODES.put(508, "Loop Detected");
        STATUS_CODES.put(509, "Bandwidth Limit Exceeded");

        STATUS_CODES.put(510, "Not Extended");
        STATUS_CODES.put(511, "Network Authentication Required");
        STATUS_CODES.put(520, "Origin Error");
        STATUS_CODES.put(522, "Connection timed out");
        STATUS_CODES.put(523, "Proxy Declined Request");
        STATUS_CODES.put(524, "A timeout occurred");
        STATUS_CODES.put(598, "Network read timeout error");
        STATUS_CODES.put(599, "Network connect timeout error");
    }

    public static String getResponseStatusCodeDescription(int statusCode) {
        String code = STATUS_CODES.get(statusCode);
        if (code != null) {
            return code;
        }
        return "";
    }
}
