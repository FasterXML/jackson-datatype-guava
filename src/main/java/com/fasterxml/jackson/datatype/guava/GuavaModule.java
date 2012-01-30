package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.*;

public class GuavaModule extends Module // can't use just SimpleModule, due to generic types
{
    private final String NAME = "GuavaModule";
    
    public GuavaModule() {
        super();
    }

    @Override public String getModuleName() { return NAME; }
    @Override public Version version() { return ModuleVersion.instance.version(); }
    
    @Override
    public void setupModule(SetupContext context)
    {
        context.addDeserializers(new GuavaDeserializers());
    }

}