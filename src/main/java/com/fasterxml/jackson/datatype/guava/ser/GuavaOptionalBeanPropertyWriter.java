package com.fasterxml.jackson.datatype.guava.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.google.common.base.Optional;

public class GuavaOptionalBeanPropertyWriter extends BeanPropertyWriter {

    protected GuavaOptionalBeanPropertyWriter(BeanPropertyWriter base) {
        super(base);
    }

    @Override
    public void serializeAsField(Object bean, JsonGenerator jgen, SerializerProvider prov) throws Exception {
        if ((get(bean) == null || Optional.absent().equals(get(bean))) && _nullSerializer == null) {
            return;
        }
        super.serializeAsField(bean, jgen, prov);
    }

}
