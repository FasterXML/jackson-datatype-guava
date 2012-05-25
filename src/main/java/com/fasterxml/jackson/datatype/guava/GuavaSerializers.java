package com.fasterxml.jackson.datatype.guava;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.fasterxml.jackson.datatype.guava.ser.GuavaOptionalSerializer;
import com.fasterxml.jackson.datatype.guava.ser.MultimapSerializer;

import com.google.common.base.Optional;
import com.google.common.collect.Multimap;

public class GuavaSerializers extends Serializers.Base
{
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
        Class<?> raw = type.getRawClass();
        if(Optional.class.isAssignableFrom(raw)){
            return new GuavaOptionalSerializer(type);
        }
        return super.findSerializer(config, type, beanDesc);
    }
    @Override
    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config,
            MapLikeType type, BeanDescription beanDesc, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {
        if (Multimap.class.isAssignableFrom(type.getRawClass())) {
            return new MultimapSerializer(config, type, beanDesc, keySerializer,
                    elementTypeSerializer, elementValueSerializer);
        }
        return null;
    }
}
