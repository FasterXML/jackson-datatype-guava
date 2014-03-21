package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MultimapSerializer
    extends ContainerSerializer<Multimap<?, ?>>
    implements ContextualSerializer
{
    private final MapLikeType _type;
    private final BeanProperty _property;
    private final JsonSerializer<Object> _keySerializer;
    private final TypeSerializer _valueTypeSerializer;
    private final JsonSerializer<Object> _valueSerializer;

    public MultimapSerializer(SerializationConfig config,
            MapLikeType type,
            BeanDescription beanDesc,
            JsonSerializer<Object> keySerializer,
            TypeSerializer valueTypeSerializer,
            JsonSerializer<Object> valueSerializer)
    {
        super(type.getRawClass(), false);
        _type = type;
        _property = null;
        _keySerializer = keySerializer;
        _valueTypeSerializer = valueTypeSerializer;
        _valueSerializer = valueSerializer;
    }

    @SuppressWarnings("unchecked")
    protected MultimapSerializer(MultimapSerializer src, BeanProperty property,
                JsonSerializer<?> keySerializer,
                TypeSerializer valueTypeSerializer, JsonSerializer<?> valueSerializer)
    {
        super(src);
        _type = src._type;
        _property = property;
        _keySerializer = (JsonSerializer<Object>) keySerializer;
        _valueTypeSerializer = valueTypeSerializer;
        _valueSerializer = (JsonSerializer<Object>) valueSerializer;
    }

    protected MultimapSerializer withResolved(BeanProperty property,
            JsonSerializer<?> keySer,
            TypeSerializer vts, JsonSerializer<?> valueSer)
    {
        return new MultimapSerializer(this, property, keySer, vts, valueSer);
    }

    @Override
    protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSer) {
        return new MultimapSerializer(this, _property, _keySerializer,
                typeSer, _valueSerializer);
    }
    
    /*
    /**********************************************************
    /* Post-processing (contextualization)
    /**********************************************************
     */

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> valueSer = _valueSerializer;
        if (valueSer == null) { // if type is final, can actually resolve:
            JavaType valueType = _type.getContentType();
            if (valueType.isFinal()) {
                valueSer = provider.findValueSerializer(valueType, property);
            }
        } else if (valueSer instanceof ContextualSerializer) {
            valueSer = ((ContextualSerializer) valueSer).createContextual(provider, property);
        }
        JsonSerializer<?> keySer = _keySerializer;
        if (keySer == null) {
            keySer = provider.findKeySerializer(_type.getKeyType(), property);
        } else if (keySer instanceof ContextualSerializer) {
            keySer = ((ContextualSerializer) keySer).createContextual(provider, property);
        }
        // finally, TypeSerializers may need contextualization as well
        TypeSerializer typeSer = _valueTypeSerializer;
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }
        return withResolved(property, keySer, typeSer, valueSer);
    }

    /*
    /**********************************************************
    /* Accessors for ContainerSerializer
    /**********************************************************
     */
    
    @Override
    public JsonSerializer<?> getContentSerializer() {
        return _valueSerializer;
    }

    @Override
    public JavaType getContentType() {
        return _type.getContentType();
    }

    @Override
    public boolean hasSingleElement(Multimap<?,?> map) {
        return map.size() == 1;
    }

    @Override
    public boolean isEmpty(Multimap<?,?> map) {
        return map.isEmpty();
    }
    
    /*
    /**********************************************************
    /* Post-processing (contextualization)
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
            if (_keySerializer != null) {
                _keySerializer.serialize(e.getKey(), jgen, provider);
            } else {
                provider.findKeySerializer(provider.constructType(String.class), _property)
                    .serialize(e.getKey(), jgen, provider);
            }
            if (_valueSerializer != null) {
                // note: value is a List, but generic type is for contents... so:
                jgen.writeStartArray();
                for (Object vv : e.getValue()) {
                    _valueSerializer.serialize(vv, jgen, provider);
                }
                jgen.writeEndArray();
            } else {
                provider.defaultSerializeValue(Lists.newArrayList(e.getValue()), jgen);
            }
        }
    }
}
