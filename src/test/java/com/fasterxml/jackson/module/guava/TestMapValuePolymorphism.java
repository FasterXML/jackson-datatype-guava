package com.fasterxml.jackson.module.guava;

import java.util.Collection;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import junit.framework.TestCase;

public class TestMapValuePolymorphism extends TestCase {
    @JsonTypeInfo(include=As.PROPERTY, use=Id.CLASS)
    public class Super {}
    public class A extends Super {}
    public class B extends Super {}

    public void testValuePolymorphism() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Long, Super> map = Maps.newHashMap();
        map.put(1L, new A());
        map.put(2L, new B());
        String result = mapper.typedWriter(new TypeReference<Map<Long, Super>>() {}).writeValueAsString(map);
        assertTrue(result, result.contains("@class"));
    }
    public void testValuePolymorphismCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<Long, Collection<Super>> map = Maps.newHashMap();
        map.put(1L, Lists.<Super>newArrayList(new A()));
        String result = mapper.typedWriter(new TypeReference<Map<Long, Collection<Super>>>() {}).writeValueAsString(map);
        assertTrue(result, result.contains("@class"));
    }
}
