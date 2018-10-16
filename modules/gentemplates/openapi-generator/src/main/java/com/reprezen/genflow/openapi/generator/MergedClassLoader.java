/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.openapi.generator;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Enumeration;

/**
 * @author Andy Lowry
 * 
 */
public class MergedClassLoader extends ClassLoader {

    private final ClassLoader[] loaders;

    public MergedClassLoader(ClassLoader... loaders) {
        this.loaders = loaders;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        ClassNotFoundException cnfe = null;
        for (ClassLoader loader : loaders) {
            try {
                return loader.loadClass(name);
            } catch (ClassNotFoundException e) {
                cnfe = e;
            }
        }
        throw cnfe != null ? cnfe : new ClassNotFoundException();
    }

    @Override
    public URL getResource(String name) {
        for (ClassLoader loader : loaders) {
            URL result = loader.getResource(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(final String name) throws IOException {
        final Deque<ClassLoader> loaderQ = new ArrayDeque<ClassLoader>();
        loaderQ.addAll(Arrays.asList(loaders));
        return new Enumeration<URL>() {
            private Enumeration<URL> currentEnum = null;

            @Override
            public boolean hasMoreElements() {
                while (currentEnum == null || !currentEnum.hasMoreElements()) {
                    if (loaderQ.isEmpty()) {
                        return false;
                    } else {
                        try {
                            currentEnum = loaderQ.removeFirst().getResources(name);
                        } catch (IOException e) {
                            currentEnum = null;
                        }
                    }
                }
                return true;
            }

            @Override
            public URL nextElement() {
                return hasMoreElements() ? currentEnum.nextElement() : null;
            }
        };
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (ClassLoader loader : loaders) {
            InputStream result = loader.getResourceAsStream(name);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        for (ClassLoader loader : loaders) {
            loader.setDefaultAssertionStatus(enabled);
        }
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        for (ClassLoader loader : loaders) {
            loader.setPackageAssertionStatus(packageName, enabled);
        }
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        for (ClassLoader loader : loaders) {
            loader.setClassAssertionStatus(className, enabled);
        }
    }

    @Override
    public void clearAssertionStatus() {
        for (ClassLoader loader : loaders) {
            loader.clearAssertionStatus();
        }
    }

}
