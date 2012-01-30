package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;

public abstract class GuavaMapDeserializer<T>
    extends JsonDeserializer<T>
    implements ResolvableDeserializer
{
    protected final MapType _mapType;

    protected final BeanProperty _property;
    
    /**
     * Key deserializer used, if not null. If null, String from JSON
     * content is used as is.
     */
    protected KeyDeserializer _keyDeserializer;

    /**
     * Value deserializer.
     */
    protected JsonDeserializer<?> _valueDeserializer;

    /**
     * If value instances have polymorphic type information, this
     * is the type deserializer that can handle it
     */
    protected final TypeDeserializer _typeDeserializerForValue;

    protected GuavaMapDeserializer(MapType type, BeanProperty prop,
            KeyDeserializer keyDeser,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        _mapType = type;
        _property = prop;
        _keyDeserializer = keyDeser;
        _typeDeserializerForValue = typeDeser;
        _valueDeserializer = deser;
    }

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
    public void resolve(DeserializationContext ctxt) throws JsonMappingException
    {
        if (_keyDeserializer == null) {
            _keyDeserializer = ctxt.findKeyDeserializer(_mapType.getKeyType(), _property);
        }
        if (_valueDeserializer == null) {
            _valueDeserializer = ctxt.findValueDeserializer(_mapType.getContentType(), _property);
        }
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
        // Ok: must point to START_OBJECT or FIELD_NAME
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) { // If START_OBJECT, move to next; may also be END_OBJECT
            t = jp.nextToken();
            if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
                throw ctxt.mappingException(_mapType.getRawClass());
            }
        } else if (t != JsonToken.FIELD_NAME) {
            throw ctxt.mappingException(_mapType.getRawClass());
        }
        return _deserializeEntries(jp, ctxt);
    }

    /*
    /**********************************************************************
    /* Abstract methods for impl classes
    /**********************************************************************
     */

    protected abstract T _deserializeEntries(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException;
    
    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

}
