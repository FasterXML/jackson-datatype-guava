package com.fasterxml.jackson.datatype.guava;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Multimap;

public class GuavaTypeModifier extends TypeModifier
{
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
            JavaType[] types = typeFactory.findTypeParameters(type, Iterable.class);
            JavaType elemType = (types == null || types.length < 1)
                    ? null : types[0];
            if (elemType == null) {
                elemType = TypeFactory.unknownType();
            }
            return typeFactory.constructParametricType(Iterable.class,  elemType);
        }
        return type;
    }
}
