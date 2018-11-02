/*******************************************************************************
 * Copyright © 2013, 2016 Modelsolv, Inc.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property
 * of ModelSolv, Inc. See the file license.html in the root directory of
 * this project for further information.
 *******************************************************************************/
package com.reprezen.genflow.api.template;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.reprezen.genflow.api.GenerationException;
import com.reprezen.genflow.api.outputitem.IOutputItem;
import com.reprezen.genflow.api.source.ILocator;
import com.reprezen.genflow.api.source.ISource;
import com.reprezen.genflow.api.target.GenTarget;
import com.reprezen.genflow.api.target.GenTargetNamedSource;
import com.reprezen.genflow.api.target.GenTargetPrerequisite;
import com.reprezen.genflow.api.template.builders.DynamicGeneratorBuilder;
import com.reprezen.genflow.api.template.builders.DynamicGeneratorBuilder.DynamicGeneratorSpec;
import com.reprezen.genflow.api.template.builders.NamedSourceBuilder;
import com.reprezen.genflow.api.template.builders.NamedSourceBuilder.NamedSourceSpec;
import com.reprezen.genflow.api.template.builders.OutputItemBuilder;
import com.reprezen.genflow.api.template.builders.OutputItemBuilder.OutputItemSpec;
import com.reprezen.genflow.api.template.builders.ParameterBuilder;
import com.reprezen.genflow.api.template.builders.ParameterBuilder.ParameterSpec;
import com.reprezen.genflow.api.template.builders.PrerequisiteBuilder;
import com.reprezen.genflow.api.template.builders.PrerequisiteBuilder.PrerequisiteSpec;
import com.reprezen.genflow.api.template.builders.PrimarySourceBuilder;
import com.reprezen.genflow.api.template.builders.PrimarySourceBuilder.PrimarySourceSpec;
import com.reprezen.genflow.api.template.builders.PropertyBuilder;
import com.reprezen.genflow.api.template.builders.StaticResourceBuilder;
import com.reprezen.genflow.api.template.builders.StaticResourceBuilder.StaticResourceSpec;
import com.reprezen.genflow.api.template.config.GenTargetConfigUtil;
import com.reprezen.genflow.api.template.config.GenTemplateConfig;
import com.reprezen.genflow.api.template.config.NamedSourceConfig;
import com.reprezen.genflow.api.template.config.OutputItemConfig;
import com.reprezen.genflow.api.template.config.ParameterConfig;
import com.reprezen.genflow.api.template.config.PrerequisiteConfig;
import com.reprezen.genflow.api.template.config.PrimarySourceConfig;
import com.reprezen.genflow.api.template.config.StaticResourceConfig;
import com.reprezen.genflow.api.trace.GenTemplateTrace;
import com.reprezen.genflow.api.trace.GenTemplateTraceBuilder;
import com.reprezen.genflow.api.trace.GenTemplateTraces;
import com.reprezen.genflow.api.util.Eval;
import com.reprezen.genflow.api.util.Eval.VarName;
import com.reprezen.genflow.api.util.FileUtils;
import com.reprezen.genflow.api.util.Strings;
import com.reprezen.genflow.api.util.TypeUtils;

public abstract class GenTemplate<PrimaryType> extends AbstractGenTemplate {

    public static final String OUTPUT_FILES_PARAM = "outputFileNames";
    public static final VarName MODEL_VAR_NAME = new VarName("_model");
    public static final VarName ITEM_VAR_NAME = new VarName("_item");

    // structures directly contributed by builders during setup
    private PrimarySourceSpec primarySourceSpec;
    protected Map<String, PrerequisiteSpec> prerequisiteSpecs = Maps.newLinkedHashMap();
    private final List<OutputItemSpec> outputItemSpecs = Lists.newArrayList();
    private final List<DynamicGeneratorSpec> dynamicGeneratorSpecs = Lists.newArrayList();
    private final Map<String, NamedSourceSpec> namedSourceSpecs = Maps.newLinkedHashMap();
    protected final Map<String, ParameterSpec> parameterSpecs = Maps.newLinkedHashMap();
    private final List<StaticResourceSpec> staticResourceSpecs = Lists.newArrayList();
    private final Map<String, GenTemplateProperty> properties = Maps.newHashMap();

    // structures extracted from specs either directly or through GenTemplate <
    private Class<PrimaryType> primaryType;
    protected Map<String, File> prerequisiteBindings = Maps.newHashMap();
    private VarName primaryVarName;
    private final Map<String, ISource<?>> namedSourceBindings = Maps.newHashMap();
    private final Map<String, Object> parameterBindings = Maps.newHashMap();

