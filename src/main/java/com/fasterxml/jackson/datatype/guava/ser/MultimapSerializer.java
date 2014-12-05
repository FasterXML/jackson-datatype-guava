package com.fasterxml.jackson.datatype.guava.ser;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 *<p>
 * Missing features, compared to standard Java Maps:
 *<ul>
 *  <li>Inclusion for content entries (non-null, non-empty)
 *   </li>
 *  <li>Sorting of entries
 *   </li>
 * </ul>
 */
public class MultimapSerializer
    extends ContainerSerializer<Multimap<?, ?>>
    implements ContextualSerializer
{
    private final MapLikeType _type;
    private final BeanProperty _property;
    private final JsonSerializer<Object> _keySerializer;
    private final TypeSerializer _valueTypeSerializer;
    private final JsonSerializer<Object> _valueSerializer;

    /**
     * Set of entries to omit during serialization, if any
     *
     * @since 2.5
     */
    protected final Set<String> _ignoredEntries;

    /**
     * If value type can not be statically determined, mapping from
     * runtime value types to serializers are stored in this object.
     *
     * @since 2.5
     */
    protected PropertySerializerMap _dynamicValueSerializers;

    /**
     * Id of the property filter to use, if any; null if none.
     *
     * @since 2.5
     */
    protected final Object _filterId;

    /**
     * Flag set if output is forced to be sorted by keys (usually due
     * to annotation).
     *<p>
     * NOTE: not yet used.
     *
     * @since 2.5
     */
    protected final boolean _sortKeys;
    
    public MultimapSerializer(MapLikeType type, BeanDescription beanDesc,
            JsonSerializer<Object> keySerializer, TypeSerializer vts, JsonSerializer<Object> valueSerializer,
            Set<String> ignoredEntries, Object filterId)
    {
        super(type.getRawClass(), false);
        _type = type;
        _property = null;
        _keySerializer = keySerializer;
        _valueTypeSerializer = vts;
        _valueSerializer = valueSerializer;
        _ignoredEntries = ignoredEntries;
        _filterId = filterId;
        _sortKeys = false;

        _dynamicValueSerializers = PropertySerializerMap.emptyMap();
    }

    /**
     * @since 2.5
     */
    @SuppressWarnings("unchecked")
    protected MultimapSerializer(MultimapSerializer src, BeanProperty property,
                JsonSerializer<?> keySerializer, TypeSerializer vts, JsonSerializer<?> valueSerializer,
                Set<String> ignoredEntries, Object filterId, boolean sortKeys)
    {
        super(src);
        _type = src._type;
        _property = property;
        _keySerializer = (JsonSerializer<Object>) keySerializer;
        _valueTypeSerializer = vts;
        _valueSerializer = (JsonSerializer<Object>) valueSerializer;
        _dynamicValueSerializers = src._dynamicValueSerializers;
        _ignoredEntries = ignoredEntries;
        _filterId = filterId;
        _sortKeys = sortKeys;
    }

    protected MultimapSerializer withResolved(BeanProperty property,
            JsonSerializer<?> keySer, TypeSerializer vts, JsonSerializer<?> valueSer,
            Set<String> ignored, Object filterId, boolean sortKeys)
    {
        return new MultimapSerializer(this, property, keySer, vts, valueSer,
                ignored, filterId, sortKeys);
    }

    @Override
    protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSer) {
        return new MultimapSerializer(this, _property, _keySerializer,
                typeSer, _valueSerializer, _ignoredEntries, _filterId, _sortKeys);
    }

    /*
    /**********************************************************
    /* Post-processing (contextualization)
    /**********************************************************
     */

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider provider,
            BeanProperty property) throws JsonMappingException
    {
        JsonSerializer<?> valueSer = _valueSerializer;
        if (valueSer == null) { // if type is final, can actually resolve:
            JavaType valueType = _type.getContentType();
            if (valueType.isFinal()) {
                valueSer = provider.findValueSerializer(valueType, property);
            }
        } else if (valueSer instanceof ContextualSerializer) {
            valueSer = ((ContextualSerializer) valueSer).createContextual(provider, property);
        }

        final AnnotationIntrospector intr = provider.getAnnotationIntrospector();
        final AnnotatedMember propertyAcc = (property == null) ? null : property.getMember();
        JsonSerializer<?> keySer = null;
        Object filterId = _filterId;
        
        // First: if we have a property, may have property-annotation overrides
        if (propertyAcc != null && intr != null) {
            Object serDef = intr.findKeySerializer(propertyAcc);
            if (serDef != null) {
                keySer = provider.serializerInstance(propertyAcc, serDef);
            }
            serDef = intr.findContentSerializer(propertyAcc);
            if (serDef != null) {
                valueSer = provider.serializerInstance(propertyAcc, serDef);
            }
            filterId = intr.findFilterId(propertyAcc);
        }
        if (valueSer == null) {
            valueSer = _valueSerializer;
        }
        // [Issue#124]: May have a content converter
        valueSer = findConvertingContentSerializer(provider, property, valueSer);
        if (valueSer == null) {
            // One more thing -- if explicit content type is annotated,
            //   we can consider it a static case as well.
            if (hasContentTypeAnnotation(provider, property)) {
                valueSer = provider.findValueSerializer(_type.getContentType(), property);
            }
        } else {
            valueSer = provider.handleSecondaryContextualization(valueSer, property);
        }
        if (keySer == null) {
            keySer = _keySerializer;
        }
        if (keySer == null) {
            keySer = provider.findKeySerializer(_type.getKeyType(), property);
        } else {
            keySer = provider.handleSecondaryContextualization(keySer, property);
        }
        // finally, TypeSerializers may need contextualization as well
        TypeSerializer typeSer = _valueTypeSerializer;
        if (typeSer != null) {
            typeSer = typeSer.forProperty(property);
        }

        Set<String> ignored = _ignoredEntries;
        boolean sortKeys = false;
        if (intr != null && propertyAcc != null) {
            String[] moreToIgnore = intr.findPropertiesToIgnore(propertyAcc);
            if (moreToIgnore != null) {
                ignored = (ignored == null) ? new HashSet<String>() : new HashSet<String>(ignored);
                for (String str : moreToIgnore) {
                    ignored.add(str);
                }
            }
            Boolean b = intr.findSerializationSortAlphabetically(propertyAcc);
            sortKeys = (b != null) && b.booleanValue();
        }
        return withResolved(property, keySer, typeSer, valueSer,
                ignored, filterId, sortKeys);
    }

    /*
    /**********************************************************
    /* Accessors for ContainerSerializer
    /**********************************************************
     */
    
    @Override
    public JsonSerializer<?> getContentSerializer() {
        return _valueSerializer;
    }

    @Override
    public JavaType getContentType() {
        return _type.getContentType();
    }

    @Override
    public boolean hasSingleElement(Multimap<?,?> map) {
        return map.size() == 1;
    }

    @Override
    public boolean isEmpty(Multimap<?,?> map) {
        return map.isEmpty();
    }
    
    /*
    /**********************************************************
    /* Post-processing (contextualization)
    /**********************************************************
     */
    
    @Override
    public void serialize(Multimap<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException
    {
        jgen.writeStartObject();
        if (!value.isEmpty()) {
            serializeFields(value, jgen, provider);
        }        
        jgen.writeEndObject();
    }

    @Override
    public void serializeWithType(Multimap<?,?> value, JsonGenerator jgen, SerializerProvider provider,
            TypeSerializer typeSer)
        throws IOException
    {
        typeSer.writeTypePrefixForObject(value, jgen);
        serializeFields(value, jgen, provider);
        typeSer.writeTypeSuffixForObject(value, jgen);
    }

    private final void serializeFields(Multimap<?, ?> value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException
    {
        for (Entry<?, ? extends Collection<?>> e : value.asMap().entrySet()) {
            if (_keySerializer != null) {
                _keySerializer.serialize(e.getKey(), jgen, provider);
            } else {
                provider.findKeySerializer(provider.constructType(String.class), _property)
                    .serialize(e.getKey(), jgen, provider);
            }
            if (_valueSerializer != null) {
                // note: value is a List, but generic type is for contents... so:
                jgen.writeStartArray();
                for (Object vv : e.getValue()) {
                    _valueSerializer.serialize(vv, jgen, provider);
                }
                jgen.writeEndArray();
            } else {
                provider.defaultSerializeValue(Lists.newArrayList(e.getValue()), jgen);
            }
        }
    }
}
