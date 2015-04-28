package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.Version;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.guava.ser.GuavaBeanSerializerModifier;

public class GuavaModule extends Module // can't use just SimpleModule, due to generic types
{
    private final String NAME = "GuavaModule";
    
    public GuavaModule() {
        super();
    }

    @Override public String getModuleName() { return NAME; }
    @Override public Version version() { return PackageVersion.VERSION; }
    
    @Override
    public void setupModule(SetupContext context)
    {
        context.addDeserializers(new GuavaDeserializers());
        context.addSerializers(new GuavaSerializers());
        context.addTypeModifier(new GuavaTypeModifier());
        context.addBeanSerializerModifier(new GuavaBeanSerializerModifier());
    }

    @Override
    public int hashCode()
    {
        return NAME.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
}