    private final ClassLoader instanceClassLoader;

    public GenTemplate() {
        this.instanceClassLoader = getClass().getClassLoader();
    }

    @Override
    public ISource<?> getPrimarySource() throws GenerationException {
        // There are three possibilities here. First, this GenTemplate may configure a
        // primary source, in which case
        // the context primary source will be set to the configured source. Second, the
        // class neither configures a
        // primary source nor supplies one via an override, in which case this method
        // will return null. Third, the
        // subclass overrides this, in which case its result will have been stored in
        // the context and any primary source
        // specified by configuration will be ignored (as will this method
        // implementation, in all likelihood).
        return context.getPrimarySource();
    }

    @Override
    public List<GenTemplateDependency> getDependencies() throws GenerationException {
        init();
        return super.getDependencies();
    }

    @Override
    public Generator getGenerator() {
        return new Generator();
    }

    protected StaticGenerator<PrimaryType> getStaticGenerator() {
        return new StaticGenerator<PrimaryType>(this, context);
    }

    public Class<?> getPrimaryType() throws GenerationException {
        return TypeUtils.getTypeParamClass(getClass(), GenTemplate.class, 0);
    }

    @Override
    public GenTemplateProperty getProperty(String name) throws GenerationException {
        init();
        return properties.get(name);
    }

    @Override
    public List<String> getAlsoKnownAsIds() throws GenerationException {
        init();
        return super.getAlsoKnownAsIds();
    }

    @Override
    public Map<String, GenTemplateProperty> getProperties() throws GenerationException {
        init();
        return Collections.unmodifiableMap(properties);
    }

    // config-related methods

    // About configure() and related methods
    //
    // The init method calls configure() to define primary source and prerequisites
    // (and other configuration for
    // subclasses); then validate() to make sure the configuration is legal; and
    // then resolve() to resolve configuration
    // into operational values. The bind() method further refines operational values
    // by linking named config items to
    // values supplied by the controlling GenTarget.
    //
    // Builder-related configuration pattern, coded in configure methods, is to call
    // define(xxx().named(...)...), where
    // xxx() delivers a newly constructed XxxBuilder object, and define() calls
    // build on its elaborated value and then
    // validates and stashes the resulting XxxSetup object for later use.
    //
    // The default configure() implementation uses configureByConfig() to obtain
    // configuration from an external config
    // resource.
    //
    // init is called by generate(), getPrimarySource(), and getDependencies()
    // before they attempt to do anything else.
    // All but the first call are no-ops.
    //
    // IMPORTANT NOTES TO SUBCLASSERS
    // Subclasses MUST call super.xxx() in their own xxx overrides, where xxx is any
    // of validate, resolve, bind, or
    // addDependencies. Overrides of configure and configureByConfig SHOULD NOT call
    // the corresponding super method.
    // Subclasses
    // that wish to support configure by configuration should provide a fully
    // self-contained configureByConfig
    // implementation,
    // and those that do not should implement configureByConfig to throw an
    // exception. Only a final implementation class
    // should override configure.

    private boolean inited = false;

    public void init() throws GenerationException {
        if (!inited) {
            @SuppressWarnings("unchecked")
            Class<PrimaryType> validType = (Class<PrimaryType>) getPrimaryType();
            primaryType = validType;
            enableConfigs(true, false);
            configure();
            validate();
            resolve();
            addDependencies();
            // note the next statement must be last performed by init - see comment in
            // #getDependencies above
            inited = true;
        }
    }

    public void initOutputs() throws GenerationException {
        enableConfigs(false, true);
        configure();
    }

    private boolean dependencyConfigEnabled = false;
    private boolean outputConfigEnabled = false;

    private void enableConfigs(boolean dependencyConfigEnabled, boolean outputConfigEnabled) {
        this.dependencyConfigEnabled = dependencyConfigEnabled;
        this.outputConfigEnabled = outputConfigEnabled;
    }

    protected void configure() throws GenerationException {
        @SuppressWarnings("unchecked")
        Class<? extends GenTemplate<?>> genericClass = (Class<? extends GenTemplate<?>>) getClass();
        try {
            configureByConfigFile(GenTargetConfigUtil.loadConfig(genericClass));
        } catch (GenerationException e) {
            throw new GenerationException("Failed to load GenTemplate config file for " + this.getClass().getName(), e);
        }
    }

