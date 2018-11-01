package com.reprezen.genflow.rapidml.jsonschema;

import org.eclipse.xtext.junit4.GlobalRegistries;
import org.eclipse.xtext.junit4.GlobalRegistries.GlobalStateMemento;
import org.eclipse.xtext.junit4.IInjectorProvider;
import org.eclipse.xtext.junit4.IRegistryConfigurator;

import com.google.inject.Injector;
import com.reprezen.rapidml.xtext.RapidMLStandaloneSetup;

public class RapidMLInjectorProvider implements IInjectorProvider, IRegistryConfigurator {

    protected GlobalStateMemento stateBeforeInjectorCreation;
    protected GlobalStateMemento stateAfterInjectorCreation;
    protected Injector injector;

    static {
        GlobalRegistries.initializeDefaults();
    }

    @Override
    public Injector getInjector() {
        if (injector == null) {
            stateBeforeInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();
            this.injector = internalCreateInjector();
            stateAfterInjectorCreation = GlobalRegistries.makeCopyOfGlobalState();
        }
        return injector;
    }

    protected Injector internalCreateInjector() {
        return new RapidMLStandaloneSetup().createInjectorAndDoEMFRegistration();
    }

    @Override
    public void restoreRegistry() {
        stateBeforeInjectorCreation.restoreGlobalState();
    }

    @Override
    public void setupRegistry() {
        getInjector();
        stateAfterInjectorCreation.restoreGlobalState();
    }
}
