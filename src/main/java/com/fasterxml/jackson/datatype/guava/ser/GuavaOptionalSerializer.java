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
            // Can only assign serializer statically if the declared type is final,
            // or if we are to use static typing (and type is not "untyped")
            if (realType &&
                    (provider.isEnabled(MapperFeature.USE_STATIC_TYPING)
                    || _referredType.isFinal())) {
                return withResolved(property,
                        _findSerializer(provider, _referredType, _property),
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

    @Override
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
            // 22-Oct-2015, tatu: With unwrapping we can not serialize value, just key/value pairs so:
            if (_unwrapper == null) {
                provider.defaultSerializeNull(gen);
            }
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
            if (_unwrapper == null) {
                provider.defaultSerializeNull(gen);
            }
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
            // 28-Sep-2015, tatu: as per [datatype-guava#83] need to ensure we don't
            //    accidentally drop parameterization
            ser = _findSerializer(visitor.getProvider(), _referredType, _property);
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
    protected final JsonSerializer<Object> _findSerializer(SerializerProvider provider,
            Class<?> type) throws JsonMappingException
    {
        JsonSerializer<Object> ser = _dynamicSerializers.serializerFor(type);
        if (ser == null) {
            ser = _findSerializer(provider, type, _property);
            if (_unwrapper != null) {
                ser = ser.unwrappingSerializer(_unwrapper);
            }
            _dynamicSerializers = _dynamicSerializers.newWith(type, ser);
        }
        return ser;
    }

    private final JsonSerializer<Object> _findSerializer(SerializerProvider provider,
            Class<?> type, BeanProperty prop) throws JsonMappingException
    {
        // Important: ask for TYPED serializer, in case polymorphic handling is needed!
        return provider.findTypedValueSerializer(type, true, prop);
    }

    private final JsonSerializer<Object> _findSerializer(SerializerProvider provider,
        JavaType type, BeanProperty prop) throws JsonMappingException
    {
        // Important: ask for TYPED serializer, in case polymorphic handling is needed!
        return provider.findTypedValueSerializer(type, true, prop);
    }
}
