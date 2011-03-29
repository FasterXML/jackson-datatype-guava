package com.fasterxml.jackson.module.guava.deser;

import java.io.IOException;
import java.util.Collection;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import com.google.common.collect.Multimap;

public class ImmutableMultimapDeserializer extends JsonDeserializer<ImmutableMultimap<Object, Object>>
{
    private final Builder<Object, Object> builder;
    private final JavaType type, keyType, valueType;
    private final BeanProperty property;
    public ImmutableMultimapDeserializer(Builder<Object, Object> builder, JavaType type, BeanProperty property)
    {
        this.builder = builder;
        this.type = type;
        JavaType[] types = TypeFactory.findParameterTypes(type, Multimap.class);
        this.keyType = types[0];
        // V is deserialized as Collection<V> so we can putAll on the builder
        this.valueType = TypeFactory.collectionType(Collection.class, types[1]);
        this.property = property;
    }

    /**
     * Base implementation that does not assume specific type
     * inclusion mechanism. Sub-classes are expected to override
     * this method if they are to handle type information.
     */
    @Override
    public Object deserializeWithType(JsonParser jp, DeserializationContext ctxt,
            TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        return typeDeserializer.deserializeTypedFromArray(jp, ctxt);
    }

    @Override
    public ImmutableMultimap<Object, Object> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException
    {
        // Ok: must point to START_OBJECT or FIELD_NAME
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) { // If START_OBJECT, move to next; may also be END_OBJECT
            t = jp.nextToken();
            if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
                throw ctxt.mappingException(type.getRawClass());
            }
        } else if (t != JsonToken.FIELD_NAME) {
            throw ctxt.mappingException(type.getRawClass());
        }
        return _deserializeEntries(jp, ctxt);
    }

    protected ImmutableMultimap<Object, Object> _deserializeEntries(JsonParser jp,
            DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        DeserializerProvider provider = ctxt.getDeserializerProvider();
        DeserializationConfig config = ctxt.getConfig();
        final KeyDeserializer keyDes = provider.findKeyDeserializer(config, keyType, property);
        final JsonDeserializer<?> valueDes = provider.findValueDeserializer(config, valueType, property);
        final TypeDeserializer typeDeser = null; // XXX

        for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
            // Must point to field name now
            String fieldName = jp.getCurrentName();
            Object key = (keyDes == null) ? fieldName : keyDes.deserializeKey(fieldName, ctxt);
            // And then the value...
            JsonToken t = jp.nextToken();
            // 28-Nov-2010, tatu: Should probably support "ignorable properties" in future...
            Object value;
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            builder.putAll(key, (Collection<?>) value);
        }
        return builder.build();
    }

}
