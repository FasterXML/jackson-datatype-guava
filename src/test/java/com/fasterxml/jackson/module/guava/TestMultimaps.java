package com.fasterxml.jackson.module.guava;

import org.junit.Before;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import static com.google.common.collect.TreeMultimap.create;

/**
 * Unit tests to verify handling of various {@link Multimap}s.
 *
 * @author steven@nesscomputing.com
 */
public class TestMultimaps extends BaseTest
{
    private static final String EXPECTED = "{\"false\":[false],\"maybe\":[false,true],\"true\":[true]}";
    ObjectMapper mapper;

    @Override
    @Before
    public void setUp()
    {
        mapper = mapperWithModule();
    }

    public void testMultimap() throws Exception
    {
        Multimap<String, Boolean> map = TreeMultimap.create();
        map.put("true", Boolean.TRUE);
        map.put("false", Boolean.FALSE);
        map.put("maybe", Boolean.TRUE);
        map.put("maybe", Boolean.FALSE);

        // Test that typed writes work
        assertEquals(EXPECTED, mapper.writerWithType(new TypeReference<Multimap<String, Boolean>>() {}).writeValueAsString(map));

        // And untyped too
        String serializedForm = mapper.writeValueAsString(map);

        assertEquals(EXPECTED, serializedForm);

        assertEquals(map, mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<TreeMultimap<String, Boolean>>() {}));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<Multimap<String, Boolean>>() {})));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<HashMultimap<String, Boolean>>() {})));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<ImmutableMultimap<String, Boolean>>() {})));
    }
}
