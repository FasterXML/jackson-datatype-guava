package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Unit tests to verify handling of various {@link Multimap}s.
 *
 * @author Steven Schlansker &lt;stevenschlansker@gmail.com&gt;
 */
public class TestMultimaps extends BaseTest
{
    /*
    /**********************************************************************
    /* Unit tests for actual registered module
    /**********************************************************************
     */

    public void testSerializers() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        Multimap<String, String> map = LinkedHashMultimap.create();
        map.put("abc", "def");
        map.put("foo", "bar");
        map.put("abc", "xyz");
        assertEquals("{\"abc\":[\"def\",\"xyz\"],\"foo\":[\"bar\"]}", mapper.writeValueAsString(map));
    }

    public void testImmutableListMultimapDeserializer() throws Exception
    {
        ObjectMapper mapper = mapperWithModule();
        Multimap<String, String> map = mapper.readValue("{\"abc\":[\"def\",\"xyz\"],\"foo\":[\"bar\"]}",
                new TypeReference<ImmutableListMultimap<String, String>>() { });

        Builder<Object, Object> expected = ImmutableListMultimap.builder();
        expected.put("abc", "def");
        expected.put("abc", "xyz");
        expected.put("foo", "bar");

        assertEquals(3, map.size());
        assertEquals(expected.build(), map);
    }
}
