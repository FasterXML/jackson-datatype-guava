package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.MapSerializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import com.google.common.collect.Table;

public class TableSerializer extends ContainerSerializer<Table<?, ?, ?>> implements ContextualSerializer
{
    private final JavaType _type;
    private final TypeFactory _typeFactory;
    private final BeanProperty _property;
    private final JsonSerializer<Object> _rowSerializer;
    private final JsonSerializer<Object> _columnSerializer;
    private final TypeSerializer _valueTypeSerializer;
    private final JsonSerializer<Object> _valueSerializer;

    private final MapSerializer _rowMapSerializer;
    private final JsonSerializer<?> _columnAndValueSerializer;

    public TableSerializer( final SerializationConfig config, final JavaType type )
    {
        super(type.getRawClass(), false);
        this._type = type;
        this._typeFactory = config.getTypeFactory();
        this._property = null;
        this._rowSerializer = null;
        this._columnSerializer = null;
        this._valueTypeSerializer = null;
        this._valueSerializer = null;

        this._rowMapSerializer = null;
        this._columnAndValueSerializer = null;
    }

    @SuppressWarnings( "unchecked" )
    protected TableSerializer( final TableSerializer src,
                               final BeanProperty property,
                               final JsonSerializer<?> rowKeySerializer,
                               final JsonSerializer<?> columnKeySerializer,
                               final TypeSerializer valueTypeSerializer,
                               final JsonSerializer<?> valueSerializer )
    {
        super(src);
        this._type = src._type;
        this._typeFactory = src._typeFactory;
        this._property = property;
        this._rowSerializer = (JsonSerializer<Object>) rowKeySerializer;
        this._columnSerializer = (JsonSerializer<Object>) columnKeySerializer;
        this._valueTypeSerializer = valueTypeSerializer;
        this._valueSerializer = (JsonSerializer<Object>) valueSerializer;

        final MapType columnAndValueType = this._typeFactory.constructMapType(Map.class, this._type.containedType(1), this._type.containedType(2));
        this._columnAndValueSerializer =
                MapSerializer.construct(null,
                                        columnAndValueType,
                                        false,
                                        this._valueTypeSerializer,
                                        this._columnSerializer,
                                        this._valueSerializer,
                                        null);

        final MapType rowMapType = this._typeFactory.constructMapType(Map.class, this._type.containedType(0), columnAndValueType);
        this._rowMapSerializer =
                MapSerializer.construct(null,
                                        rowMapType,
                                        false,
                                        this._valueTypeSerializer,
                                        this._rowSerializer,
                                        (JsonSerializer<Object>) this._columnAndValueSerializer,
                                        null);

    }

    protected TableSerializer withResolved( final BeanProperty property,
                                            final JsonSerializer<?> rowKeySer,
                                            final JsonSerializer<?> columnKeySer,
                                            final TypeSerializer vts,
                                            final JsonSerializer<?> valueSer )
    {
        return new TableSerializer(this, property, rowKeySer, columnKeySer, vts, valueSer);
    }

    @Override
    protected ContainerSerializer<?> _withValueTypeSerializer( final TypeSerializer typeSer )
    {
        return new TableSerializer(this, this._property, this._rowSerializer, this._columnSerializer, typeSer, this._valueSerializer);
    }

    @Override
    public JsonSerializer<?> createContextual( final SerializerProvider provider, final BeanProperty property ) throws JsonMappingException
    {
        JsonSerializer<?> valueSer = this._valueSerializer;
        if (valueSer == null) { // if type is final, can actually resolve:
            final JavaType valueType = this._type.containedType(2);
            if (valueType.isFinal()) {
                valueSer = provider.findValueSerializer(valueType, property);
            }
        }
        else if (valueSer instanceof ContextualSerializer) {
            valueSer = ((ContextualSerializer) valueSer).createContextual(provider, property);
        }
        JsonSerializer<?> rowKeySer = this._rowSerializer;
        if (rowKeySer == null) {
            rowKeySer = provider.findKeySerializer(this._type.containedType(0), property);
        }
        else if (rowKeySer instanceof ContextualSerializer) {
            rowKeySer = ((ContextualSerializer) rowKeySer).createContextual(provider, property);
        }
        JsonSerializer<?> columnKeySer = this._columnSerializer;
        if (columnKeySer == null) {
            columnKeySer = provider.findKeySerializer(this._type.containedType(1), property);
        }
        else if (columnKeySer instanceof ContextualSerializer) {
            columnKeySer = ((ContextualSerializer) columnKeySer).createContextual(provider, property);
        }
        // finally, TypeSerializers may need contextualization as well
        TypeSerializer typeSer = this._valueTypeSerializer;
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }
        return this.withResolved(property, rowKeySer, columnKeySer, typeSer, valueSer);
    }

    @Override
    public JavaType getContentType()
    {
        return this._type.getContentType();
    }

    @Override
    public JsonSerializer<?> getContentSerializer()
    {
        return this._valueSerializer;
    }

    @Override
    public boolean isEmpty( final Table<?, ?, ?> table )
    {
        return table.isEmpty();
    }

    @Override
    public boolean hasSingleElement( final Table<?, ?, ?> table )
    {
        return table.size() == 1;
    }

    @Override
    public void serialize( final Table<?, ?, ?> value, final JsonGenerator jgen, final SerializerProvider provider )
        throws IOException, JsonGenerationException
    {
        jgen.writeStartObject();
        if ( !value.isEmpty()) {
            this.serializeFields(value, jgen, provider);
        }
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType( final Table<?, ?, ?> value,
                                   final JsonGenerator jgen,
                                   final SerializerProvider provider,
                                   final TypeSerializer typeSer ) throws IOException, JsonGenerationException
    {
        typeSer.writeTypePrefixForObject(value, jgen);
        this.serializeFields(value, jgen, provider);
        typeSer.writeTypeSuffixForObject(value, jgen);
    }

    private final void serializeFields( final Table<?, ?, ?> table, final JsonGenerator jgen, final SerializerProvider provider )
        throws IOException, JsonProcessingException
    {
        this._rowMapSerializer.serializeFields(table.rowMap(), jgen, provider);
    }
}
