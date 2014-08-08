package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;

public class IterablesTest extends ModuleTestBase
{
    private final ObjectMapper MAPPER = mapperWithModule();

    public void testIterablesSerialization() throws Exception
    {
        String json = MAPPER.writeValueAsString(Iterables.limit(Iterables.cycle(1,2,3), 3));
        assertNotNull(json);
        assertEquals("[1,2,3]", json);
    }
}
