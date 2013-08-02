package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;

import java.io.IOException;

/**
 * @author mvolkhart
 */
public class NewTestMultimaps extends BaseTest {

    private static final String multimap = "{\"map\":{\"first\":[\"abc\",\"abc\",\"foo\"]," +
            "\"second\":[\"bar\"]}}";

    public void testHashMultimap() throws IOException {
        ObjectMapper mapper = mapperWithModule();
        HashMultimap<String, String> map =
                mapper.readValue(multimap, new TypeReference<HashMultimap<String, String>>() {
                });
        assertEquals(3, map.size());
        assertTrue(map.containsEntry("first", "abc"));
        assertTrue(map.containsEntry("first", "foo"));
        assertTrue(map.containsEntry("second", "bar"));
    }

}