    protected void configureByConfigFile(GenTemplateConfig config) throws GenerationException {
        configurePrimarySource(config.getPrimarySource());
        configurePrerequisites(config.getPrerequisites());
        configureNamedSources(config.getNamedSources());
        configureOutputItems(config.getOutputItems());
        configureStaticResources(config.getStaticResources());
        configureParameters(config.getParameters());
    }

    private void configurePrimarySource(PrimarySourceConfig config) throws GenerationException {
        define(primarySource().ofType(config.getType()).withValueType(config.getValueType())
                .required(config.isRequired()).withDescription(config.getDescription()));
    }

    private void configurePrerequisites(List<PrerequisiteConfig> prerequisiteConfigs) throws GenerationException {
        for (PrerequisiteConfig config : prerequisiteConfigs) {
            define(prerequisite().named(config.getName()).on(config.getGenTemplateId()).required(config.isRequired())
                    .withDescription(config.getDescription()));
        }
    }

    private void configureNamedSources(List<NamedSourceConfig> namedSourceConfigs) throws GenerationException {
        for (NamedSourceConfig config : namedSourceConfigs) {
            define(namedSource().named(config.getName()).ofType(config.getType()).withValueType(config.getValueType())
                    .required(config.isRequired()).withDescription(config.getDescription()));
        }
    }

    private void configureOutputItems(List<OutputItemConfig> outputItemConfigs) throws GenerationException {
        for (OutputItemConfig config : outputItemConfigs) {
            define(outputItem().named(config.getName()).using(config.getType()).writing(config.getOutputFile())
                    .when(config.getCondition()));
        }
    }

    private void configureStaticResources(List<StaticResourceConfig> staticResourceConfigs) throws GenerationException {
        for (StaticResourceConfig config : staticResourceConfigs) {
            define(staticResource().copying(config.getResourcePath()).to(config.getOutput()));

        }
    }

    private void configureParameters(List<ParameterConfig> parameterConfigs) throws GenerationException {
        for (ParameterConfig config : parameterConfigs) {
            define(parameter().named(config.getName()).required(config.isRequired())
                    .withDefault(config.getDefaultValue()).withDescription(config.getDescription()));
        }
    }

    protected void define(PrimarySourceBuilder builder) throws GenerationException {
        if (dependencyConfigEnabled) {
            primarySourceSpec = builder.build();
        }
    }

    protected PrimarySourceBuilder primarySource() {
        return new PrimarySourceBuilder();
    }

    public void define(PrerequisiteBuilder builder) throws GenerationException {
        if (dependencyConfigEnabled) {
            PrerequisiteSpec spec = builder.build();
            String name = spec.getName();
            if (prerequisiteSpecs.containsKey(name)) {
                throw new GenerationException("Multiple definitions of prerequisite named \"" + name + "\"");
            }
            prerequisiteSpecs.put(name, spec);
        }
    }

    public PrerequisiteBuilder prerequisite() {
        return new PrerequisiteBuilder();
    }

    public void define(NamedSourceBuilder builder) throws GenerationException {
        if (dependencyConfigEnabled) {
            NamedSourceSpec spec = builder.build();
            String name = spec.getName();
            if (namedSourceSpecs.containsKey(name)) {
                throw new GenerationException("Multiple defintions for source named \"" + name + "\"");
            }
            namedSourceSpecs.put(name, spec);
        }
    }

    public NamedSourceBuilder namedSource() {
        return new NamedSourceBuilder();
    }

    public void define(OutputItemBuilder builder) throws GenerationException {
        if (outputConfigEnabled) {
            OutputItemSpec spec = builder.build();
            String name = spec.getName();
            if (name != null) {
                for (OutputItemSpec existing : outputItemSpecs) {
                    if (name.equals(existing.getName())) {
                        throw new GenerationException("Multiple output items defined with name \"" + name + "\"");
                    }
                }
            }
            outputItemSpecs.add(spec);
        }
    }

    public OutputItemBuilder outputItem() {
        return new OutputItemBuilder();
    }

    public DynamicGeneratorBuilder dynamicGenerator() {
        return new DynamicGeneratorBuilder();
    }

    public void define(DynamicGeneratorBuilder builder) throws GenerationException {
        if (outputConfigEnabled) {
            dynamicGeneratorSpecs.add(builder.build());
        }
    }

