package com.fasterxml.jackson.datatype.guava;

import com.google.common.collect.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapType;

import com.fasterxml.jackson.datatype.guava.deser.*;

/**
 * Custom deserializers module offers.
 * 
 * @author tsaloranta
 */
public class GuavaDeserializers
    extends Deserializers.Base
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

    /*
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException;
     */
    
    /**
     * We have plenty of collection types to support...
     */
    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        Class<?> raw = type.getRawClass();

        // Multi-xxx collections?
        if (Multiset.class.isAssignableFrom(raw)) {
            // Quite a few variations...
            if (LinkedHashMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (HashMultiset.class.isAssignableFrom(raw)) {
                return new HashMultisetDeserializer(type, property,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (ImmutableMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (EnumMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (TreeMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }

            // TODO: make configurable (for now just default blindly)
            return new HashMultisetDeserializer(type, property,
                    elementTypeDeserializer, elementDeserializer);
        }
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type, property,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
                // sorted one?
                if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
                    /* 28-Nov-2010, tatu: With some more work would be able to use other things
                     *   than natural ordering; but that'll have to do for now...
                     */
                    Class<?> elemType = type.getContentType().getRawClass();
                    if (!Comparable.class.isAssignableFrom(elemType)) {
                        throw new IllegalArgumentException("Can not handle ImmutableSortedSet with elements that are not Comparable<?> ("
                                +raw.getName()+")");
                    }
                    return new ImmutableSortedSetDeserializer(type, property,
                            elementTypeDeserializer, elementDeserializer);
                }
                // nah, just regular one
                return new ImmutableSetDeserializer(type, property,
                        elementTypeDeserializer, elementDeserializer);
            }
        }
        return null;
    }

    /**
     * A few Map types to support.
     */
    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config,
            BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer,
            TypeDeserializer elementTypeDeserializer, JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
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
            return new ImmutableMapDeserializer(type, property,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
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
