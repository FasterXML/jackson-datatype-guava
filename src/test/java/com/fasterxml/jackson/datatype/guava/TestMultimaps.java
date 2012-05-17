package com.fasterxml.jackson.datatype.guava;

import java.util.Map;

import org.junit.Before;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

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
    private final ObjectMapper MAPPER =  mapperWithModule();

    public void testMultimap() throws Exception
    {
        Multimap<String, Boolean> map = TreeMultimap.create();
        map.put("true", Boolean.TRUE);
        map.put("false", Boolean.FALSE);
        map.put("maybe", Boolean.TRUE);
        map.put("maybe", Boolean.FALSE);

        // Test that typed writes work
        assertEquals(EXPECTED, MAPPER.writerWithType(new TypeReference<Multimap<String, Boolean>>() {}).writeValueAsString(map));

        // And untyped too
        String serializedForm = MAPPER.writeValueAsString(map);

        assertEquals(EXPECTED, serializedForm);

        assertEquals(map, MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<TreeMultimap<String, Boolean>>() {}));
        assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<Multimap<String, Boolean>>() {})));
        assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<HashMultimap<String, Boolean>>() {})));
        assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<ImmutableMultimap<String, Boolean>>() {})));
    }

    public void testMultimapIssue3() throws Exception
    {
        Multimap<String, String> m1 = TreeMultimap.create();
        m1.put("foo", "bar");
        m1.put("foo", "baz");
        m1.put("qux", "quux");
        ObjectMapper o = MAPPER;
        
        String t1 = o.writerWithType(new TypeReference<TreeMultimap<String, String>>(){}).writeValueAsString(m1);
        Map<?,?> javaMap = o.readValue(t1, Map.class);
        assertEquals(2, javaMap.size());
        
        String t2 = o.writerWithType(new TypeReference<Multimap<String, String>>(){}).writeValueAsString(m1);
        javaMap = o.readValue(t2, Map.class);
        assertEquals(2, javaMap.size());
        
        TreeMultimap<String, String> m2 = TreeMultimap.create();
        m2.put("foo", "bar");
        m2.put("foo", "baz");
        m2.put("qux", "quux");
        
        String t3 = o.writerWithType(new TypeReference<TreeMultimap<String, String>>(){}).writeValueAsString(m2);
        javaMap = o.readValue(t3, Map.class);
        assertEquals(2, javaMap.size());
   
        String t4 = o.writerWithType(new TypeReference<Multimap<String, String>>(){}).writeValueAsString(m2);
        javaMap = o.readValue(t4, Map.class);
        assertEquals(2, javaMap.size());
    }
}
