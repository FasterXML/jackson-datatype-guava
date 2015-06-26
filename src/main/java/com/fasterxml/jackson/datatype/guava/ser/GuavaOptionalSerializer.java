package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.google.common.base.Optional;

@SuppressWarnings("serial")
public final class GuavaOptionalSerializer
    extends StdSerializer<Optional<?>>
    implements ContextualSerializer
{
    /**
     * Declared type parameter for Optional.
     */
    protected final JavaType _referredType;

    protected final BeanProperty _property;
    
    protected final JsonSerializer<Object> _valueSerializer;

    /**
     * To support unwrapped values of dynamic types, will need this:
     */
    protected final NameTransformer _unwrapper;

    /**
     * If element type can not be statically determined, mapping from
     * runtime type to serializer is handled using this object
     *
     * @since 2.6
     */
    protected transient PropertySerializerMap _dynamicSerializers;
    
    /*
    /**********************************************************
    /* Life-cycle
    /**********************************************************
     */

    public GuavaOptionalSerializer(JavaType optionalType) {
        super(optionalType);
        _referredType = _valueType(optionalType);
        _property = null;
        _valueSerializer = null;
        _unwrapper = null;
        _dynamicSerializers = PropertySerializerMap.emptyForProperties();
    }

    @SuppressWarnings("unchecked")
    protected GuavaOptionalSerializer(GuavaOptionalSerializer base,
            BeanProperty property, JsonSerializer<?> valueSer, NameTransformer unwrapper)
    {
        super(base);
        _referredType = base._referredType;
        _dynamicSerializers = base._dynamicSerializers;
        _property = property;
        _valueSerializer = (JsonSerializer<Object>) valueSer;
        _unwrapper = unwrapper;
    }

    protected GuavaOptionalSerializer withResolved(BeanProperty prop,
            JsonSerializer<?> ser, NameTransformer unwrapper)
    {
        if ((_property == prop) && (_valueSerializer == ser) && (_unwrapper == unwrapper)) {
            return this;
        }
        return new GuavaOptionalSerializer(this, prop, ser, unwrapper);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> ser = _valueSerializer;
        if (ser == null) {
            // we'll have type parameter available due to GuavaTypeModifier making sure it is, so:
            boolean realType = !_referredType.hasRawClass(Object.class);
            /* Can only assign serializer statically if the declared type is final,
             * or if we are to use static typing (and type is not "untyped")
             */
            if (realType &&
                    (provider.isEnabled(MapperFeature.USE_STATIC_TYPING)
                    || _referredType.isFinal())) {
                return withResolved(property,
                        provider.findPrimaryPropertySerializer(_referredType, property),
                        _unwrapper);
            }
        } else {
            // not sure if/when this should occur but proper way to deal would be:
            return withResolved(property,
                    provider.handlePrimaryContextualization(ser, property),
                    _unwrapper);
        }
        return this;
    }

    @Override
    public JsonSerializer<Optional<?>> unwrappingSerializer(NameTransformer transformer) {
        JsonSerializer<Object> ser = _valueSerializer;
        if (ser != null) {
            ser = ser.unwrappingSerializer(transformer);
        }
        NameTransformer unwrapper = (_unwrapper == null) ? transformer
                : NameTransformer.chainedTransformer(transformer, _unwrapper);
        return withResolved(_property, ser, unwrapper);
    }

    /*
    /**********************************************************
    /* API overrides
    /**********************************************************
     */

    @Override
    @Deprecated
    public boolean isEmpty(Optional<?> value) {
        return isEmpty(null, value);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Optional<?> value) {
        return (value == null) || !value.isPresent();
    }

    public boolean isUnwrappingSerializer() {
        return (_unwrapper != null);
    }

    /*
    /**********************************************************
    /* Serialization methods
    /**********************************************************
     */

    @Override
    public void serialize(Optional<?> opt, JsonGenerator gen, SerializerProvider provider)
        throws IOException
    {
        if (opt.isPresent()) {
            Object value = opt.get();
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) {
                ser = _findSerializer(provider, value.getClass());
            }
            ser.serialize(value, gen, provider);
        } else {
            provider.defaultSerializeNull(gen);
        }
    }

    @Override
    public void serializeWithType(Optional<?> opt,
            JsonGenerator gen, SerializerProvider provider,
            TypeSerializer typeSer) throws IOException
    {
        if (opt.isPresent()) {
            Object value = opt.get();
            JsonSerializer<Object> ser = _valueSerializer;
            if (ser == null) {
                ser = _findSerializer(provider, value.getClass());
            }
            ser.serializeWithType(value, gen, provider, typeSer);
        } else {
            provider.defaultSerializeNull(gen);
        }
    }

    /*
    /**********************************************************
    /* Introspection support
    /**********************************************************
     */
    
    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException
    {
        JsonSerializer<?> ser = _valueSerializer;
        if (ser == null) {
            ser = _findSerializer(visitor.getProvider(), _referredType.getRawClass());
        }
        ser.acceptJsonFormatVisitor(visitor, _referredType);
    }

    /*
    /**********************************************************
    /* Misc other
    /**********************************************************
     */
    
    protected static JavaType _valueType(JavaType optionalType) {
        JavaType valueType = optionalType.containedType(0);
        if (valueType == null) {
            valueType = TypeFactory.unknownType();
        }
        return valueType;
    }

    /**
     * Helper method that encapsulates logic of retrieving and caching required
     * serializer.
     */
    protected final JsonSerializer<Object> _findSerializer(SerializerProvider provider, Class<?> type)
        throws JsonMappingException
    {
        PropertySerializerMap.SerializerAndMapResult result = _dynamicSerializers
                .findAndAddPrimarySerializer(type, provider, _property);
        if (_dynamicSerializers != result.map) {
            _dynamicSerializers = result.map;
        }
        JsonSerializer<Object> ser = result.serializer;
        // 26-Jun-2015, tatu: Sub-optimal if we do not cache unwrapped instance; but on plus side
        //   construction is a cheap operation, so won't add huge overhead
        if (_unwrapper != null) {
            ser = ser.unwrappingSerializer(_unwrapper);
        }
        return ser;
    }
}
