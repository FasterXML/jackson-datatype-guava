package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.fasterxml.jackson.datatype.guava.deser.util.RangeFactory;

/**
 * Jackson deserializer for a Guava {@link Range}.
 *<p>
 * TODO: I think it would make sense to reimplement this deserializer to
 * use Delegating Deserializer, using a POJO as an intermediate form (properties
 * could be of type {@link java.lang.Object})
 * This would also also simplify the implementation a bit.
 */
public class RangeDeserializer
    extends StdDeserializer<Range<?>>
    implements ContextualDeserializer
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _rangeType;

    protected final JsonDeserializer<Object> _endpointDeserializer;

    private BoundType _defaultBoundType;

    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    /**
     * @deprecated Since 2.7
     */
    @Deprecated // since 2.7
    public RangeDeserializer(JavaType rangeType) {
        this(null, rangeType);
    }
    
    public RangeDeserializer(BoundType defaultBoundType, JavaType rangeType) {
        this(rangeType, null);
        _defaultBoundType = defaultBoundType;
    }

    @SuppressWarnings("unchecked")
    public RangeDeserializer(JavaType rangeType, JsonDeserializer<?> endpointDeser)
    {
        super(rangeType);
        _rangeType = rangeType;
        _endpointDeserializer = (JsonDeserializer<Object>) endpointDeser;
    }

    @SuppressWarnings("unchecked")
    public RangeDeserializer(JavaType rangeType, JsonDeserializer<?> endpointDeser, BoundType defaultBoundType)
    {
        super(rangeType);
        _rangeType = rangeType;
        _endpointDeserializer = (JsonDeserializer<Object>) endpointDeser;
        _defaultBoundType = defaultBoundType;
    }

    @Override
    public JavaType getValueType() { return _rangeType; }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        if (_endpointDeserializer == null) {
            JavaType endpointType = _rangeType.containedType(0);
            if (endpointType == null) { // should this ever occur?
                endpointType = TypeFactory.unknownType();
            }
            JsonDeserializer<Object> deser = ctxt.findContextualValueDeserializer(endpointType, property);
            return new RangeDeserializer(_rangeType, deser, _defaultBoundType);
        }
        return this;
    }

    /*
    /**********************************************************
    /* Actual deserialization
    /**********************************************************
     */
    
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException
    {
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }

    @Override
    public Range<?> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException
    {
        // NOTE: either START_OBJECT _or_ FIELD_NAME fine; latter for polymorphic cases
        JsonToken t = parser.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            t = parser.nextToken();
        }

        Comparable<?> lowerEndpoint = null;
        Comparable<?> upperEndpoint = null;
        BoundType lowerBoundType = _defaultBoundType;
        BoundType upperBoundType = _defaultBoundType;

        for (; t != JsonToken.END_OBJECT; t = parser.nextToken()) {
            expect(parser, JsonToken.FIELD_NAME, t);
            String fieldName = parser.getCurrentName();
            try {
                if (fieldName.equals("lowerEndpoint")) {
                    parser.nextToken();
                    lowerEndpoint = deserializeEndpoint(parser, context);
                } else if (fieldName.equals("upperEndpoint")) {
                    parser.nextToken();
                    upperEndpoint = deserializeEndpoint(parser, context);
                } else if (fieldName.equals("lowerBoundType")) {
                    parser.nextToken();
                    lowerBoundType = deserializeBoundType(parser);
                } else if (fieldName.equals("upperBoundType")) {
                    parser.nextToken();
                    upperBoundType = deserializeBoundType(parser);
                } else {
                    throw context.mappingException("Unexpected Range field: " + fieldName);
                }
            } catch (IllegalStateException e) {
                throw JsonMappingException.from(parser, e.getMessage());
            }
        }
        try {
            if ((lowerEndpoint != null) && (upperEndpoint != null)) {
                Preconditions.checkState(lowerEndpoint.getClass() == upperEndpoint.getClass(),
                                         "Endpoint types are not the same - 'lowerEndpoint' deserialized to [%s], and 'upperEndpoint' deserialized to [%s].",
                                         lowerEndpoint.getClass().getName(),
                                         upperEndpoint.getClass().getName());
                Preconditions.checkState(lowerBoundType != null, "'lowerEndpoint' field found, but not 'lowerBoundType'");
                Preconditions.checkState(upperBoundType != null, "'upperEndpoint' field found, but not 'upperBoundType'");
                return RangeFactory.range(lowerEndpoint, lowerBoundType, upperEndpoint, upperBoundType);
            }
            if (lowerEndpoint != null) {
                Preconditions.checkState(lowerBoundType != null, "'lowerEndpoint' field found, but not 'lowerBoundType'");
                return RangeFactory.downTo(lowerEndpoint, lowerBoundType);
            }
            if (upperEndpoint != null) {
                Preconditions.checkState(upperBoundType != null, "'upperEndpoint' field found, but not 'upperBoundType'");
                return RangeFactory.upTo(upperEndpoint, upperBoundType);
            }
            return RangeFactory.all();
        } catch (IllegalStateException e) {
            throw JsonMappingException.from(parser, e.getMessage());
        }
    }

    private BoundType deserializeBoundType(JsonParser parser) throws IOException
    {
        expect(parser, JsonToken.VALUE_STRING, parser.getCurrentToken());
        String name = parser.getText();
        try {
            return BoundType.valueOf(name);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("[" + name + "] is not a valid BoundType name.");
        }
    }

    private Comparable<?> deserializeEndpoint(JsonParser parser, DeserializationContext context) throws IOException
    {
        Object obj = _endpointDeserializer.deserialize(parser, context);
        if (!(obj instanceof Comparable)) {
            throw context.mappingException(String.format(
                                 "Field [%s] deserialized to [%s], which does not implement Comparable.",
                                 parser.getCurrentName(), obj.getClass().getName()));
        }
        return (Comparable<?>) obj;
    }

    private void expect(JsonParser p, JsonToken expected, JsonToken actual) throws JsonMappingException
    {
        if (actual != expected) {
            throw JsonMappingException.from(p, "Expecting " + expected + ", found " + actual);
        }
    }
}
