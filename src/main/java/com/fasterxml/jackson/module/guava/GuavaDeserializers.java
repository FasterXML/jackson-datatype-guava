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

        // First things first: find value deserializer, if it's missing so far:
        if (elementDeser == null) {
            // 'null' -> collections have no referring fields
            elementDeser = provider.findValueDeserializer(config, type.getContentType(), type, null);            
        }
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type, elementTypeDeser, elementDeser);
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
                // sorted one?
                if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
                    // !!! TODO
                }
                // nah, just regular one
                // !!! TODO
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
}