    public void define(StaticResourceBuilder builder) throws GenerationException {
        if (outputConfigEnabled) {
            staticResourceSpecs.add(builder.build());
        }
    }

    public StaticResourceBuilder staticResource() {
        return new StaticResourceBuilder();
    }

    public void define(ParameterBuilder builder) throws GenerationException {
        if (dependencyConfigEnabled) {
            ParameterSpec spec = builder.build();
            String name = spec.getName();
            if (parameterSpecs.containsKey(name)) {
                throw new GenerationException("Multiple definitions for parameter named \"" + name + "\"");
            }
            parameterSpecs.put(name, spec);
        }
    }

    public ParameterBuilder parameter() {
        return new ParameterBuilder();
    }

    public void define(PropertyBuilder builder) throws GenerationException {
        GenTemplateProperty prop = builder.build();
        properties.put(prop.getName(), prop);
    }

    public PropertyBuilder property() {
        return new PropertyBuilder();
    }

    protected void validate() throws GenerationException {
        if (primarySourceSpec != null) {
            Optional<ISource<?>> instance = primarySourceSpec.getInstance(instanceClassLoader);
            if (instance.isPresent() && !primaryType.isAssignableFrom(instance.get().getValueType())) {
                throw new GenerationException("Primary source produces values of wrong type for GenTemplate: expected "
                        + primaryType + ", got " + instance.get().getValueType());
            }
        }
    }

    protected void resolve() throws GenerationException {
        resolvePrimarySource();
        this.primaryVarName = new VarName(Strings.toFirstLower(getPrimaryType().getSimpleName()));
    }

    /**
     * Determine our primary source and set it in the context.
     * <p>
     * If the extending class overrides #getPrimarySource, we defer to that (at this point our own implementation of
     * #getPrimarySource is guaranteed to return null, a non-null value must come from an override). Otherwise, we
     * resolve a primary source specified through configuration, if any. Either way, we place the final result in the
     * context for all to see.
     * 
     * @throws GenerationException
     */
    protected void resolvePrimarySource() throws GenerationException {
        @SuppressWarnings("unchecked")
        ISource<PrimaryType> primarySource = (ISource<PrimaryType>) getPrimarySource();
        if (primarySource == null && primarySourceSpec != null) {
            Optional<ISource<?>> source = primarySourceSpec.getInstance(instanceClassLoader);
            if (source.isPresent()) {
                @SuppressWarnings("unchecked")
                ISource<PrimaryType> validSource = (ISource<PrimaryType>) source.get();
                primarySource = validSource;
            } else {
                throw new GenerationException("Failed to instantiate primary source: " + primarySourceSpec.toString());
            }
        }
        context.setPrimarySource(primarySource);
    }

    protected void addDependencies() {
        GenTemplateDependencies dependencies = context.getDependencies();
        if (primarySourceSpec != null) {
            dependencies.addPrimarySouceDependency(primarySourceSpec);
        }
        for (PrerequisiteSpec spec : prerequisiteSpecs.values()) {
            dependencies.addGeneratorDependency(spec);
        }
        for (NamedSourceSpec spec : namedSourceSpecs.values()) {
            context.addNamedSourceDependency(spec);
        }
        for (ParameterSpec spec : parameterSpecs.values()) {
            context.addParameterDependency(spec);
        }
    }

    public static String getParamNameFor(String outputItemSpecName) {
        return outputItemSpecName;
    }

    public static class StaticGenerator<PrimaryType> extends AbstractGenTemplate.StaticGenerator {

        protected GenTemplate<PrimaryType> genTemplate;
        private boolean overridesInnerGenerator;

        public StaticGenerator(GenTemplate<PrimaryType> genTemplate, GenTemplateContext context) {
            super(genTemplate, context);
            this.genTemplate = genTemplate;
            this.overridesInnerGenerator = genTemplate.getGenerator().getClass() != GenTemplate.Generator.class;
        }

        @Override
        public GenTemplateTrace generate(GenTarget target, GenTemplateTraces traces) throws GenerationException {
            genTemplate.init();
            genTemplate.initOutputs();
            bind(target);
            context.setTraceBuilder(
                    new GenTemplateTraceBuilder(genTemplate.getId()).withBaseDirectory(target.getBaseDir()));
            context.setTraces(traces);
            context.setControllingGenTarget(target);
            context.setOutputDirectory(target.getOutputDir());
            @SuppressWarnings("unchecked")
            ISource<PrimaryType> primarySource = (ISource<PrimaryType>) context.getPrimarySource();
            PrimaryType primaryValue = primarySource != null ? primarySource.load() : null;
            generate(primaryValue);
            GenTemplateTrace trace = context.getTraceBuilder().build();
            context.setTraceBuilder(null);
            context.setTraces(null);
            context.setControllingGenTarget(null);
            context.setOutputDirectory(null);
            context.setPrimarySource(null);
            return trace;
        }

