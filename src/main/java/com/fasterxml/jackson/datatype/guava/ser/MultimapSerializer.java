package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MultimapSerializer extends JsonSerializer<Multimap<?, ?>> {

    private final BeanProperty property;
    private final JsonSerializer<Object> keySerializer;
    private final TypeSerializer elementTypeSerializer;
    private final JsonSerializer<Object> elementValueSerializer;

    public MultimapSerializer(
            SerializationConfig config,
            MapLikeType type,
            BeanDescription beanDesc,
            BeanProperty property,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer)
    {
        this.property = property;
        this.keySerializer = keySerializer;
        this.elementTypeSerializer = elementTypeSerializer;
        this.elementValueSerializer = elementValueSerializer;
    }

    @Override
    public void serialize(Multimap<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        if (elementTypeSerializer == null)
        {
            jgen.writeStartObject();
        }
        else
        {
            elementTypeSerializer.writeTypePrefixForObject(value, jgen);
        }

        for (Entry<?, ? extends Collection<?>> e : value.asMap().entrySet()) {
            if (keySerializer != null)
            {
                keySerializer.serialize(e.getKey(), jgen, provider);
            }
            else
            {
                provider.findKeySerializer(provider.constructType(String.class), property).serialize(e.getKey(), jgen, provider);
            }
            if (elementValueSerializer != null)
            {
                elementValueSerializer.serialize(e.getValue(), jgen, provider);
            }
            else
            {
                provider.defaultSerializeValue(Lists.newArrayList(e.getValue()), jgen);
            }
        }

        if (elementTypeSerializer == null)
        {
            jgen.writeEndObject();
        }
        else
        {
            elementTypeSerializer.writeTypeSuffixForObject(value, jgen);
        }
    }
}
