/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.modelsolv.reprezen.core.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Pretty-prints xml, supplied as a string.
 * <p/>
 * eg. <code>
 * String formattedXml = new XmlFormatter().format("<tag><nested>hello</nested></tag>");
 * </code>
 * 
 * Requires Java 6 to successfully run.
 * 
 * @see http ://stackoverflow.com/questions/139076/how-to-pretty-print-xml-from-java
 */
public class XmlFormatter {

    public String format(String xml) throws RuntimeException {
        try {
            final InputSource src = new InputSource(new StringReader(xml));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Transformer transformer;

            Document document = documentBuilder.parse(src);

            transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "false");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(document);

            transformer.transform(source, result);

            return result.getWriter().toString();

        } catch (TransformerFactoryConfigurationError | TransformerException | SAXException | IOException
                | ParserConfigurationException e) {
            throwNotProcess(e);
        }

        return null;
    }

    private void throwNotProcess(Throwable e) {
        throw new RuntimeException("Could not process the XML file to pretty-print.", e);
    }
}