        public void generate(PrimaryType primaryValue) throws GenerationException {
            if (overridesInnerGenerator) {
                genTemplate.new Generator().generate(primaryValue);
            } else {
                if (genTemplate.outputItemSpecs.isEmpty() && genTemplate.dynamicGeneratorSpecs.isEmpty()
                        && genTemplate.staticResourceSpecs.isEmpty()) {
                    throw new GenerationException(
                            "No output items, dynamic generators, or static resources configured for GenTemplate that does not override #generate(PrimaryType); GenTemplate is incapable of creating generated output.");
                }
                processOutputItems(primaryValue);
                processDynamicGenerators(primaryValue);
                copyStaticResources();
            }
        }

        public void processOutputItems(PrimaryType primaryValue) throws GenerationException {
            runOutputItems(primaryValue);
        }

        private void runOutputItems(PrimaryType primaryValue) throws GenerationException {
            @SuppressWarnings("unchecked")
            ISource<PrimaryType> primarySource = (ISource<PrimaryType>) context.getPrimarySource();
            ILocator<PrimaryType> locator = primarySource.getLocator(primaryValue);
            for (OutputItemSpec oiSpec : genTemplate.outputItemSpecs) {
                IOutputItem<PrimaryType, ?> outputItem = initOutputItem(oiSpec);
                VarName itemVarName = new VarName(Strings.toFirstLower(outputItem.getItemType().getSimpleName()));
                Class<?> inputItemType = outputItem.getItemType();
                String condition = oiSpec.getCondition();
                for (Object inputItem : primarySource.extractByType(primaryValue, inputItemType)) {
                    if (!inputItemType.isAssignableFrom(inputItem.getClass())) {
                        throw new GenerationException("Primary source extracted a value of incompatible type: expected "
                                + inputItemType + " but got " + inputItem.getClass());
                    }
                    String content = null;
                    File outputPath = new OutputPathBuilder<PrimaryType>(genTemplate, context) //
                            .forItem(outputItem) //
                            .withSpec(oiSpec) //
                            .withPrimary(primarySource, primaryValue, genTemplate.primaryVarName) //
                            .withItem(inputItem, itemVarName) //
                            .build();
                    File outputFile = context.getControllingGenTarget().resolveOutputPath(outputPath);
                    context.setCurrentOutputFile(outputFile);
                    if (shouldGenerate(condition, primaryValue, itemVarName, inputItem)) {
                        if (locator != null) {
                            String loc = null;
                            try {
                                loc = locator.locate(inputItem);
                            } catch (GenerationException e) {
                                // could not produce locator, so no locator will appear in trace item
                            }
                            context.addTraceItem("file").withOutputFile(outputFile).withPrimarySourceItem(loc);
                        }
                        content = generate(outputItem, primaryValue, inputItem);
                    }
                    if (content == null) {
                        continue;
                    }
                    // do the right output file processing
                    try {
                        content = postProcessContent(content);
                    } catch (Exception e) {
                        throw new GenerationException("Failed during content postprocessing", e);
                    }
                    writeOutputFile(content, outputFile);
                }
            }
        }

        private IOutputItem<PrimaryType, ?> initOutputItem(OutputItemSpec spec) throws GenerationException {
            Optional<IOutputItem<?, ?>> outputItem = spec.getOutputItemInstance(genTemplate.instanceClassLoader);
            if (outputItem.isPresent()) {
                outputItem.get().init(context);
                @SuppressWarnings("unchecked")
                IOutputItem<PrimaryType, ?> validOutputItem = (IOutputItem<PrimaryType, ?>) outputItem.get();
                return validOutputItem;
            } else {
                throw new GenerationException("Failed to instantiate output item: " + spec.toString());
            }
        }

        private boolean shouldGenerate(String condition, PrimaryType primaryValue, VarName itemVarName, Object item) {
            if (condition != null) {
                return (Boolean) Eval.substEval(condition, genTemplate.parameterBindings, //
                        genTemplate.primaryVarName, primaryValue, //
                        MODEL_VAR_NAME, primaryValue, //
                        itemVarName, item, //
                        ITEM_VAR_NAME, item //
                );
            } else {
                return true;
            }
        }

