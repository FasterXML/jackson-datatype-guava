package com.fasterxml.jackson.datatype.guava;

import com.google.common.collect.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
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
     * We have plenty of collection types to support...
     */
    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, BeanDescription beanDesc,
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
                return new HashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
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
            return new HashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
        }
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type,
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
                    return new ImmutableSortedSetDeserializer(type,
                            elementTypeDeserializer, elementDeserializer);
                }
                // nah, just regular one
                return new ImmutableSetDeserializer(type,
                        elementTypeDeserializer, elementDeserializer);
            }
        }
        return null;
    }

    /**
     * A few Map types to support.
     */
    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type,
            DeserializationConfig config, BeanDescription beanDesc,
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
            return new ImmutableMapDeserializer(type,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
        }
        return null;
    }

    @Override
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type,
            DeserializationConfig config, BeanDescription beanDesc,
            KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        if (Multimap.class.isAssignableFrom(type.getRawClass())) {
            return new MultimapDeserializer(type,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
        }

        return null;
    }

}
