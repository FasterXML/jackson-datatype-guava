package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapType;

import com.google.common.collect.ImmutableMap;

public class ImmutableMapDeserializer
    extends GuavaMapDeserializer<ImmutableMap<Object,Object>>
{
    public ImmutableMapDeserializer(MapType type, BeanProperty prop,
            KeyDeserializer keyDeser,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, prop, keyDeser, typeDeser, deser);
    }
    
    @Override
    protected ImmutableMap<Object, Object> _deserializeEntries(JsonParser jp,
            DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        final KeyDeserializer keyDes = _keyDeserializer;
        final JsonDeserializer<?> valueDes = _valueDeserializer;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;

        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder();
        for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
            // Must point to field name now
            String fieldName = jp.getCurrentName();
            Object key = (keyDes == null) ? fieldName : keyDes.deserializeKey(fieldName, ctxt);
            // And then the value...
            JsonToken t = jp.nextToken();
            // 28-Nov-2010, tatu: Should probably support "ignorable properties" in future...
            Object value;            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            builder.put(key, value);
        }
        return builder.build();
    }

}