        private <ItemType> String generate(IOutputItem<PrimaryType, ItemType> outputItem, PrimaryType primaryValue,
                Object item) throws GenerationException {
            @SuppressWarnings("unchecked")
            ItemType validItem = (ItemType) item;
            return outputItem.generate(primaryValue, validItem);
        }

        private void processDynamicGenerators(PrimaryType primaryValue) throws GenerationException {
            for (DynamicGeneratorSpec dgSpec : genTemplate.dynamicGeneratorSpecs) {
                IDynamicGenerator<PrimaryType> generator = initDynamicGenerator(dgSpec);
                generator.generate(primaryValue);
            }
        }

        private IDynamicGenerator<PrimaryType> initDynamicGenerator(DynamicGeneratorSpec spec)
                throws GenerationException {
            Optional<IDynamicGenerator<?>> generator = spec
                    .getDynamicGeneratorInstance(genTemplate.instanceClassLoader);
            if (generator.isPresent()) {
                generator.get().init(context);
                @SuppressWarnings("unchecked")
                IDynamicGenerator<PrimaryType> validGenerator = (IDynamicGenerator<PrimaryType>) generator.get();
                return validGenerator;
            } else {
                throw new GenerationException("Failed to instantiate dynamic generator: " + spec.toString());
            }
        }

        protected void copyStaticResources() throws GenerationException {
            GenTarget target = context.getControllingGenTarget();
            File outputDir = context.getOutputDirectory();
            GenTemplateTraceBuilder traceBuilder = context.getTraceBuilder();
            for (StaticResourceSpec spec : genTemplate.staticResourceSpecs) {
                String resourcePath = spec.getResourcePath();
                if (!resourcePath.startsWith("/")) {
                    resourcePath = genTemplate.getClass().getPackage().getName().replaceAll("\\.", "/") + "/"
                            + resourcePath;
                } else {
                    resourcePath = resourcePath.substring(1);
                }
                File destination = target.resolveOutputPath(spec.getOutput());
                try {
                    List<File> outputFiles = FileUtils.copyResources(context.getExecutingGenTemplate().getClass(),
                            resourcePath, destination);
                    for (File outputFile : outputFiles) {
                        traceBuilder.addStaticResource(resourcePath, outputFile, outputDir);
                    }
                } catch (Exception e) {
                    throw new GenerationException(
                            "Failed copying static resource(s) " + resourcePath + " to " + destination, e);
                }
            }
        }

        protected String postProcessContent(String content) throws Exception {
            return content;
        }

        private void writeOutputFile(String output, File outputFile) throws GenerationException {
            try {
                outputFile.getParentFile().mkdirs();
                Files.write(output, outputFile, Charsets.UTF_8);
            } catch (IOException e) {
                throw new GenerationException("Failed to write generated output file", e);
            }
        }

        // methods for GenTemplates to access GenTarget bindings
        public Object getParameterValue(String name) {
            return genTemplate.parameterBindings.get(name);
        }

        public ISource<?> getNamedSource(String name) {
            return genTemplate.namedSourceBindings.get(name);
        }

        public Object loadNamedSource(String name) throws GenerationException {
            ISource<?> source = getNamedSource(name);
            return source.load();
        }

        protected void bind(GenTarget target) throws GenerationException {
            bindPrimarySource(target);
            bindPrerequisitesToGenTargets(target);
            bindNamedSourcesToInputFiles(target);
            bindParameters(target);
        }

        private void bindPrimarySource(GenTarget target) throws GenerationException {
            ISource<?> primarySource = context.getPrimarySource();
            if (primarySource != null) {
                File sourceFile = target.resolvePrimarySourcePath();
                if (sourceFile != null) {
                    if (!primarySource.hasInputFile() || sourceFile.equals(primarySource.getInputFile())) {
                        primarySource.setInputFile(sourceFile);
                    } else {
                        throw new GenerationException(
                                "GenTarget and GenTemplate provide different primary input files");
                    }
                }
            }
        }

