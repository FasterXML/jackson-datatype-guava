package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.base.Optional;

public final class GuavaOptionalSerializer extends StdSerializer<Optional<?>> {
    public GuavaOptionalSerializer(JavaType type) {
        super(type);
    }

    @Override
    public void serialize(Optional<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {
        if(value.isPresent()){
            provider.defaultSerializeValue(value.get(), jgen);
        } else{
            provider.defaultSerializeNull(jgen);
        }
    }
    
    @Override
    public void serializeWithType(Optional<?> value,
                                  JsonGenerator jgen,
                                  SerializerProvider provider,
                                  TypeSerializer typeSer) throws IOException, JsonProcessingException {
        serialize(value, jgen, provider);
    }
}