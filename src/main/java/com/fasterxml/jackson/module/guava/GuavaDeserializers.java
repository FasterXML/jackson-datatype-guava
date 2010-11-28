package com.fasterxml.jackson.module.guava;

import com.google.common.collect.*;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.ArrayType;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

/**
 * Custom deserializers module offers.
 * 
 * @author tsaloranta
 */
public class GuavaDeserializers implements Deserializers
{
    /**
     * Concrete implementation class to use for properties declared as
     * {@link Multiset}s.
     * Defaults to using 
     */
//    protected Class<? extends Multiset<?>> _cfgDefaultMultiset;

//    protected Class<? extends Multimap<?>> _cfgDefaultMultimap;
    
    /**
     * No need for overrides for array deserializers.
     * fine.
     */
    public JsonDeserializer<?> findArrayDeserializer(ArrayType arg0,
            DeserializationConfig arg1, DeserializerProvider arg2,
            TypeDeserializer arg3, JsonDeserializer<?> arg4) {
        return null;
    }

    /**
     * No bean types to support yet; may need to add?
     */
    public JsonDeserializer<?> findBeanDeserializer(JavaType arg0,
            DeserializationConfig arg1, DeserializerProvider arg2,
            BeanDescription arg3) {
        return null;
    }

    /**
     * We have plenty of collection types to support...
     */
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
    {
        Class<?> raw = type.getRawClass();
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
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
     * No need for overrides for enumeration type deserializers.
     */
    public JsonDeserializer<?> findEnumDeserializer(Class<?> enumType,
            DeserializationConfig config, BeanDescription beanDesc) {
        return null;
    }

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

    /**
     * No need for overrides for Tree Node deserializers.
     */
    public JsonDeserializer<?> findTreeNodeDeserializer(
            Class<? extends JsonNode> arg0, DeserializationConfig arg1) {
        return null;
    }

}