        private void bindPrerequisitesToGenTargets(GenTarget target) throws GenerationException {
            genTemplate.prerequisiteBindings.clear();
            Map<String, GenTargetPrerequisite> targetBindings = target.getPrerequisites();
            List<String> missing = Lists.newArrayList();
            for (PrerequisiteSpec spec : genTemplate.prerequisiteSpecs.values()) {
                String name = spec.getName();
                GenTargetPrerequisite prereqTarget = targetBindings.get(name);
                if (prereqTarget != null) {
                    genTemplate.prerequisiteBindings.put(name, target.resolvePath(prereqTarget.getGenFilePath()));
                } else if (spec.isRequired()) {
                    missing.add(name);
                } else {
                    // optional prereq without gentarget binding - nothing to do
                }
            }
            if (!missing.isEmpty()) {
                throw new GenerationException("GenTarget does not identify required prerequisite GenTargets: "
                        + missing.stream().collect(Collectors.joining(", ")));
            }
        }

        private void bindNamedSourcesToInputFiles(GenTarget target) throws GenerationException {
            Map<String, GenTargetNamedSource> targetBindings = target.getNamedSources();
            List<String> missing = Lists.newArrayList();
            for (NamedSourceSpec spec : genTemplate.namedSourceSpecs.values()) {
                String name = spec.getName();
                Optional<ISource<?>> source = spec.getInstance(genTemplate.instanceClassLoader);
                if (!source.isPresent()) {
                    throw new GenerationException("Failed to instantiate named source: " + spec.toString());
                }
                File path = targetBindings.get(name).getPath();
                if (path != null) {
                    source.get().setInputFile(target.resolvePath(path));
                    genTemplate.namedSourceBindings.put(name, source.get());
                } else if (spec.isRequired()) {
                    missing.add(name);
                } else {
                    // Unbound Optional source will either load a preconfigured source file or
                    // deliver null when asked
                    // to load
                    genTemplate.namedSourceBindings.put(name, source.get());
                }
            }
            if (!missing.isEmpty()) {
                throw new GenerationException("GenTarget does not provide input files for required named sources: "
                        + missing.stream().collect(Collectors.joining(", ")));
            }
        }

        private void bindParameters(GenTarget target) throws GenerationException {
            Map<String, Object> targetBindings = target.getParameters();
            List<String> missing = Lists.newArrayList();
            for (ParameterSpec spec : genTemplate.parameterSpecs.values()) {
                String name = spec.getName();
                Object value = targetBindings.get(name);
                if (value != null) {
                    genTemplate.parameterBindings.put(name, value);
                } else if (spec.isRequired()) {
                    missing.add(name);
                } else {
                    // optional params with no default are explcitly bound to null, which ensures
                    // their names will be
                    // available in eval contexts
                    genTemplate.parameterBindings.put(name, spec.getDefaultValue());
                }
            }
            if (!missing.isEmpty()) {
                throw new GenerationException("GenTarget does not provide values for required parameters: "
                        + missing.stream().collect(Collectors.joining(", ")));
            }
        }
    }

    /**
     * @deprecated GenTemplates should extend {@link StaticGenerator} instead, and override
     *             {@link #getStaticGenerator()}.
     */
    @Deprecated
    public class Generator extends AbstractGenTemplate.Generator {
        private StaticGenerator<PrimaryType> staticGenerator = null;

        public void generate(PrimaryType primaryValue) throws GenerationException {
            // If this is an old-style gentemplate that overrides
            // GenTemplate#getGenerator(),
            // then the Generator instance should override this method to do something,
            // since
            // the default is to do nothing. A new-style gentemplate will instead override
            // GenTemplate#getStaticGenerator, and the static generator class will override
            // StaticGenerator#generate(PrimaryType).
        }

        @Override
        public GenTemplateTrace generate(GenTarget target, GenTemplateTraces traces) throws GenerationException {
            acquireStaticGenerator();
            return staticGenerator.generate(target, traces);
        }

        protected void copyStaticResources() throws GenerationException {
            acquireStaticGenerator();
            staticGenerator.copyStaticResources();
        }

        protected String postProcessContent(String content) throws Exception {
            acquireStaticGenerator();
            return staticGenerator.postProcessContent(content);
        }

        private void acquireStaticGenerator() {
            if (staticGenerator == null) {
                staticGenerator = getStaticGenerator();
            }
        }

    }

    private static class OutputPathBuilder<PrimaryType> {

        private IOutputItem<PrimaryType, ?> outputItem;
        private OutputItemSpec spec;
        private ISource<PrimaryType> primarySource;
        private PrimaryType primaryValue;
        private VarName primaryVarName;
        private Object itemValue;
        private VarName itemVarName;
        private GenTemplate<PrimaryType> genTemplate;
        private GenTemplateContext context;

