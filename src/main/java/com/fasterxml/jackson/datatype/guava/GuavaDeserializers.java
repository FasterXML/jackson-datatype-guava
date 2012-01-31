package com.fasterxml.jackson.datatype.guava;

import com.google.common.collect.*;

import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapLikeType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

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
            DeserializationConfig config, DeserializerProvider provider,
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
                return new HashMultisetDeserializer(type, elementTypeDeserializer,
                        _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
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
            return new HashMultisetDeserializer(type, elementTypeDeserializer,
                    _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
        }

        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type, elementTypeDeserializer,
                        _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
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
                    return new ImmutableSortedSetDeserializer(type, elementTypeDeserializer,
                            _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
                }
                // nah, just regular one
                return new ImmutableSetDeserializer(type, elementTypeDeserializer,
                        _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
            }
        }
        return null;
    }

    /**
     * A few Map types to support.
     */
    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
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
            return new ImmutableMapDeserializer(type, keyDeserializer, elementTypeDeserializer,
                    _verifyElementDeserializer(elementDeserializer, config, provider, property, type));
        }
        return null;
    }

    @Override
    public JsonDeserializer<?> findMapLikeDeserializer(MapLikeType type, DeserializationConfig config,
            DeserializerProvider provider, BeanDescription beanDesc, BeanProperty property,
            KeyDeserializer keyDeserializer, TypeDeserializer elementTypeDeserializer,
            JsonDeserializer<?> elementDeserializer)
        throws JsonMappingException
    {
        if (Multimap.class.isAssignableFrom(type.getRawClass())) {
            return new MultimapDeserializer(type, config, provider, beanDesc, property,
                    keyDeserializer, elementTypeDeserializer, elementDeserializer);
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
    protected JsonDeserializer<?> _verifyElementDeserializer(JsonDeserializer<?> deser,
            DeserializationConfig config, DeserializerProvider provider,
            BeanProperty prop, JavaType type)
        throws JsonMappingException
    {
        if (deser == null) {
            // 'null' -> collections have no referring fields
            deser = provider.findValueDeserializer(config, type.getContentType(), prop);
        }
        return deser;
    }
}
