package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

public abstract class GuavaCollectionDeserializer<T>
    extends StdDeserializer<T>
    implements ContextualDeserializer
{
    protected final CollectionType _containerType;
    
    /**
     * Deserializer used for values contained in collection being deserialized;
     * either assigned on constructor, or during resolve().
     */
    protected final JsonDeserializer<?> _valueDeserializer;

    /**
     * If value instances have polymorphic type information, this
     * is the type deserializer that can deserialize required type
     * information
     */
    protected final TypeDeserializer _typeDeserializerForValue;
    
    protected GuavaCollectionDeserializer(CollectionType type,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type);
        _containerType = type;
        _typeDeserializerForValue = typeDeser;
        _valueDeserializer = deser;
    }

    /**
     * Overridable fluent factory method used for creating contextual
     * instances.
     */
    public abstract GuavaCollectionDeserializer<T> withResolved(TypeDeserializer typeDeser,
            JsonDeserializer<?> valueDeser);
    
    /*
    /**********************************************************
    /* Validation, post-processing (ResolvableDeserializer)
    /**********************************************************
     */

    /**
     * Method called to finalize setup of this deserializer,
     * after deserializer itself has been registered. This
     * is needed to handle recursive and transitive dependencies.
     */
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        JsonDeserializer<?> deser = _valueDeserializer;
        TypeDeserializer typeDeser = _typeDeserializerForValue;
        // Do we need any contextualization?
        if ((deser != null) && (typeDeser == null)) { // nope
            return this;
        }
        if (deser == null) {
            deser = ctxt.findValueDeserializer(_containerType.getContentType(), property);
        }
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }
        return withResolved(typeDeser, deser);
    }

    /*
    /**********************************************************
    /* Deserialization interface
    /**********************************************************
     */
    
    /**
     * Base implementation that does not assume specific type
     * inclusion mechanism. Sub-classes are expected to override
     * this method if they are to handle type information.
     */
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }
    
    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        // Ok: must point to START_ARRAY
        if (jp.getCurrentToken() != JsonToken.START_ARRAY) {
            throw ctxt.mappingException(_containerType.getRawClass());
        }
        return _deserializeContents(jp, ctxt);
    }

    /*
    /**********************************************************************
    /* Abstract methods for impl classes
    /**********************************************************************
     */

    protected abstract T _deserializeContents(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException;
}
