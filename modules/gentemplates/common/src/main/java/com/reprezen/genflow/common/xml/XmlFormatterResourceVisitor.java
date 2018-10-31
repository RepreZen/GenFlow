/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.common.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Resource visitor for XML pretty-printing.
 * 
 * @author jimleroyer
 * @since 2013/07/10
 */
public class XmlFormatterResourceVisitor implements IResourceVisitor {

    @Override
    public boolean visit(IResource iResource) throws CoreException {
        if (iResource instanceof IFolder) {
            return true;
        }
        if (!(iResource instanceof IFile)) {
            return false;
        }
        if (!"xml".equalsIgnoreCase(iResource.getFileExtension())
                && !"xsd".equalsIgnoreCase(iResource.getFileExtension())
                && !"wadl".equalsIgnoreCase(iResource.getFileExtension())) {
            return false;
        }

        XmlFormatter formatter = new XmlFormatter();
        IPath path = iResource.getLocation();
        File file = path.toFile();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file.getAbsolutePath());
            String xml = "";
            try (BufferedReader reader = new BufferedReader(fileReader)) {
                xml = reader.lines().collect(Collectors.joining("\n"));
            }
            String formattedXml = formatter.format(xml);
            Files.write(file.toPath(), formattedXml.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            // TODO: Find a way to log or report the error in a non-ui plug-in.
            // IStatus status = new Status(IStatus.ERROR, "com.modelsolv.reprezen.gentemplates.wadl",
            // "Could not find the file to format: " + file.getAbsolutePath(), e);
            return false;
        } catch (IOException e) {
            // TODO: Find a way to log or report the error in a non-ui plug-in.
            // IStatus status = new Status(IStatus.ERROR, "com.modelsolv.reprezen.gentemplates.wadl",
            // "Could not read the file to format: " + file.getAbsolutePath(), e);
            return false;
        } catch (Exception e) {
            // TODO: Find a way to log or report the error in a non-ui plug-in.
            // IStatus status = new Status(IStatus.ERROR, "com.modelsolv.reprezen.gentemplates.wadl",
            // "Could not read the file to format: " + file.getAbsolutePath(), e);
            return false;
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (final IOException e) {
                    // ignored
                }
            }
        }

        return false;
    }
}
