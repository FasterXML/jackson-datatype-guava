package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.google.common.base.Optional;

public final class GuavaOptionalDeserializer extends StdDeserializer<Optional<?>> {
    private final JavaType _referenceType;

    public GuavaOptionalDeserializer(JavaType valueType) {
        super(valueType);
        _referenceType = valueType.containedType(0);
    }

    @Override
    public Optional<?> getNullValue() {
        return Optional.absent();
    }

    @Override
    public Optional<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Object reference = ctxt.findRootValueDeserializer(_referenceType).deserialize(jp, ctxt);
        return Optional.of(reference);
    }
    
    @Override
    public Optional<?> deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
            throws IOException, JsonProcessingException {
        return deserialize(jp, ctxt);
    }
}