        public OutputPathBuilder(GenTemplate<PrimaryType> genTemplate, GenTemplateContext context) {
            this.genTemplate = genTemplate;
            this.context = context;
        }

        public OutputPathBuilder<PrimaryType> forItem(IOutputItem<PrimaryType, ?> outputItem) {
            this.outputItem = outputItem;
            return this;

        }

        public OutputPathBuilder<PrimaryType> withSpec(OutputItemSpec spec) {
            this.spec = spec;
            return this;
        }

        public OutputPathBuilder<PrimaryType> withPrimary(ISource<PrimaryType> primarySource, PrimaryType primaryValue,
                VarName primaryVarName) {
            this.primarySource = primarySource;
            this.primaryValue = primaryValue;
            this.primaryVarName = primaryVarName;
            return this;
        }

        public OutputPathBuilder<PrimaryType> withItem(Object itemValue, VarName itemVarName) {
            this.itemValue = itemValue;
            this.itemVarName = itemVarName;
            return this;
        }

        public File build() throws GenerationException {
            return doBuild(itemValue);
        }

        public <ItemType> File doBuild(ItemType itemValue) throws GenerationException {
            @SuppressWarnings("unchecked")
            IOutputItem<PrimaryType, ItemType> safeItem = (IOutputItem<PrimaryType, ItemType>) outputItem;
            File outputFile = safeItem.getOutputFile(primaryValue, itemValue);
            if (outputFile == null) {
                String path = spec.getOutputFile();
                if (path != null) {
                    Object[] otherBindings = { //
                            primaryVarName, primaryValue, //
                            MODEL_VAR_NAME, primaryValue, //
                            itemVarName, itemValue, //
                            ITEM_VAR_NAME, itemValue, //
                            new VarName("primarySource"),
                            new FilePojo(primarySource != null ? primarySource.getInputFile() : null) //
                    };
                    tryResolveAndSetOutputFileNames();
                    try {
                        path = Eval.subst(path, genTemplate.parameterBindings, otherBindings);
                    } catch (Exception e) {
                        throw new GenerationException("Could not execute MVEL expression for output item", e);
                    }
                    outputFile = path != null ? new File(path) : null;
                }
            }
            if (outputFile == null) {
                throw new GenerationException("Could not determine output file for output item");
            }
            return outputFile;
        }

        private void tryResolveAndSetOutputFileNames() {
            if (genTemplate.parameterBindings.containsKey(OUTPUT_FILES_PARAM)
                    && genTemplate.parameterBindings.get(OUTPUT_FILES_PARAM) instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> outputParams = (Map<String, Object>) genTemplate.parameterBindings
                        .get(OUTPUT_FILES_PARAM);
                Object contextOutputFileNames = context.getGenTargetParameters().get(OUTPUT_FILES_PARAM);
                for (String paramName : outputParams.keySet()) {
                    if (paramName instanceof String && outputParams.get(paramName) instanceof String) {
                        String resolvedValue = Eval.subst((String) outputParams.get(paramName),
                                genTemplate.parameterBindings, itemVarName, itemValue);
                        genTemplate.parameterBindings.put(paramName, resolvedValue);
                        // If we got to this point, contextOutputFileNames should be a non-null Map, as
                        // parameterBindings
                        // Checking just in case
                        if (contextOutputFileNames instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> cast = (Map<String, String>) contextOutputFileNames;
                            // Set resolved value to the context's GenTargetParameters, so they will be
                            // available during
                            // GenTemplate execution
                            cast.put(paramName, resolvedValue);
                        }
                    }
                }
            }
        }
    }

    public static class FilePojo {
        private File file;

        public FilePojo(File file) {
            this.file = file;
        }

        public String getFileName() {
            return file != null ? file.getName() : null;
        }

        public String getBaseName() {
            return file != null ? FilenameUtils.getBaseName(file.getPath()) : null;
        }

        public String getExtension() {
            return file != null ? FilenameUtils.getExtension(file.getPath()) : null;
        }

        public String getPath() {
            return file != null ? file.getPath() : null;
        }

        public String getAbsolutePath() {
            return file != null ? file.getAbsolutePath() : null;
        }

        public String getCanonicalPath() {
            try {
                return file != null ? file.getCanonicalPath() : null;
            } catch (IOException e) {
                return null;
            }
        }

        public String getParent() {
            return file != null ? file.getParent() : null;
        }
    }
}
