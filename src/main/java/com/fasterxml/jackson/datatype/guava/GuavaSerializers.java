package com.fasterxml.jackson.datatype.guava;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.fasterxml.jackson.datatype.guava.serializer.MultimapSerializer;

import com.google.common.collect.Multimap;

public class GuavaSerializers extends Serializers.Base
{
    @Override
    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type,
            BeanDescription beanDesc, BeanProperty property, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        if (Multimap.class.isAssignableFrom(type.getRawClass())) {
            return new MultimapSerializer(config, type, beanDesc, property, keySerializer,
                    elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }
}
