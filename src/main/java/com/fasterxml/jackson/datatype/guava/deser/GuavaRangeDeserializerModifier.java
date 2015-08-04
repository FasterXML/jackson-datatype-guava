package com.fasterxml.jackson.datatype.guava.deser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.google.common.collect.BoundType;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link BeanDeserializerModifier} needed to sneak in handler to set the default
 * {@link com.google.common.collect.BoundType} when deserializing Range objects..
 */
public class GuavaRangeDeserializerModifier extends BeanDeserializerModifier {

    private final BoundType _defaultBoundType;

    public GuavaRangeDeserializerModifier(BoundType defaultBoundType) {
        checkNotNull(defaultBoundType);
        this._defaultBoundType = defaultBoundType;
    }

    @Override
    public List<BeanPropertyDefinition> updateProperties(DeserializationConfig config, BeanDescription beanDesc, List<BeanPropertyDefinition> propDefs) {
        //TODO how to add boundType attribute?
        return super.updateProperties(config, beanDesc, propDefs);
    }
}
