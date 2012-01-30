package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.CollectionType;

import com.google.common.collect.ImmutableSortedSet;

public class ImmutableSortedSetDeserializer  extends GuavaCollectionDeserializer<ImmutableSortedSet<Object>>
{
    public ImmutableSortedSetDeserializer(CollectionType type, TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, typeDeser, deser);
    }

    @Override
    protected ImmutableSortedSet<Object> _deserializeContents(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonDeserializer<?> valueDes = _valueDeserializer;
        JsonToken t;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
        /* Not quite sure what to do with sorting/ordering; may require better support either
         * via annotations, or via custom serialization (bean style that includes ordering
         * aspects)
         */
        @SuppressWarnings("unchecked")
        ImmutableSortedSet.Builder<?> builderComp = ImmutableSortedSet.<Comparable>naturalOrder();
        @SuppressWarnings("unchecked")
        ImmutableSortedSet.Builder<Object> builder = (ImmutableSortedSet.Builder<Object>) builderComp;

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;
            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            builder.add(value);
        }
        return builder.build();
    }
}
