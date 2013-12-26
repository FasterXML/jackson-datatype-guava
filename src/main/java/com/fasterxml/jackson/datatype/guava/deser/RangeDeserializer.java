package com.fasterxml.jackson.datatype.guava.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import java.io.IOException;

/**
 * Jackson deserializer for a Guava {@link Range}.
 */
public class RangeDeserializer extends JsonDeserializer<Range> {

    @Override
    public Range deserialize(JsonParser parser, DeserializationContext context)
            throws IOException, JsonProcessingException {
        expect(parser, JsonToken.START_OBJECT);

        Comparable lowerEndpoint = null;
        Comparable upperEndpoint = null;
        BoundType lowerBoundType = null;
        BoundType upperBoundType = null;

        while ((parser.nextToken() != null) && (parser.getCurrentToken() != JsonToken.END_OBJECT)) {
            expect(parser, JsonToken.FIELD_NAME);
            String fieldName = parser.getCurrentName();
            try {
                if (fieldName.equals("lowerEndpoint")) {
                    Preconditions.checkState(lowerEndpoint == null, "'lowerEndpoint' field included multiple times.");
                    parser.nextToken();
                    lowerEndpoint = deserializeEndpoint(parser, context);
                } else if (fieldName.equals("upperEndpoint")) {
                    Preconditions.checkState(upperEndpoint == null, "'upperEndpoint' field included multiple times.");
                    parser.nextToken();
                    upperEndpoint = deserializeEndpoint(parser, context);
                } else if (fieldName.equals("lowerBoundType")) {
                    Preconditions.checkState(lowerBoundType == null, "'lowerBoundType' field included multiple times.");
                    parser.nextToken();
                    lowerBoundType = deserializeBoundType(parser);
                } else if (fieldName.equals("upperBoundType")) {
                    Preconditions.checkState(upperBoundType == null, "'upperBoundType' field included multiple times.");
                    parser.nextToken();
                    upperBoundType = deserializeBoundType(parser);
                } else {
                    throw new JsonMappingException("Unexpected field: " + fieldName);
                }
            } catch (IllegalStateException e) {
                throw new JsonMappingException(e.getMessage());
            }
        }

        Range range;
        try {
            if ((lowerEndpoint != null) && (upperEndpoint != null)) {
                Preconditions.checkState(lowerEndpoint.getClass() == upperEndpoint.getClass(),
                                         "Endpoint types are not the same - 'lowerEndpoint' deserialized to [%s], and 'upperEndpoint' deserialized to [%s].",
                                         lowerEndpoint.getClass().getName(),
                                         upperEndpoint.getClass().getName());
                Preconditions.checkState(lowerBoundType != null, "'lowerEndpoint' field found, but not 'lowerBoundType'");
                Preconditions.checkState(upperBoundType != null, "'upperEndpoint' field found, but not 'upperBoundType'");
                range = Range.range(lowerEndpoint, lowerBoundType, upperEndpoint, upperBoundType);
            } else if (lowerEndpoint != null) {
                Preconditions.checkState(lowerBoundType != null, "'lowerEndpoint' field found, but not 'lowerBoundType'");
                range = Range.downTo(lowerEndpoint, lowerBoundType);
            } else if (upperEndpoint != null) {
                Preconditions.checkState(upperBoundType != null, "'upperEndpoint' field found, but not 'upperBoundType'");
                range = Range.upTo(upperEndpoint, upperBoundType);
            } else {
                range = Range.all();
            }
        } catch (IllegalStateException e) {
            throw new JsonMappingException(e.getMessage());
        }
        return range;
    }

    private BoundType deserializeBoundType(JsonParser parser) throws IOException {
        expect(parser, JsonToken.VALUE_STRING);
        String name = parser.getText();
        try {
            return BoundType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("[" + name + "] is not a valid BoundType name.");
        }
    }

    private Comparable deserializeEndpoint(JsonParser parser, DeserializationContext context) throws IOException {
        Object obj = new UntypedObjectDeserializer().deserialize(parser, context);
        Preconditions.checkState(obj instanceof Comparable,
                                 "Field [%s] deserialized to [%s], which does not implement Comparable.",
                                 parser.getCurrentName(), obj.getClass().getName());
        //noinspection ConstantConditions
        return (Comparable) obj;
    }

    private void expect(JsonParser jp, JsonToken token) throws IOException {
        if (jp.getCurrentToken() != token) {
            throw new JsonMappingException("Expecting " + token + ", found " + jp.getCurrentToken(), jp.getCurrentLocation());
        }
    }

}
