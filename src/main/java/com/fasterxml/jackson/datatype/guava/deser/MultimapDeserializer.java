package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.MapLikeType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

public class MultimapDeserializer extends JsonDeserializer<Multimap<?, ?>>
    implements ContextualDeserializer
{
    private static final List<String> METHOD_NAMES = ImmutableList.of("copyOf", "create");

    private final MapLikeType type;
    private final KeyDeserializer keyDeserializer;
    private final TypeDeserializer elementTypeDeserializer;
    private final JsonDeserializer<?> elementDeserializer;

    /**
     * Since we have to use a method to transform from a known multi-map
     * type into actual one, we'll resolve method just once, use it.
     * Note that if this is set to null, we can just construct a
     * {@link LinkedListMultimap} instance and be done with it.
     */
    private final Method creatorMethod;
    
    public MultimapDeserializer(MapLikeType type,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
    {
        this(type, keyDeserializer, elementTypeDeserializer, elementDeserializer,
                findTransformer(type.getRawClass()));
    }
    
    public MultimapDeserializer(MapLikeType type,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer,
            Method creatorMethod)
    {
        this.type = type;
        this.keyDeserializer = keyDeserializer;
        this.elementTypeDeserializer = elementTypeDeserializer;
        this.elementDeserializer = elementDeserializer;
        this.creatorMethod = creatorMethod;
    }

    /**
     * We need to use this method to properly handle possible contextual
     * variants of key and value deserializers, as well as type deserializers.
     */
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        KeyDeserializer kd = keyDeserializer;
        if (kd == null) {
            kd = ctxt.findKeyDeserializer(type.getKeyType(), property);
        }
        JsonDeserializer<?> ed = elementDeserializer;
        if (ed == null) {
            ed = ctxt.findContextualValueDeserializer(type.getContentType(), property);
        }
        // Type deserializer is slightly different; must be passed, but needs to become contextual:
        TypeDeserializer etd = elementTypeDeserializer;
        if (etd != null && property != null) {
            etd = etd.forProperty(property);
        }
        return new MultimapDeserializer(type, kd, etd, ed, this.creatorMethod);
    }
    
    @Override
    public Multimap<?, ?> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // Picked LLM since it is preserves both K, V ordering and supports nulls.
        LinkedListMultimap<Object, Object> builder = LinkedListMultimap.create();

        //     private static final List<String> METHOD_NAMES = ImmutableList.of("create", "copyOf");

        
        while (jp.nextToken() != JsonToken.END_OBJECT)
        {
            final Object key;
            if (keyDeserializer != null)
            {
                key = keyDeserializer.deserializeKey(jp.getCurrentName(), ctxt);
            }
            else
            {
                key = jp.getCurrentName();
            }

            jp.nextToken();
            expect(jp, JsonToken.START_ARRAY);

            while (jp.nextToken() != JsonToken.END_ARRAY)
            {
                if (elementTypeDeserializer != null)
                {
                    builder.put(key, elementDeserializer.deserializeWithType(jp, ctxt, elementTypeDeserializer));
                }
                else
                {
                    builder.put(key, elementDeserializer.deserialize(jp, ctxt));
                }
            }
        }
        if (creatorMethod == null) {
            return builder;
        }
        try {
            return (Multimap<?, ?>) creatorMethod.invoke(null, builder);
        } catch (InvocationTargetException e) {
            throw new JsonMappingException("Could not map to " + type, _peel(e));
        } catch (IllegalArgumentException e) {
            throw new JsonMappingException("Could not map to " + type, _peel(e));
        } catch (IllegalAccessException e) {
            throw new JsonMappingException("Could not map to " + type, _peel(e));
        }
    }

    private static Method findTransformer(Class<?> rawType)
    {
        // Very first thing: if it's a "standard multi-map type", can avoid copying
        if (rawType == LinkedListMultimap.class
                || rawType == ListMultimap.class
                || rawType == Multimap.class) {
            return null;
        }
        
        // First, check type itself for matching methods
        for (String methodName : METHOD_NAMES) {
            try {
                Method m = rawType.getMethod(methodName, Multimap.class);
                if (m != null) {
                    return m;
                }
            } catch (NoSuchMethodException e) { }
            // pass SecurityExceptions as-is:
            // } catch (SecurityException e) { }
        }

        // If not working, possibly super types too (should we?)
        for (String methodName : METHOD_NAMES) {
            try {
                Method m = rawType.getMethod(methodName, Multimap.class);
                if (m != null) {
                    return m;
                }
            } catch (NoSuchMethodException e) { }
            // pass SecurityExceptions as-is:
            // } catch (SecurityException e) { }
        }

        return null;
    }
    
    private void expect(JsonParser jp, JsonToken token) throws IOException
    {
        if (jp.getCurrentToken() != token) {
            throw new JsonMappingException("Expecting " + token + ", found " + jp.getCurrentToken(), jp.getCurrentLocation());
        }
    }

    private Throwable _peel(Throwable t)
    {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
}
