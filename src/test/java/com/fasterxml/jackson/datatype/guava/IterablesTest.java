package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
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

    // for [#60]
    public void testIterablesWithTransform() throws Exception
    {
        Iterable<String> input = Iterables.transform(ImmutableList.of("mr", "bo", "jangles"),
                new Function<String, String>() {
                  @Override
                  public String apply(String input) {
                    return new StringBuffer(input).reverse().toString();
                  }
                });
        String json = MAPPER.writeValueAsString(input);
        assertEquals(aposToQuotes("['rm','ob','selgnaj']"), json);
    }
}
