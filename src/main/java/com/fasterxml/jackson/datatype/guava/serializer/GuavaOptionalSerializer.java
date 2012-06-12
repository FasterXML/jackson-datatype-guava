package com.fasterxml.jackson.datatype.guava.serializer;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.type.JavaType;
import com.google.common.base.Optional;

public final class GuavaOptionalSerializer extends JsonSerializer<Optional<?>> {
    public GuavaOptionalSerializer(JavaType type) {
        super();
    }

    @Override
    public void serialize(Optional<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {
        if(value.isPresent()){
            provider.defaultSerializeValue(value.get(), jgen);
        } else{
            provider.defaultSerializeNull(jgen);
        }
    }
}