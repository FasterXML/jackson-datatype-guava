package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.Serializers;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionLikeType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

import com.fasterxml.jackson.module.guava.deser.MultimapSerializer;
import com.google.common.collect.Multimap;

/**
 * Installed by the {@link GuavaModule}; provides {@link JsonSerializer}
 * implementations for Guava classes.  Right now only covers Multimaps.
 */
public class GuavaSerializers implements Serializers {
    public JsonSerializer<?> findSerializer(SerializationConfig config,
            JavaType type, BeanDescription beanDesc, BeanProperty property) {
        Class<?> raw = type.getRawClass();
        if (Multimap.class.isAssignableFrom(raw)) {
            return new MultimapSerializer(config, type, property);
        }
        return null;
    }

    // XXX: these appeared in 1.8; do we have to do anything here?

    public JsonSerializer<?> findArraySerializer(SerializationConfig config,
            ArrayType type, BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findCollectionSerializer(
            SerializationConfig config, CollectionType type,
            BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findCollectionLikeSerializer(
            SerializationConfig config, CollectionLikeType type,
            BeanDescription beanDesc, BeanProperty property,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findMapSerializer(SerializationConfig config,
            MapType type, BeanDescription beanDesc, BeanProperty property,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) {
        return null;
    }

    public JsonSerializer<?> findMapLikeSerializer(SerializationConfig config,
            MapLikeType type, BeanDescription beanDesc, BeanProperty property,
            JsonSerializer<Object> keySerializer,
            TypeSerializer elementTypeSerializer,
            JsonSerializer<Object> elementValueSerializer) {
        return null;
    }
}
