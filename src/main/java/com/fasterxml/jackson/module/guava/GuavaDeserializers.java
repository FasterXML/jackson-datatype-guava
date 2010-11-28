package com.fasterxml.jackson.module.guava;

import com.google.common.collect.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;

import com.fasterxml.jackson.module.guava.deser.*;

/**
 * Custom deserializers module offers.
 * 
 * @author tsaloranta
 */
public class GuavaDeserializers
    extends Deserializers.None
{
    /**
     * Concrete implementation class to use for properties declared as
     * {@link Multiset}s.
     * Defaults to using 
     */
//    protected Class<? extends Multiset<?>> _cfgDefaultMultiset;

//    protected Class<? extends Multimap<?>> _cfgDefaultMultimap;
    
    /*
     * No bean types to support yet; may need to add?
     */
    /*
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc) {
        return null;
    }
    */

    /**
     * We have plenty of collection types to support...
     */
    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException
    {
        Class<?> raw = type.getRawClass();
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type, elementTypeDeser,
                        _verifyElementDeserializer(elementDeser, type, config, provider));
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
                // sorted one?
                if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
                    return new ImmutableSortedSetDeserializer(type, elementTypeDeser,
                            _verifyElementDeserializer(elementDeser, type, config, provider));
                }
                // nah, just regular one
                return new ImmutableSetDeserializer(type, elementTypeDeser,
                        _verifyElementDeserializer(elementDeser, type, config, provider));
            }
        }
        // Multi-xxx collections?
        if (Multiset.class.isAssignableFrom(raw)) {
            // !!! TODO
        }
        return null;
    }

    /**
     * A few Map types to support.
     */
    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, KeyDeserializer keyDeser,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
    {
        Class<?> raw = type.getRawClass();
        // ImmutableXxxMap types?
        if (ImmutableMap.class.isAssignableFrom(raw)) {
            if (ImmutableSortedMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (ImmutableBiMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            // Otherwise, plain old ImmutableMap...
        }
        // Multimaps?
        if (Multimap.class.isAssignableFrom(raw)) {
            if (ListMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (SetMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (SortedSetMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
        }
        return null;
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    /**
     * Helper method used to ensure that we have a deserializer for elements
     * of collection being deserialized.
     */
    JsonDeserializer<?> _verifyElementDeserializer(JsonDeserializer<?> deser,
            CollectionType type,
            DeserializationConfig config, DeserializerProvider provider)
        throws JsonMappingException
    {
        if (deser == null) {
            // 'null' -> collections have no referring fields
            deser = provider.findValueDeserializer(config, type.getContentType(), type, null);            
        }
        return deser;
    }
}
