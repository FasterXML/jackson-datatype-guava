package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.*;

import com.google.common.collect.*;

/**
 * Unit tests to verify handling of various {@link Multiset}s.
 * 
 * @author tsaloranta
 */
public class TestMultisets extends BaseTest
{

    /*
    /**********************************************************************
    /* Unit tests for verifying handling in absence of module registration
    /**********************************************************************
     */
    
    /**
     * Multi-sets can actually be serialized as regular collections, without
     * problems.
     */
    public void testWithoutSerializers() throws Exception
    {
        
        ObjectMapper mapper = new ObjectMapper();
        Multiset<String> set = LinkedHashMultiset.create();
        // hash-based multi-sets actually keeps 'same' instances together, otherwise insertion-ordered:
        set.add("abc");
        set.add("foo");
        set.add("abc");
        String json = mapper.writeValueAsString(set);
        assertEquals("[\"abc\",\"abc\",\"foo\"]", json);
    }

    public void testWithoutDeserializers() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        try {
            /*Multiset<String> set =*/ mapper.readValue("[\"abc\",\"abc\",\"foo\"]",
                    new TypeReference<Multiset<String>>() { });
        } catch (JsonMappingException e) {
            verifyException(e, "can not find a deserializer");
        }
        /*
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
        */
    }
    
    /*
    /**********************************************************************
    /* Unit tests for actual registered module
    /**********************************************************************
     */

    public void testDefault() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        Multiset<String> set = mapper.readValue("[\"abc\",\"abc\",\"foo\"]", new TypeReference<Multiset<String>>() { });
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
    }
    
    public void testLinkedHashMultiset() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        LinkedHashMultiset<String> set = mapper.readValue("[\"abc\",\"abc\",\"foo\"]", new TypeReference<LinkedHashMultiset<String>>() { });
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
    }
    
    public void testHashMultiset() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        HashMultiset<String> set = mapper.readValue("[\"abc\",\"abc\",\"foo\"]", new TypeReference<HashMultiset<String>>() { });
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
    }
    
    public void testTreeMultiset() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        TreeMultiset<String> set = mapper.readValue("[\"abc\",\"abc\",\"foo\"]", new TypeReference<TreeMultiset<String>>() { });
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
    }
    
    public void testImmutableMultiset() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        ImmutableMultiset<String> set = mapper.readValue("[\"abc\",\"abc\",\"foo\"]", new TypeReference<ImmutableMultiset<String>>() { });
        assertEquals(3, set.size());
        assertEquals(1, set.count("foo"));
        assertEquals(2, set.count("abc"));
        assertEquals(0, set.count("bar"));
    }
}
