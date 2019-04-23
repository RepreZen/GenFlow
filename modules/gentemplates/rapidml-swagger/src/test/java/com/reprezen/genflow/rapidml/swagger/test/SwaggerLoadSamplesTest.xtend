/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 * 
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.rapidml.swagger.test

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.core.report.ProcessingReport
import com.github.fge.jsonschema.main.JsonSchema
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.reprezen.genflow.api.template.FakeGenTemplateContext
import com.reprezen.genflow.rapidml.swagger.SwaggerOutputFormat
import com.reprezen.genflow.rapidml.swagger.XGenerateSwagger
import com.reprezen.genflow.test.common.GeneratorTestFixture
import com.reprezen.rapidml.implicit.ZenModelNormalizer
import com.reprezen.rapidml.xtext.loaders.ZenModelLoader
import java.io.File
import java.net.JarURLConnection

import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.ArrayList
import java.util.Collections
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.eclipse.emf.common.util.URI

@RunWith(typeof(Parameterized))
class SwaggerLoadSamplesTest extends Assert {
    var JsonSchema schema = null

    @Parameters(name="{index}: {0}")
    def static Iterable<Object[]> data() {
        val uri = GeneratorTestFixture.classLoader.getResource("models/dsl/").toURI

        if (uri.toString.startsWith("jar:"))
            collectFiles(uri.toURL.openConnection as JarURLConnection, uri)
        else
            collectFiles(new File(uri.path))
    }

    private static def Iterable<Object[]> collectFiles(JarURLConnection connection, java.net.URI basePath) {
        Collections.list(connection.jarFile.entries) //
            .filter[name.startsWith("models/dsl/") && name.endsWith(".rapid")] //
            .map[name.substring("models/dsl/".length)] //
            .map[#[URI.createURI(basePath.toString + it)] as Object[]]
    }

    private static def Iterable<Object[]> collectFiles(File directory) {
        val files = new ArrayList<Object[]>

        Files.walkFileTree(directory.toPath, new SimpleFileVisitor<Path>() {
            override FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                if (path.last.toString.endsWith(".rapid")) {
                    files.add(#[URI.createFileURI(path.toAbsolutePath.toString)] as Object[])
                }
                return FileVisitResult.CONTINUE
            }
        })

        files
    }

    val URI modelPath

    new(URI modelPath) {
        this.modelPath = modelPath
    }

    @Test
    def void validateSwaggerModel() {
        val model = new ZenModelLoader().loadAndValidateModel(modelPath)
        new ZenModelNormalizer().normalize(model)

        val generator = new XGenerateSwagger()
        generator.init(
            new FakeGenTemplateContext(
                #{XGenerateSwagger::OUTPUT_FORMAT_PARAM -> SwaggerOutputFormat.JSON.toString as Object}))
        val swagger = generator.generate(model)
        val JsonSchema schema = getSwaggerSchema()
        val ProcessingReport report = schema.validate(JsonLoader.fromString(swagger), true);
        if (!report.success) {
            val StringBuilder errors = new StringBuilder
            report.forEach [
                errors.append(
                    it.logLevel.name.toUpperCase + ' [' + it.asJson.get('instance').get('pointer') + '] - ' +
                        it.message + '\n')
            ]
            fail(errors.toString)
        }
    }

    def getSwaggerSchema() {
        if (schema === null) {
            val schemaUrl = SwaggerLoadSamplesTest.getResource("schema.json")
            schema = JsonSchemaFactory.byDefault.getJsonSchema(schemaUrl.toString)
        }
        return schema
    }
}
