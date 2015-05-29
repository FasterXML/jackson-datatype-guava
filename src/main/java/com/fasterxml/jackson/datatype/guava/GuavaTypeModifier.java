package com.fasterxml.jackson.datatype.guava;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;

import com.google.common.base.Optional;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

/**
 * We need somewhat hacky support for following Guava types:
 *<ul>
 *  <li>FluentIterable: addition of seeming "empty" property should not prevent serialization as
 *       basic `Iterable` (with standard Jackson (de)serializer)
 *   </li>
 *  <li>Multimap: can reuse much/most of standard Map support as long as we make sure it is
 *      recognized as "Map-like" type (similar to how Scala Maps are supported)
 *   </li>
 *  <li>Optional: generic type, simpler, more-efficient to detect parameterization here (although
 *      not strictly mandatory)
 *   </li>Range: same as with Optional, might as well resolve generic type information early on
 *  <li>
 *   </li>
 *</ul>
 */
public class GuavaTypeModifier extends TypeModifier
{
    /**
     * Set of single-parameter-type types that we can handle using "standard" handling,
     * no additional tricks needed.
     */
    private final static Class<?>[] SINGLE_PARAM_TYPES = new Class<?>[] {
        Range.class, Optional.class
    };
    
    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory)
    {
        final Class<?> raw = type.getRawClass();
        if (Multimap.class.isAssignableFrom(raw)) {
            JavaType keyType = type.containedType(0);
            JavaType contentType = type.containedType(1);

            if (keyType == null) {
                keyType = TypeFactory.unknownType();
            }
            if (contentType == null) {
                contentType = TypeFactory.unknownType();
            }
            return typeFactory.constructMapLikeType(type.getRawClass(), keyType, contentType);
        }
        /* Guava 12 changed the implementation of their {@link FluentIterable} to include a method named "isEmpty."  This method
         * causes Jackson to treat FluentIterables as a Bean instead of an {@link Iterable}.  Serialization of FluentIterables by
         * default result in a string like "{\"empty\":true}."  This module modifies the JavaType of FluentIterable to be
         * the same as Iterable.
         */
        /* Hmmh. This won't work too well for deserialization. But I guess it'll
         * have to do for now...
         */
        if (FluentIterable.class.isAssignableFrom(raw)) {
            JavaType elemType = null;
            JavaType[] types;
            try {
                types = typeFactory.findTypeParameters(type, Iterable.class);
                if (types != null && types.length > 0) {
                    elemType = types[0];
                }
            } catch (IllegalArgumentException e) {
                /* 07-Aug-2015, tatu: Nasty hack, but until we get 100% functioning
                 *   type resolution (from ClassMate project, f.ex.), need to work around
                 *   edge cases with aliasing and/or unresolved type variables.
                 *   So... here we go:
                 */
                String msg = e.getMessage();
                if (msg == null || !msg.contains("Type variable 'T' can not be resolved")) {
                    throw e;
                }
            }
            if (elemType == null) {
                elemType = TypeFactory.unknownType();
            }
            // 30-Dec-2014, tatu: This _should_ work; but if not, may need to consider making
            //    first parameter same as second (old way)
            return typeFactory.constructParametrizedType(FluentIterable.class, Iterable.class, elemType);
        }
        for (Class<?> target : SINGLE_PARAM_TYPES) {
            if (target.isAssignableFrom(raw)) {
                JavaType[] types = typeFactory.findTypeParameters(type, target);
                JavaType t = (types == null || types.length == 0) ? null : types[0];
                if (t == null) {
                    t = TypeFactory.unknownType();
                }
                /* Downcasting is necessary with 'Optional', due to implementation details.
                 * Not sure if it'd be with Range; but let's assume it is, for now: sub-classes
                 * could eliminate/change type parameterization anyway.
                 */
                // 28-May-2015, tatu: Further, Optional needs to be a ReferenceType
                if (target == Optional.class) {
                    return typeFactory.constructReferenceType(raw, t);
                }
                return typeFactory.constructParametrizedType(raw, target, t);
            }
        }
        return type;
    }
}
