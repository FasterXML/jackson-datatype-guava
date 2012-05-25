package com.fasterxml.jackson.datatype.guava;


import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.datatype.guava.deser.*;
import com.google.common.base.Optional;
import com.google.common.collect.*;

/**
 * Custom deserializers module offers.
 * 
 * @author tsaloranta
 */
public class GuavaDeserializers
    extends Deserializers.Base
{
    @Override
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, DeserializationConfig config,
            BeanDescription beanDesc) throws JsonMappingException {
        Class<?> raw = type.getRawClass();
        if(Optional.class.isAssignableFrom(raw)){
            return new GuavaOptionalDeserializer(type);
        }
        return super.findBeanDeserializer(type, config, beanDesc);
    }
    
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

        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (ImmutableMultiset.class.isAssignableFrom(raw)) {
                // 15-May-2012, pgelinas: There is no ImmutableSortedMultiset
                // available yet
                return new ImmutableMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
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
            // TODO: make configurable (for now just default blindly to a list)
            return new ImmutableListDeserializer(type, elementTypeDeserializer, elementDeserializer);
        }

        // Multi-xxx collections?
        if (Multiset.class.isAssignableFrom(raw)) {
            // Quite a few variations...
            if (LinkedHashMultiset.class.isAssignableFrom(raw)) {
                return new LinkedHashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }
            if (HashMultiset.class.isAssignableFrom(raw)) {
                return new HashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }
            if (EnumMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (TreeMultiset.class.isAssignableFrom(raw)) {
                return new TreeMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }

            // TODO: make configurable (for now just default blindly)
            return new HashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
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
                return new ImmutableSortedMapDeserializer(type, keyDeserializer, elementTypeDeserializer,
                        elementDeserializer);
            }
            if (ImmutableBiMap.class.isAssignableFrom(raw)) {
                return new ImmutableBiMapDeserializer(type, keyDeserializer, elementTypeDeserializer,
                        elementDeserializer);
            }
            // Otherwise, plain old ImmutableMap...
            return new ImmutableMapDeserializer(type, keyDeserializer, elementTypeDeserializer, elementDeserializer);
        }

        // XxxBiMap types?
        if (BiMap.class.isAssignableFrom(raw)) {
            if (EnumBiMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (EnumHashBiMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (HashBiMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            // !!! TODO default
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
        Class<?> raw = type.getRawClass();

        // ImmutableMultimap
        if (ImmutableMultimap.class.isAssignableFrom(raw)) {
            if (ImmutableListMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (ImmutableSetMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            // TODO plain immutable multimap
        }

        if (Multimap.class.isAssignableFrom(raw)) {
            return new MultimapDeserializer(type, keyDeserializer, elementTypeDeserializer, elementDeserializer);
        }

        if (Table.class.isAssignableFrom(raw)) {
            // !!! TODO
        }

        return null;
    }

}
