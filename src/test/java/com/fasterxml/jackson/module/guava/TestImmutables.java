package com.fasterxml.jackson.module.guava;

import java.util.Iterator;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Unit tests for verifying that various immutable types
 * (like {@link ImmutableList}, {@link ImmutableMap} and {@link ImmutableSet})
 * work as expected.
 * 
 * @author tsaloranta
 */
public class TestImmutables extends BaseTest
{
    /*
    /**********************************************************************
    /* Unit tests for verifying handling in absence of module registration
    /**********************************************************************
     */
    
    /**
     * Immutable types can actually be serialized as regular collections, without
     * problems.
     */
    public void testWithoutSerializers() throws Exception
    {
        ImmutableList<Integer> list = ImmutableList.<Integer>builder()
            .add(1).add(2).add(3).build();
        ObjectMapper mapper = new ObjectMapper();
        assertEquals("[1,2,3]", mapper.writeValueAsString(list));

        ImmutableSet<String> set = ImmutableSet.<String>builder()
            .add("abc").add("def").build();
        assertEquals("[\"abc\",\"def\"]", mapper.writeValueAsString(set));

        ImmutableMap<String,Integer> map = ImmutableMap.<String,Integer>builder()
            .put("a", 1).put("b", 2).build();
        assertEquals("{\"a\":1,\"b\":2}", mapper.writeValueAsString(map));
    }

    /**
     * Deserialization will fail, however.
     */
    public void testWithoutDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();

        try {
            mapper.readValue("[1,2,3]", new TypeReference<ImmutableList<Integer>>() { });
            fail("Expected failure for missing deserializer");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find a deserializer");
        }

        try {
            mapper.readValue("[1,2,3]", new TypeReference<ImmutableSet<Integer>>() { });
            fail("Expected failure for missing deserializer");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find a deserializer");
        }

        try {
            mapper.readValue("[1,2,3]", new TypeReference<ImmutableSortedSet<Integer>>() { });
            fail("Expected failure for missing deserializer");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find a deserializer");
        }
        
        try {
            mapper.readValue("{\"a\":true,\"b\":false}", new TypeReference<ImmutableMap<Integer,Boolean>>() { });
            fail("Expected failure for missing deserializer");
        } catch (JsonMappingException e) {
            verifyException(e, "can not find a deserializer");
        }
    }
        
    /*
    /**********************************************************************
    /* Unit tests for actual registered module
    /**********************************************************************
     */

    public void testImmutableList() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        ImmutableList<Integer> list = mapper.readValue("[1,2,3]", new TypeReference<ImmutableList<Integer>>() { });
        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(1), list.get(0));
        assertEquals(Integer.valueOf(2), list.get(1));
        assertEquals(Integer.valueOf(3), list.get(2));
    }

    public void testImmutableSet() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        ImmutableSet<Integer> set = mapper.readValue("[3,7,8]", new TypeReference<ImmutableSet<Integer>>() { });
        assertEquals(3, set.size());
        Iterator<Integer> it = set.iterator();
        assertEquals(Integer.valueOf(3), it.next());
        assertEquals(Integer.valueOf(7), it.next());
        assertEquals(Integer.valueOf(8), it.next());
    }

    public void testImmutableSortedSet() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        ImmutableSortedSet<Integer> set = mapper.readValue("[5,1,2]", new TypeReference<ImmutableSortedSet<Integer>>() { });
        assertEquals(3, set.size());
        Iterator<Integer> it = set.iterator();
        assertEquals(Integer.valueOf(1), it.next());
        assertEquals(Integer.valueOf(2), it.next());
        assertEquals(Integer.valueOf(5), it.next());
    }
    
    public void testImmutableMap() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        ImmutableMap<Integer,Boolean> map = mapper.readValue("{\"12\":true,\"4\":false}", new TypeReference<ImmutableMap<Integer,Boolean>>() { });
        assertEquals(2, map.size());
        assertEquals(Boolean.TRUE, map.get(Integer.valueOf(12)));
        assertEquals(Boolean.FALSE, map.get(Integer.valueOf(4)));
    }
    
}
