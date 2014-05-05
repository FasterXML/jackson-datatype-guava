package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.google.common.base.Optional;

public class GuavaOptionalDeserializer
    extends StdDeserializer<Optional<?>>
    implements ContextualDeserializer
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _referenceType;

    protected final JsonDeserializer<?> _valueDeserializer;

    protected final TypeDeserializer _valueTypeDeserializer;

    @Deprecated // since 2.3, 
    public GuavaOptionalDeserializer(JavaType valueType) {
        this(valueType, null, null);
    }
    
    public GuavaOptionalDeserializer(JavaType valueType,
            TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser)
    {
        super(valueType);
        _referenceType = valueType.containedType(0);
        _valueTypeDeserializer = typeDeser;
        _valueDeserializer = valueDeser;
    }

    @Override
    public Optional<?> getNullValue() {
        return Optional.absent();
    }

    /**
     * Overridable fluent factory method used for creating contextual
     * instances.
     */
    public GuavaOptionalDeserializer withResolved(
            TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser)
    {
        return new GuavaOptionalDeserializer(_referenceType,
                typeDeser, valueDeser);
    }
    
    /*
    /**********************************************************
    /* Validation, post-processing
    /**********************************************************
     */

    /**
     * Method called to finalize setup of this deserializer,
     * after deserializer itself has been registered. This
     * is needed to handle recursive and transitive dependencies.
     */
    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt,
            BeanProperty property) throws JsonMappingException
    {
        JsonDeserializer<?> deser = _valueDeserializer;
        TypeDeserializer typeDeser = _valueTypeDeserializer;
        if (deser == null) {
            deser = ctxt.findContextualValueDeserializer(_referenceType, property);
        }
        if (typeDeser == null) {
            typeDeser = _findTypeDeserializer(ctxt.getConfig(), _referenceType);
        }
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }
        if (deser == _valueDeserializer && typeDeser == _valueTypeDeserializer) {
            return this;
        }
        return withResolved(typeDeser, deser);
    }

    /**
     * Method that is temporarily copied here, until 2.4 is released (at which point
     * jackson-databind has it available via DeserializationConfig)
     */
    private TypeDeserializer _findTypeDeserializer(DeserializationConfig cfg, JavaType baseType)
        throws JsonMappingException
    {
        BeanDescription bean = cfg.introspectClassAnnotations(baseType.getRawClass());
        AnnotatedClass ac = bean.getClassInfo();
        TypeResolverBuilder<?> b = cfg.getAnnotationIntrospector().findTypeResolver(cfg, ac, baseType);

        Collection<NamedType> subtypes = null;
        if (b == null) {
            b = cfg.getDefaultTyper(baseType);
            if (b == null) {
                return null;
            }
        } else {
            subtypes = cfg.getSubtypeResolver().collectAndResolveSubtypes(ac, cfg, cfg.getAnnotationIntrospector());
        }
        return b.buildTypeDeserializer(cfg, baseType, subtypes);
    }
    
    @Override
    public Optional<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException,
            JsonProcessingException
    {
        Object refd;

        if (_valueTypeDeserializer == null) {
            refd = _valueDeserializer.deserialize(jp, ctxt);
        } else {
            refd = _valueDeserializer.deserializeWithType(jp, ctxt, _valueTypeDeserializer);
        }
        return Optional.of(refd);
    }

    /* NOTE: usually should not need this method... but for some reason, it is needed here.
     */
    @Override
    public Optional<?> deserializeWithType(JsonParser jp, DeserializationContext ctxt, TypeDeserializer typeDeserializer)
        throws IOException, JsonProcessingException
    {
        final JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.VALUE_NULL) {
            return getNullValue();
        }
        // 03-Nov-2013, tatu: This gets rather tricky with "natural" types
        //   (String, Integer, Boolean), which do NOT include type information.
        //   These might actually be handled ok except that nominal type here
        //   is `Optional`, so special handling is not invoked; instead, need
        //   to do a work-around here.
        if (t != null && t.isScalarValue()) {
            return deserialize(jp, ctxt);
        }
        // with type deserializer to use here? Looks like we get passed same one?
        return Optional.of(typeDeserializer.deserializeTypedFromAny(jp, ctxt));
    }
}