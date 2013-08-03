package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;

import java.io.IOException;

/**
 * @author mvolkhart
 */
public class TestMultimapsNew extends BaseTest {

    private static final String multimap =
            "{\"first\":[\"abc\",\"abc\",\"foo\"]," + "\"second\":[\"bar\"]}";

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

    private SetMultimap<String, String> setBasedHelper(TypeReference type) throws IOException {
        ObjectMapper mapper = mapperWithModule();
        SetMultimap<String, String> map = mapper.readValue(multimap, type);
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

    private ListMultimap<String, String> listBasedHelper(TypeReference type) throws IOException {
        ObjectMapper mapper = mapperWithModule();
        ListMultimap<String, String> map = mapper.readValue(multimap, type);
        assertEquals(4, map.size());
        assertTrue(map.remove("first", "abc"));
        assertTrue(map.containsEntry("first", "abc"));
        assertTrue(map.containsEntry("first", "foo"));
        assertTrue(map.containsEntry("second", "bar"));
        return map;
    }

}

