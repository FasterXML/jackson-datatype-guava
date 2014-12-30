package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Optional;

@SuppressWarnings("serial")
public final class GuavaOptionalSerializer
    extends StdSerializer<Optional<?>>
    implements ContextualSerializer
{
    /**
     * Declared type for the property being serialized with
     * this serializer instance.
     */
    protected final JavaType _optionalType;

    protected final JsonSerializer<Object> _valueSerializer;

    public GuavaOptionalSerializer(JavaType type) {
        this(type, null);
    }

    @SuppressWarnings("unchecked")
    protected GuavaOptionalSerializer(JavaType type, JsonSerializer<?> valueSer)
    {
        super(type);
        _optionalType = type;
        _valueSerializer = (JsonSerializer<Object>) valueSer;
    }

    protected GuavaOptionalSerializer withResolved(BeanProperty property,
            JsonSerializer<?> ser)
    {
        if (_valueSerializer == ser) {
            return this;
        }
        return new GuavaOptionalSerializer(_optionalType, ser);
    }

    @Override
    @Deprecated
    public boolean isEmpty(Optional<?> value) {
        return isEmpty(null, value);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Optional<?> value) {
        return (value == null) || !value.isPresent();
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> ser = _valueSerializer;
        if (ser == null) {
            // we'll have type parameter available due to GuavaTypeModifier making sure it is, so:
            JavaType valueType = _valueType();
            boolean realType = !valueType.hasRawClass(Object.class);
            /* Can only assign serializer statically if the declared type is final,
             * or if we are to use static typing (and type is not "untyped")
             */
            if (realType &&
                    (provider.isEnabled(MapperFeature.USE_STATIC_TYPING)
                    || valueType.isFinal())) {
                return withResolved(property,
                        provider.findPrimaryPropertySerializer(valueType, property));
            }
        } else {
            // not sure if/when this should occur but proper way to deal would be:
            return withResolved(property,
                    provider.handlePrimaryContextualization(ser, property));
        }
        return this;
    }

    @Override
    public void serialize(Optional<?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException
    {
        if (value.isPresent()) {
            if (_valueSerializer != null) {
                _valueSerializer.serialize(value.get(), jgen, provider);
            } else {
                provider.defaultSerializeValue(value.get(), jgen);
            }
        } else {
            provider.defaultSerializeNull(jgen);
        }
    }

    @Override
    public void serializeWithType(Optional<?> value,
            JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException
    {
        if (value.isPresent()) {
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) {
                // note: could improve by retaining property... needed?
                ser = provider.findValueSerializer(_valueType(), null);
            }
            ser.serializeWithType(value.get(), jgen, provider, typeSer);
        } else {
            provider.defaultSerializeNull(jgen);
        }
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        JavaType valueType = _valueType();
        if (valueType != null) {
            JsonSerializer<?> ser = _valueSerializer;
            if (ser == null) {
                ser = visitor.getProvider().findValueSerializer(valueType, null);
            }
            ser.acceptJsonFormatVisitor(visitor, valueType);
        } else {
            super.acceptJsonFormatVisitor(visitor, typeHint);
        }
    }

    protected JavaType _valueType() {
        JavaType valueType = _optionalType.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        return valueType;
    }
}
    