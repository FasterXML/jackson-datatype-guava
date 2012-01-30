package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import static org.codehaus.jackson.JsonToken.*;
import org.codehaus.jackson.map.BeanDescription;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.DeserializerProvider;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.type.JavaType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class MultimapDeserializer extends JsonDeserializer<Multimap<?, ?>> {

    private static final List<Class<?>> KNOWN_IMPLEMENTATIONS =
            ImmutableList.<Class<?>>of(
                ImmutableListMultimap.class,
                ImmutableSetMultimap.class,
                ImmutableMultimap.class
            );
    private static final List<String> METHOD_NAMES = ImmutableList.of("create", "copyOf");

    private final MapLikeType type;
    private final DeserializationConfig config;
    private final DeserializerProvider provider;
    private final BeanDescription beanDesc;
    private final BeanProperty property;
    private final KeyDeserializer keyDeserializer;
    private final TypeDeserializer elementTypeDeserializer;
    private final JsonDeserializer<?> elementDeserializer;

    public MultimapDeserializer(MapLikeType type, DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property, KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        JavaType keyType = type.getKeyType();
        JavaType valueType = type.getContentType();

        this.type = type;
        this.config = config;
        this.provider = provider;
        this.beanDesc = beanDesc;
        this.property = property;
        this.keyDeserializer = keyDeserializer == null ? provider.findKeyDeserializer(config, keyType, property) : keyDeserializer;
        this.elementTypeDeserializer = elementTypeDeserializer;
        this.elementDeserializer = elementDeserializer == null ? provider.findValueDeserializer(config, valueType, property) : elementDeserializer;
    }

    @Override
    public Multimap<?, ?> deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        // Picked LLM since it is preserves both K, V ordering and supports nulls.
        LinkedListMultimap<Object, Object> builder = LinkedListMultimap.create();

        while (jp.nextToken() != END_OBJECT)
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
            expect(jp, START_ARRAY);

            while (jp.nextToken() != END_ARRAY)
            {
                if (elementDeserializer != null)
                {
                    builder.put(key, elementDeserializer.deserializeWithType(jp, ctxt, elementTypeDeserializer));
                }
                else
                {
                    builder.put(key, elementDeserializer.deserialize(jp, ctxt));
                }
            }
        }

        return transform(type.getRawClass(), builder);
    }

    private Multimap<?, ?> transform(Class<?> rawClass, Multimap<Object, Object> map)
        throws JsonMappingException
    {
        LinkedList<Class<?>> classesToTry = Lists.newLinkedList(KNOWN_IMPLEMENTATIONS);
        classesToTry.addFirst(rawClass);

        for (Class<?> klass : classesToTry)
        {
            for (String methodName : METHOD_NAMES)
            {
                try {
                    Method m = klass.getMethod(methodName, Multimap.class);
                    return (Multimap<?, ?>) m.invoke(null, map);
                }
                catch (SecurityException e)
                {
                    throw new JsonMappingException("Could not map to " + klass, e);
                }
                catch (NoSuchMethodException e) { }
                catch (IllegalAccessException e) { }
                catch (InvocationTargetException e)
                {
                    throw new JsonMappingException("Could not map to " + klass, e);
                }
            }
        }

        // If everything goes wrong, just give them the LinkedListMultimap...
        return map;
    }

    private void expect(JsonParser jp, JsonToken token) throws IOException
    {
        if (jp.getCurrentToken() != token)
        {
            throw new JsonMappingException("Expecting " + token + ", found " + jp.getCurrentToken(), jp.getCurrentLocation());
        }
    }
}
