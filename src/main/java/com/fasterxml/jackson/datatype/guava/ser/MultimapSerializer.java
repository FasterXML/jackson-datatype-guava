package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MultimapSerializer
    extends JsonSerializer<Multimap<?, ?>>
    implements ContextualSerializer
{
    private final MapLikeType type;
    private final BeanProperty property;
    private final JsonSerializer<Object> keySerializer;
    private final TypeSerializer valueTypeSerializer;
    private final JsonSerializer<Object> valueSerializer;

    public MultimapSerializer(SerializationConfig config,
            MapLikeType type,
            BeanDescription beanDesc,
            JsonSerializer<Object> keySerializer,
            TypeSerializer valueTypeSerializer,
            JsonSerializer<Object> valueSerializer)
    {
        this.type = type;
        this.property = null;
        this.keySerializer = keySerializer;
        this.valueTypeSerializer = valueTypeSerializer;
        this.valueSerializer = valueSerializer;
    }

    @SuppressWarnings("unchecked")
    protected MultimapSerializer(MultimapSerializer src, BeanProperty property,
                JsonSerializer<?> keySerializer,
                TypeSerializer valueTypeSerializer, JsonSerializer<?> valueSerializer)
    {
        this.type = src.type;
        this.property = property;
        this.keySerializer = (JsonSerializer<Object>) keySerializer;
        this.valueTypeSerializer = valueTypeSerializer;
        this.valueSerializer = (JsonSerializer<Object>) valueSerializer;
    }
            
    
    protected MultimapSerializer withResolved(BeanProperty property,
            JsonSerializer<?> keySerializer,
            TypeSerializer valueTypeSerializer, JsonSerializer<?> valueSerializer)
    {
        return new MultimapSerializer(this, property, keySerializer,
                valueTypeSerializer, valueSerializer);
    }
    
    /*
    /**********************************************************
    /* Post-processing (contextualization)
    /**********************************************************
     */

    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> valueSer = valueSerializer;
        if (valueSer == null) { // if type is final, can actually resolve:
            JavaType valueType = type.getContentType();
            if (valueType.isFinal()) {
                valueSer = provider.findValueSerializer(valueType, property);
            }
        } else if (valueSer instanceof ContextualSerializer) {
            valueSer = ((ContextualSerializer) valueSer).createContextual(provider, property);
        }
        JsonSerializer<?> keySer = keySerializer;
        if (keySer == null) {
            keySer = provider.findKeySerializer(type.getKeyType(), property);
        } else if (keySer instanceof ContextualSerializer) {
            keySer = ((ContextualSerializer) keySer).createContextual(provider, property);
        }
        // finally, TypeSerializers may need contextualization as well
        TypeSerializer typeSer = valueTypeSerializer;
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }
        return withResolved(property, keySer, typeSer, valueSer);
    }

    /*
    /**********************************************************
    /* JsonSerializer implementation
    /**********************************************************
     */
    
    @Override
    public void serialize(Multimap<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            serializeFields(value, jgen, provider);
        }        
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(Multimap<?,?> value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException, JsonGenerationException
    {
        typeSer.writeTypePrefixForObject(value, jgen);
        serializeFields(value, jgen, provider);
        typeSer.writeTypeSuffixForObject(value, jgen);
    }

    private final void serializeFields(Multimap<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        for (Entry<?, ? extends Collection<?>> e : value.asMap().entrySet()) {
            if (keySerializer != null) {
                keySerializer.serialize(e.getKey(), jgen, provider);
            } else {
                provider.findKeySerializer(provider.constructType(String.class), property)
                    .serialize(e.getKey(), jgen, provider);
            }
            if (valueSerializer != null) {
                // note: value is a List, but generic type is for contents... so:
                jgen.writeStartArray();
                for (Object vv : e.getValue()) {
                    valueSerializer.serialize(vv, jgen, provider);
                }
                jgen.writeEndArray();
            } else {
                provider.defaultSerializeValue(Lists.newArrayList(e.getValue()), jgen);
            }
        }
    }
}
