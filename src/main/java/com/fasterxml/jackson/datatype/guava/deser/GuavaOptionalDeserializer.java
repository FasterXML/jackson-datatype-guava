package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.deser.std.StdDeserializer;
import org.codehaus.jackson.type.JavaType;
import com.google.common.base.Optional;

public final class GuavaOptionalDeserializer extends StdDeserializer<Optional<?>> {
    private final JsonDeserializer<?> _referenceTypeDeserializer;

    public GuavaOptionalDeserializer(JavaType valueType, JsonDeserializer<?> referenceTypeDeserializer) {
        super(valueType);
        _referenceTypeDeserializer = referenceTypeDeserializer;
    }

    @Override
    public Optional<?> getNullValue() {
        return Optional.absent();
    }

    @Override
    public Optional<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException {
        Object reference = _referenceTypeDeserializer.deserialize(jp, ctxt);
        return Optional.of(reference);
    }
}