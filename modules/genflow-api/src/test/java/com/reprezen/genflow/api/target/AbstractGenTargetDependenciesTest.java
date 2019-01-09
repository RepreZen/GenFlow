/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.target;

import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.target.CircularDependenciesException;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetPrerequisite;
import com.reprezen.genflow.api.target.GenTargetPrimarySource;
import com.reprezen.genflow.api.target.GenTargetUtils;
import com.reprezen.genflow.api.target.IncorrectGenTargetException;
import com.reprezen.genflow.api.template.GenTemplateDependencies;
import com.reprezen.genflow.api.template.IGenTemplate;
import com.reprezen.genflow.api.template.IGenTemplate.Generator;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;

/**
 * @author Konstantin Zaitsev
 * 
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ GenTargetUtils.class })
public abstract class AbstractGenTargetDependenciesTest {

    private final Map<File, GenTarget> targets = Maps.newHashMap();

    protected abstract void registerGenTemplate(String id, IGenTemplate genTemplate);

    protected abstract void executeGenTargets(String... genTargets)
            throws IncorrectGenTargetException, GenerationException, IOException;

    public void initMocks() throws Exception {
        PowerMockito.mockStatic(GenTargetUtils.class);
        when(GenTargetUtils.load(Mockito.any(File.class))).thenAnswer(new Answer<GenTarget>() {
            @Override
            public GenTarget answer(InvocationOnMock invocation) throws Throwable {
                File targetFile = (File) invocation.getArguments()[0];
                return targets.get(targetFile);
            }
        });
    }

    @Ignore
    @Test
    public void testDependenciesChain() throws IOException, IncorrectGenTargetException, GenerationException {
        // create mocks
        IGenTemplate genTemplate1 = mockGenTemplate("gen_template1", "gen2", "gen_template2", "gen3", "gen_template3");
        IGenTemplate genTemplate2 = mockGenTemplate("gen_template2", "gen3", "gen_template3");
        IGenTemplate genTemplate3 = mockGenTemplate("gen_template3");

        mockGenTarget("gen-target1.gen", "gen_template1", "gen2", "gen-target2.gen", "gen3", "gen-target3.gen");
        mockGenTarget("gen-target2.gen", "gen_template2", "gen3", "gen-target3.gen");
        mockGenTarget("gen-target3.gen", "gen_template3");

        // execute
        executeGenTargets("gen-target1.gen");

        // verify
        verifyGenerationOrder(genTemplate3, genTemplate2, genTemplate1);
    }

    @Ignore
    @Test
    public void testBatchGenTargetExecution() throws IOException, IncorrectGenTargetException, GenerationException {
        // create mocks
        IGenTemplate genTemplate1 = mockGenTemplate("gen_template1", "gen2", "gen_template2", "gen3", "gen_template3");
        IGenTemplate genTemplate2 = mockGenTemplate("gen_template2", "gen3", "gen_template3");
        IGenTemplate genTemplate3 = mockGenTemplate("gen_template3");

        mockGenTarget("gen-target1.gen", "gen_template1", "gen2", "gen-target2.gen", "gen3", "gen-target3.gen");
        mockGenTarget("gen-target2.gen", "gen_template2", "gen3", "gen-target3.gen");
        mockGenTarget("gen-target3.gen", "gen_template3");

        // execute
        executeGenTargets("gen-target2.gen", "gen-target1.gen", "gen-target3.gen");

        // verify
        verifyGenerationOrder(genTemplate3, genTemplate2, genTemplate1);
    }

    @Ignore
    @Test(expected = CircularDependenciesException.class)
    public void testCircularDependencies() throws IOException, IncorrectGenTargetException, GenerationException {
        // create mocks
        mockGenTemplate("gen_template1", "gen2", "gen_template2", "gen3", "gen_template3");
        mockGenTemplate("gen_template2", "gen3", "gen_template3", "gen1", "gen_template1");
        mockGenTemplate("gen_template3");

        mockGenTarget("gen-target1.gen", "gen_template1", "gen2", "gen-target2.gen", "gen3", "gen-target3.gen");
        mockGenTarget("gen-target2.gen", "gen_template2", "gen3", "gen-target3.gen", "gen1", "gen-target1.gen");
        mockGenTarget("gen-target3.gen", "gen_template3");

        // execute
        executeGenTargets("gen-target1.gen");
    }

    private void verifyGenerationOrder(IGenTemplate... genTemplates) throws GenerationException {
        InOrder order = Mockito.inOrder((Object[]) genTemplates);
        for (IGenTemplate genTemplate : genTemplates) {
            Generator generator = genTemplate.getGenerator();
            order.verify(generator, Mockito.times(1)).generate(Mockito.any(GenTarget.class),
                    Mockito.any(GenTemplateTraces.class));
        }
    }

    private IGenTemplate mockGenTemplate(String id, String... depEntries) throws GenerationException {
        GenTemplateDependencies dependencies = new GenTemplateDependencies();

        for (int i = 0; i < depEntries.length; i += 2) {
            dependencies.addRequiredGeneratorDependency(depEntries[i], depEntries[i + 1], null);
        }

        IGenTemplate genTemplate = Mockito.mock(IGenTemplate.class);
        Generator generator = Mockito.mock(Generator.class);
        when(genTemplate.getGenerator()).thenReturn(generator);
        when(genTemplate.getDependencies()).thenReturn(dependencies.get());
        when(generator.generate(Mockito.any(GenTarget.class), Mockito.any(GenTemplateTraces.class)))
                .thenReturn(new GenTemplateTraceBuilder(id).build());
        registerGenTemplate(id, genTemplate);
        return genTemplate;
    }

    private void mockGenTarget(String targetName, String genTemplate, String... sEntries) throws GenerationException {
        targets.put(new File(targetName), createGenTarget(genTemplate, sEntries));
    }

    private GenTarget createGenTarget(String genTemplate, String... prereqs) throws GenerationException {
        GenTarget target = new GenTarget();
        target.setBaseDir(new File("."));
        target.setGenTemplateId(genTemplate);
        target.setRelativeOutputDir(new File("generated"));
        target.setParameters(Maps.<String, Object> newHashMap());
        GenTargetPrimarySource primarySource = new GenTargetPrimarySource();
        primarySource.setPath(new File("test.rapid"));
        target.setPrimarySource(primarySource);
        List<GenTargetPrerequisite> prerequisites = Lists.newArrayList();
        for (int i = 0; i < prereqs.length; i += 2) {
            GenTargetPrerequisite prereq = new GenTargetPrerequisite();
            prereq.setName(prereqs[i]);
            prereq.setGenFilePath(new File(prereqs[i + 1]));
            prerequisites.add(prereq);
        }
        target.setPrerequisiteList(prerequisites);
        return target;
    }
}
