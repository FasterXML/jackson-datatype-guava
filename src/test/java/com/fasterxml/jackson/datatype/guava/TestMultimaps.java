package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.collect.*;

import java.io.IOException;
import java.util.Map;

import static com.google.common.collect.TreeMultimap.create;

/**
 * Unit tests to verify handling of various {@link Multimap}s.
 *
 * @author steven@nesscomputing.com
 */
public class TestMultimaps extends BaseTest
{
    // Test for issue #13 on github, provided by stevenschlansker
    public static enum MyEnum {
        YAY,
        BOO
    }

    // [Issue#41]
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    static class MultiMapWrapper {
        @JsonProperty
        Multimap<String, String> map = ArrayListMultimap.create();
    }

    private static final String StringStringMultimap =
            "{\"first\":[\"abc\",\"abc\",\"foo\"]," + "\"second\":[\"bar\"]}";

    private final ObjectMapper MAPPER = mapperWithModule();

    public void testMultimap() throws Exception
    {
        _testMultimap(TreeMultimap.create(), true,
                "{\"false\":[false],\"maybe\":[false,true],\"true\":[true]}");
        _testMultimap(LinkedListMultimap.create(), false,
                "{\"true\":[true],\"false\":[false],\"maybe\":[true,false]}");
        _testMultimap(LinkedHashMultimap.create(), false, null);
    }
    
    private void _testMultimap(Multimap<?,?> map0, boolean fullyOrdered, String EXPECTED) throws Exception
    {
        @SuppressWarnings("unchecked")
        Multimap<String,Boolean> map = (Multimap<String,Boolean>) map0;
        map.put("true", Boolean.TRUE);
        map.put("false", Boolean.FALSE);
        map.put("maybe", Boolean.TRUE);
        map.put("maybe", Boolean.FALSE);

        // Test that typed writes work
        if (EXPECTED != null) {
            String json =  MAPPER.writerWithType(new TypeReference<Multimap<String, Boolean>>() {}).writeValueAsString(map);
            assertEquals(EXPECTED, json);
        }

        // And untyped too
        String serializedForm = MAPPER.writeValueAsString(map);

        if (EXPECTED != null) {
            assertEquals(EXPECTED, serializedForm);
        }

        // these seem to be order-sensitive as well, so only use for ordered-maps
        if (fullyOrdered) {
            assertEquals(map, MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<TreeMultimap<String, Boolean>>() {}));
            assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<Multimap<String, Boolean>>() {})));
            assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<HashMultimap<String, Boolean>>() {})));
            assertEquals(map, create(MAPPER.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<ImmutableMultimap<String, Boolean>>() {})));
        }
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

    public void testEnumKey() throws Exception
    {
        final TypeReference<TreeMultimap<MyEnum, Integer>> type = new TypeReference<TreeMultimap<MyEnum, Integer>>() {};
        final Multimap<MyEnum, Integer> map = TreeMultimap.create();

        map.put(MyEnum.YAY, 5);
        map.put(MyEnum.BOO, 2);

        final String serializedForm = MAPPER.writerWithType(type).writeValueAsString(map);

        assertEquals(serializedForm, MAPPER.writeValueAsString(map));
        assertEquals(map, MAPPER.readValue(serializedForm, type));
    }

    // [Issue#41]
    public void testEmptyMapExclusion() throws Exception
    {
        String json = MAPPER.writeValueAsString(new MultiMapWrapper());
        assertEquals("{}", json);
    }
    
    /*
    /**********************************************************************
    /* Unit tests for set-based multimaps
    /**********************************************************************
     */
    public void testTreeMultimap() {

    }

    public void testForwardingSortedSetMultimap() {

    }

    public void testImmutableSetMultimap() {
        // TODO look at others
    }

    public void testHashMultimap() throws IOException {
        SetMultimap<String, String> map =
                setBasedHelper(new TypeReference<HashMultimap<String, String>>() {
                });
        assertTrue(map instanceof HashMultimap);
    }

    public void testLinkedHashMultimap() throws IOException {
        SetMultimap<String, String> map =
                setBasedHelper(new TypeReference<LinkedHashMultimap<String, String>>() {
                });
        assertTrue(map instanceof LinkedHashMultimap);
    }

    public void testForwardingSetMultimap() {

    }

    private SetMultimap<String, String> setBasedHelper(TypeReference<?> type)
            throws IOException
    {
        SetMultimap<String, String> map = MAPPER.readValue(StringStringMultimap, type);
        assertEquals(3, map.size());
        assertTrue(map.containsEntry("first", "abc"));
        assertTrue(map.containsEntry("first", "foo"));
        assertTrue(map.containsEntry("second", "bar"));
        return map;
    }

    /*
    /**********************************************************************
    /* Unit tests for list-based multimaps
    /**********************************************************************
     */

    public void testArrayListMultimap() throws IOException {
        ListMultimap<String, String> map =
                listBasedHelper(new TypeReference<ArrayListMultimap<String, String>>() {
                });
        assertTrue(map instanceof ArrayListMultimap);
    }

    public void testLinkedListMultimap() throws IOException {
        ListMultimap<String, String> map =
                listBasedHelper(new TypeReference<LinkedListMultimap<String, String>>() {
                });
        assertTrue(map instanceof LinkedListMultimap);
    }

    private ListMultimap<String, String> listBasedHelper(TypeReference<?> type) throws IOException {
        ListMultimap<String, String> map = MAPPER.readValue(StringStringMultimap, type);
        assertEquals(4, map.size());
        assertTrue(map.remove("first", "abc"));
        assertTrue(map.containsEntry("first", "abc"));
        assertTrue(map.containsEntry("first", "foo"));
        assertTrue(map.containsEntry("second", "bar"));
        return map;
    }
}