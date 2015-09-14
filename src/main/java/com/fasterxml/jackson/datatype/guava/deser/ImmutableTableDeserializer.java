package com.fasterxml.jackson.datatype.guava.deser;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import com.google.common.collect.ImmutableTable;

public class ImmutableTableDeserializer extends GuavaImmutableTableDeserializer<ImmutableTable<Object, Object, Object>>
{
    public ImmutableTableDeserializer( final JavaType javaType )
    {
        super(javaType);
    }

    public ImmutableTableDeserializer( final JavaType javaType,
                                       final KeyDeserializer rowKeyDeser,
                                       final KeyDeserializer columnKeyDeser,
                                       final TypeDeserializer typeDeser,
                                       final JsonDeserializer<?> deser )
    {
        super(javaType);
        this._rowKeyDeserializer = rowKeyDeser;
        this._columnKeyDeserializer = columnKeyDeser;
        this._valueDeserializer = deser;
        this._typeDeserializerForValue = typeDeser;
    }

    @Override
    public ImmutableTableDeserializer withResolved( final KeyDeserializer rowKeyDeser,
                                                    final KeyDeserializer columnKeyDeser,
                                                    final TypeDeserializer typeDeser,
                                                    final JsonDeserializer<?> valueDeser )
    {
        return new ImmutableTableDeserializer(this._javaType, rowKeyDeser, columnKeyDeser, typeDeser, valueDeser);
    }

    @Override
    protected ImmutableTable.Builder<Object, Object, Object> createBuilder()
    {
        return ImmutableTable.builder();
    }
}
