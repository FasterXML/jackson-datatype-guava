package com.fasterxml.jackson.module.guava.deser;

import java.io.IOException;
import java.util.Collection;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import com.google.common.collect.Multimap;

public class MultimapSerializer extends JsonSerializer<Multimap<Object, Object>> {
    private final BeanProperty property;
    public MultimapSerializer(SerializationConfig config, JavaType type, BeanProperty property) {
        this.property = property;
    }

    @Override
    public void serialize(Multimap<Object, Object> value, JsonGenerator jgen, SerializerProvider provider)
    throws IOException, JsonProcessingException {
        jgen.writeStartObject();
        for (Entry<Object, Collection<Object>> e : value.asMap().entrySet()) {
            Object key = e.getKey();
            JsonSerializer<Object> keySerializer = provider.getKeySerializer(TypeFactory.type(key.getClass()), property);
            keySerializer.serialize(key, jgen, provider);
            jgen.writeObject(e.getValue());
        }
        jgen.writeEndObject();
    }
}
