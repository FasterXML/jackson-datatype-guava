package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import com.google.common.collect.ImmutableTable;

abstract class GuavaImmutableTableDeserializer<T extends ImmutableTable<Object, Object, Object>> extends GuavaTableDeserializer<T>
{
    GuavaImmutableTableDeserializer( final JavaType javaType )
    {
        super(javaType);
    }

    protected abstract ImmutableTable.Builder<Object, Object, Object> createBuilder();

    @Override
    protected T _deserializeEntries( final JsonParser jp, final DeserializationContext ctxt ) throws IOException, JsonProcessingException
    {
        final KeyDeserializer rowKeyDes = this._rowKeyDeserializer;
        final KeyDeserializer columnKeyDes = this._columnKeyDeserializer;
        final JsonDeserializer<?> valueDes = this._valueDeserializer;
        final TypeDeserializer typeDeser = this._typeDeserializerForValue;

        final ImmutableTable.Builder<Object, Object, Object> builder = this.createBuilder();
        for ( ; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken() ) {
            // Must point to row now
            final String rowName = jp.getCurrentName();
            final Object row = (rowKeyDes == null) ? rowName : rowKeyDes.deserializeKey(rowName, ctxt);
            // And then the {column => value} start token...
            jp.nextToken();
            // Now pointing to column
            jp.nextToken();

            for ( ; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken() ) {
                // Must point to column now
                final String columnName = jp.getCurrentName();
                final Object column = (columnKeyDes == null) ? columnName : columnKeyDes.deserializeKey(columnName, ctxt);
                // And then the value...
                final JsonToken tValue = jp.nextToken();
                // 28-Nov-2010, tatu: Should probably support "ignorable properties" in future...
                Object value;
                if (tValue == JsonToken.VALUE_NULL) {
                    value = null;
                }
                else {
                    value = (typeDeser == null) ? valueDes.deserialize(jp, ctxt) : valueDes.deserializeWithType(jp, ctxt, typeDeser);
                    builder.put(row, column, value);
                }
            }
        }
        // No class outside of the package will be able to subclass us,
        // and we provide the proper builder for the subclasses we implement.
        @SuppressWarnings( "unchecked" )
        final T table = (T) builder.build();
        return table;
    }
}
