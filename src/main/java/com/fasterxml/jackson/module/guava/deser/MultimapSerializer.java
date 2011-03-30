package com.fasterxml.jackson.module.guava.deser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

public class MultimapSerializer extends JsonSerializer<Multimap<Object, Object>> {
    private final BeanProperty property;
    private final JavaType mapType;
    public MultimapSerializer(SerializationConfig config, JavaType type, BeanProperty property) {
        this.property = property;
        JavaType kType = type.containedType(0);
        JavaType vType = type.containedType(1);
        if (kType == null) {
            kType = TypeFactory.fastSimpleType(Object.class);
        }
        if (vType == null) {
            vType = TypeFactory.fastSimpleType(Object.class);
        }
        mapType = TypeFactory.mapType(Map.class, kType,
                TypeFactory.collectionType(Collection.class, vType));
    }

    @Override
    public void serialize(Multimap<Object, Object> value, JsonGenerator jgen, SerializerProvider provider)
    throws IOException, JsonProcessingException {
        // Copy the collections in the Multimap - some of the inner Multimap classes do not serialize properly.
        JsonSerializer<Object> serializer = provider.findTypedValueSerializer(mapType, true, property);
        serializer.serialize(
                ImmutableMap.copyOf(Maps.transformValues(value.asMap(),
                        new Function<Collection<Object>, Collection<Object>>() {
                            public Collection<Object> apply(
                                    Collection<Object> input) {
                                return ImmutableList.copyOf(input);
                            }
                        })), jgen, provider);
    }
}
