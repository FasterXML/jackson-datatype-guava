package com.fasterxml.jackson.datatype.guava.ser;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.google.common.base.Optional;

import java.util.List;

public class GuavaBeanSerializerModifier extends BeanSerializerModifier {

    @Override
    public List<BeanPropertyWriter> changeProperties(SerializationConfig config,
                                                     BeanDescription beanDesc,
                                                     List<BeanPropertyWriter> beanProperties) {

        for (int i = 0; i < beanProperties.size(); ++i) {
            final BeanPropertyWriter writer = beanProperties.get(i);
            if (Optional.class.isAssignableFrom(writer.getPropertyType()))
                beanProperties.set(i, new GuavaOptionalBeanPropertyWriter(writer));
        }

        return beanProperties;
    }

}
