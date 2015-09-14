package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

public abstract class GuavaTableDeserializer<T> extends JsonDeserializer<T> implements ContextualDeserializer
{
    protected final JavaType _javaType;

    /**
     * Row key deserializer used, if not null. If null, String from JSON content is used as is.
     */
    protected KeyDeserializer _rowKeyDeserializer;

    /**
     * Column key deserializer used, if not null. If null, String from JSON content is used as is.
     */
    protected KeyDeserializer _columnKeyDeserializer;

    /**
     * Value deserializer.
     */
    protected JsonDeserializer<?> _valueDeserializer;

    /**
     * If value instances have polymorphic type information, this is the type deserializer that can handle it.
     */
    protected TypeDeserializer _typeDeserializerForValue;

    /*
     * Life-cycle
     */

    protected GuavaTableDeserializer( final JavaType javaType )
    {
        this._javaType = javaType;
    }

    /**
     * Overridable fluent factory method used for creating contextual instances.
     */
    public abstract GuavaTableDeserializer<T> withResolved( final KeyDeserializer rowKeyDeser,
                                                            final KeyDeserializer columnKeyDeser,
                                                            final TypeDeserializer typeDeser,
                                                            final JsonDeserializer<?> valueDeser );

    /*
     * Validation, post-processing
     */

    /**
     * Method called to finalize setup of this deserializer, after deserializer itself has been registered. This is needed to handle recursive and
     * transitive dependencies.
     */
    @Override
    public JsonDeserializer<?> createContextual( final DeserializationContext ctxt, final BeanProperty property ) throws JsonMappingException
    {
        KeyDeserializer rowKeyDeser = this._rowKeyDeserializer;
        KeyDeserializer columnKeyDeser = this._columnKeyDeserializer;
        JsonDeserializer<?> deser = this._valueDeserializer;
        TypeDeserializer typeDeser = this._typeDeserializerForValue;
        // Do we need any contextualization?
        if ((rowKeyDeser != null) && (columnKeyDeser != null) && (deser != null) && (typeDeser == null)) { // nope
            return this;
        }
        if (rowKeyDeser == null) {
            rowKeyDeser = ctxt.findKeyDeserializer(this._javaType.containedType(0), property);
        }
        if (columnKeyDeser == null) {
            columnKeyDeser = ctxt.findKeyDeserializer(this._javaType.containedType(1), property);
        }
        if (deser == null) {
            deser = ctxt.findContextualValueDeserializer(this._javaType.containedType(2), property);
        }
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }
        return this.withResolved(rowKeyDeser, columnKeyDeser, typeDeser, deser);
    }

    /*
     * Deserialization interface
     */

    /**
     * Base implementation that does not assume specific type inclusion mechanism. Sub-classes are expected to override this method if they are to
     * handle type information.
     */
    @Override
    public Object deserializeWithType( final JsonParser jp, final DeserializationContext ctxt, final TypeDeserializer typeDeserializer )
        throws IOException, JsonProcessingException
    {
        // note: call "...FromObject" because expected output structure
        // for value is JSON Object (regardless of contortions used for type id)
        return typeDeserializer.deserializeTypedFromObject(jp, ctxt);
    }

    @Override
    public T deserialize( final JsonParser jp, final DeserializationContext ctxt ) throws IOException, JsonProcessingException
    {
        // Ok: must point to START_OBJECT or FIELD_NAME
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) { // If START_OBJECT, move to next; may also be END_OBJECT
            t = jp.nextToken();
        }
        if (t != JsonToken.FIELD_NAME && t != JsonToken.END_OBJECT) {
            throw ctxt.mappingException(this._javaType.getRawClass());
        }
        return this._deserializeEntries(jp, ctxt);
    }

    /*
     * Abstract methods for impl classes
     */

    protected abstract T _deserializeEntries( final JsonParser jp, final DeserializationContext ctxt ) throws IOException, JsonProcessingException;
}
