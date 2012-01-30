package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

import com.google.common.collect.HashMultiset;

public class HashMultisetDeserializer  extends GuavaCollectionDeserializer<HashMultiset<Object>>
{
    public HashMultisetDeserializer(CollectionType type, BeanProperty prop,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, prop, typeDeser, deser);
    }

    @Override
    protected HashMultiset<Object> _deserializeContents(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonDeserializer<?> valueDes = _valueDeserializer;
        JsonToken t;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
        HashMultiset<Object> set = HashMultiset.create();

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;
            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            set.add(value);
        }
        return set;
    }
}
