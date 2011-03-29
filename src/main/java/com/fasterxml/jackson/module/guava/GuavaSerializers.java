package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.Serializers;
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
}
