package com.fasterxml.jackson.datatype.guava;

import com.google.common.base.Optional;
import com.google.common.collect.*;
import com.google.common.hash.HashCode;
import com.google.common.net.HostAndPort;
import com.google.common.net.InternetDomainName;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.ReferenceType;
import com.fasterxml.jackson.datatype.guava.deser.*;
import com.fasterxml.jackson.datatype.guava.deser.multimap.list.ArrayListMultimapDeserializer;
import com.fasterxml.jackson.datatype.guava.deser.multimap.list.LinkedListMultimapDeserializer;
import com.fasterxml.jackson.datatype.guava.deser.multimap.set.HashMultimapDeserializer;
import com.fasterxml.jackson.datatype.guava.deser.multimap.set.LinkedHashMultimapDeserializer;

/**
 * Custom deserializers module offers.
 */
public class GuavaDeserializers
    extends Deserializers.Base
{
    protected BoundType _defaultBoundType;

    public GuavaDeserializers() {
        this(null);
    }

    public GuavaDeserializers(BoundType defaultBoundType) {
        _defaultBoundType = defaultBoundType;
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
                // sorted one?
                if (ImmutableSortedMultiset.class.isAssignableFrom(raw)) {
                    /* See considerations for ImmutableSortedSet below. */
                    requireCollectionOfComparableElements(type, "ImmutableSortedMultiset");
                    return new ImmutableSortedMultisetDeserializer(type,
                            elementTypeDeserializer, elementDeserializer);
                }
                // nah, just regular one
                return new ImmutableMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
                // sorted one?
                if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
                    /* 28-Nov-2010, tatu: With some more work would be able to use other things
                     *   than natural ordering; but that'll have to do for now...
                     */
                    requireCollectionOfComparableElements(type, "ImmutableSortedSet");
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
            if (SortedMultiset.class.isAssignableFrom(raw)) {
                if (TreeMultiset.class.isAssignableFrom(raw)) {
                    return new TreeMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
                }

                // TODO: make configurable (for now just default blindly)
                return new TreeMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
            }

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

            // TODO: make configurable (for now just default blindly)
            return new HashMultisetDeserializer(type, elementTypeDeserializer, elementDeserializer);
        }

        return null;
    }

    private void requireCollectionOfComparableElements(CollectionType actualType, String targetType) {
        Class<?> elemType = actualType.getContentType().getRawClass();
        if (!Comparable.class.isAssignableFrom(elemType)) {
            throw new IllegalArgumentException("Can not handle " + targetType
                    + " with elements that are not Comparable<?> (" + elemType.getName() + ")");
        }
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

        // ListMultimaps
        if (ListMultimap.class.isAssignableFrom(raw)) {
            if (ImmutableListMultimap.class.isAssignableFrom(raw)) {
                // TODO
            }
            if (ArrayListMultimap.class.isAssignableFrom(raw)) {
                return new ArrayListMultimapDeserializer(type, keyDeserializer,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (LinkedListMultimap.class.isAssignableFrom(raw)) {
                return new LinkedListMultimapDeserializer(type, keyDeserializer,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (ForwardingListMultimap.class.isAssignableFrom(raw)) {
                // TODO
            }

            // TODO: Remove the default fall-through once all implementations are in place.
            return new ArrayListMultimapDeserializer(type, keyDeserializer,
                    elementTypeDeserializer, elementDeserializer);
        }

        // SetMultimaps
        if (SetMultimap.class.isAssignableFrom(raw)) {

            // SortedSetMultimap
            if (SortedSetMultimap.class.isAssignableFrom(raw)) {
                if (TreeMultimap.class.isAssignableFrom(raw)) {
                    // TODO
                }
                if (ForwardingSortedSetMultimap.class.isAssignableFrom(raw)) {
                    // TODO
                }
            }

            if (ImmutableSetMultimap.class.isAssignableFrom(raw)) {
                // [Issue#67]: Preserve order of entries
                return new LinkedHashMultimapDeserializer(type, keyDeserializer,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (HashMultimap.class.isAssignableFrom(raw)) {
                return new HashMultimapDeserializer(type, keyDeserializer, elementTypeDeserializer,
                        elementDeserializer);
            }
            if (LinkedHashMultimap.class.isAssignableFrom(raw)) {
                return new LinkedHashMultimapDeserializer(type, keyDeserializer,
                        elementTypeDeserializer, elementDeserializer);
            }
            if (ForwardingSetMultimap.class.isAssignableFrom(raw)) {
                // TODO
            }

            // TODO: Remove the default fall-through once all implementations are covered.
            return new HashMultimapDeserializer(type, keyDeserializer, elementTypeDeserializer,
                    elementDeserializer);
        }

        // Handle the case where nothing more specific was provided.
        if (Multimap.class.isAssignableFrom(raw)) {
            return new LinkedListMultimapDeserializer(type, keyDeserializer,
                    elementTypeDeserializer, elementDeserializer);
        }

        if (Table.class.isAssignableFrom(raw)) {
            // !!! TODO
        }

        return null;
    }

    // 21-Oct-2015, tatu: Code much simplified with 2.7 where we should be getting much
    //    of boilerplate handling automatically

    @Override // since 2.7
    public JsonDeserializer<?> findReferenceDeserializer(ReferenceType refType,
            DeserializationConfig config, BeanDescription beanDesc,
            TypeDeserializer contentTypeDeserializer, JsonDeserializer<?> contentDeserializer)
    {
        if (refType.hasRawClass(Optional.class)) {
            return new GuavaOptionalDeserializer(refType, contentTypeDeserializer, contentDeserializer);
        }
        return null;
    }
    
    @Override
    public JsonDeserializer<?> findBeanDeserializer(final JavaType type, DeserializationConfig config,
            BeanDescription beanDesc)
    {
        if (type.hasRawClass(Range.class)) {
            return new RangeDeserializer(_defaultBoundType, type);
        }
        if (type.hasRawClass(HostAndPort.class)) {
            return HostAndPortDeserializer.std;
        }
        if (type.hasRawClass(InternetDomainName.class)) {
            return InternetDomainNameDeserializer.std;
        }
        if (type.hasRawClass(HashCode.class)) {
            return HashCodeDeserializer.std;
        }
        return null;
    }
}
