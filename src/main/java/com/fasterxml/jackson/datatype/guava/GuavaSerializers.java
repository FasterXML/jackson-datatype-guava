package com.fasterxml.jackson.datatype.guava;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.type.MapLikeType;

import com.fasterxml.jackson.datatype.guava.serializer.MultimapSerializer;
import com.google.common.collect.Multimap;

public class GuavaSerializers extends Serializers.Base
{

    @Override
    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config, MapLikeType type,
            BeanDescription beanDesc, BeanProperty property, JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer, JsonSerializer<Object> elementValueSerializer)
    {

        if (Multimap.class.isAssignableFrom(type.getRawClass()))
        {
            return new MultimapSerializer(config, type, beanDesc, property, keySerializer,
                    elementTypeSerializer, elementValueSerializer);
        }

        return null;
    }
}
