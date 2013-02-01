package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeModifier;
import com.google.common.collect.FluentIterable;

import java.lang.reflect.Type;

/**
 * Guava 12 changed the implementation of their {@link FluentIterable} to include a method named "isEmpty."  This method
 * causes Jackson to treat FluentIterables as a Bean instead of an {@link Iterable}.  Serialization of FluentIterables by
 * default result in a string like "{\"empty\":true}."  This module modifies the JavaType of FluentIterable to be
 * the same as Iterable.
 */
public class FluentIterableTypeModifier extends TypeModifier {
    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings context, TypeFactory typeFactory) {
        if (FluentIterable.class.isAssignableFrom(type.getRawClass())) {
            return typeFactory.constructType(Iterable.class, context);
        }

        return type;
    }
}
