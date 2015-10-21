package com.fasterxml.jackson.datatype.guava;

import java.lang.reflect.Type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.*;

import com.google.common.base.Optional;
import com.google.common.collect.*;

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
    @Override
    public JavaType modifyType(JavaType type, Type jdkType, TypeBindings bindings, TypeFactory typeFactory)
    {
        if (type.isReferenceType() || type.isContainerType()) {
            return type;
        }

        final Class<?> raw = type.getRawClass();
        // First: make Multimaps look more Map-like
        if (raw == Multimap.class) {
            return MapLikeType.upgradeFrom(type,
                            type.containedTypeOrUnknown(0),
                            type.containedTypeOrUnknown(1));
        }
        if (raw == Optional.class) {
            return ReferenceType.upgradeFrom(type, type.containedTypeOrUnknown(0));
        }
        return type;
    }
}
