package com.fasterxml.jackson.datatype.guava.deser;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import com.google.common.base.Optional;

public class GuavaOptionalDeserializer
    extends StdDeserializer<Optional<?>>
    implements ContextualDeserializer
{
    private static final long serialVersionUID = 1L;

    protected final JavaType _fullType;
    
    protected final JavaType _referenceType;

    protected final JsonDeserializer<?> _valueDeserializer;

    protected final TypeDeserializer _valueTypeDeserializer;

    public GuavaOptionalDeserializer(JavaType fullType, JavaType refType,
            TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser)
    {
        super(fullType);
        _fullType = fullType;
        _referenceType = refType;
        _valueTypeDeserializer = typeDeser;
        _valueDeserializer = valueDeser;
    }

    @Override
    public JavaType getValueType() { return _fullType; }
    
    @Override
    public Optional<?> getNullValue() { return Optional.absent(); }

    @Deprecated // since 2.6.3; internal, remove from 2.7
    protected GuavaOptionalDeserializer withResolved(
            TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser) {
        return withResolved(_referenceType, typeDeser, valueDeser);
    }
            
    
    /**
     * Overridable fluent factory method used for creating contextual
     * instances.
     */
    protected GuavaOptionalDeserializer withResolved(JavaType refType,
            TypeDeserializer typeDeser, JsonDeserializer<?> valueDeser)
    {
        if ((refType == _referenceType)
                && (valueDeser == _valueDeserializer) && (typeDeser == _valueTypeDeserializer)) {
            return this;
        }
        return new GuavaOptionalDeserializer(_fullType, refType, typeDeser, valueDeser);
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
        JavaType refType = _referenceType;

        if (deser == null) {
            // 08-Oct-2015, tatu: As per [datatype-jdk8#13], need to use type
            //    override, if any
            if (property != null) {
                AnnotationIntrospector intr = ctxt.getAnnotationIntrospector();
                AnnotatedMember member = property.getMember();
                if ((intr != null)  && (member != null)) {
                    Class<?> cc = intr.findDeserializationContentType(member, refType);
                    if ((cc != null) && !refType.hasRawClass(cc)) {
                        // 08-Oct-2015, tatu: One open question is whether we should also
                        //   modify "full type"; seems like it's not needed quite yet
                        refType = refType.narrowBy(cc);
                    }
                }
            }
            deser = ctxt.findContextualValueDeserializer(refType, property);
        } else { // otherwise directly assigned, probably not contextual yet:
            deser = ctxt.handleSecondaryContextualization(deser, property, refType);
        }
        if (typeDeser != null) {
            typeDeser = typeDeser.forProperty(property);
        }
        return withResolved(refType, typeDeser, deser);
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
        return Optional.fromNullable(refd);
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
        return Optional.fromNullable(typeDeserializer.deserializeTypedFromAny(jp, ctxt));
    }
}