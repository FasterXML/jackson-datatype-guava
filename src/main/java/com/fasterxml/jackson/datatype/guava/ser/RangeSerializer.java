package com.fasterxml.jackson.datatype.guava.ser;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Range;

import java.io.IOException;

/**
 * Jackson serializer for a Guava {@link Range}.
 */
public class RangeSerializer extends StdSerializer<Range> {

    public RangeSerializer(JavaType type) {
        super(type);
    }

    @Override
    public void serialize(Range value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonGenerationException {

        jgen.writeStartObject();

        if (value.hasLowerBound()) {
            provider.defaultSerializeField("lowerEndpoint", value.lowerEndpoint(), jgen);
            provider.defaultSerializeField("lowerBoundType", value.lowerBoundType(), jgen);
        }

        if (value.hasUpperBound()) {
            provider.defaultSerializeField("upperEndpoint", value.upperEndpoint(), jgen);
            provider.defaultSerializeField("upperBoundType", value.upperBoundType(), jgen);
        }

        jgen.writeEndObject();

    }

}
