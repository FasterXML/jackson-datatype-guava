package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.Range;

import java.io.IOException;

/**
 * Unit tests to verify serialization of Guava {@link Range}s.
 */
public class TestRange extends BaseTest {

    private final ObjectMapper MAPPER = mapperWithModule();

    protected static class Untyped
    {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Object range;

        public Untyped() { }
        public Untyped(Range<?> r) { range = r; }
    }
    
    /**
     * This test is present so that we know if either Jackson's handling of Range
     * or Guava's implementation of Range changes.
     * @throws Exception
     */
    public void testSerializationWithoutModule() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Range<Integer> range = Range.closed(1, 10);
        String json = mapper.writeValueAsString(range);
        assertEquals("{\"empty\":false}", json);
    }

    public void testSerialization() throws Exception
    {
        testSerialization(MAPPER, Range.open(1, 10));
        testSerialization(MAPPER, Range.openClosed(1, 10));
        testSerialization(MAPPER, Range.closedOpen(1, 10));
        testSerialization(MAPPER, Range.closed(1, 10));
        testSerialization(MAPPER, Range.atLeast(1));
        testSerialization(MAPPER, Range.greaterThan(1));
        testSerialization(MAPPER, Range.atMost(10));
        testSerialization(MAPPER, Range.lessThan(10));
        testSerialization(MAPPER, Range.all());
    }

    public void testDeserialization() throws Exception
    {
        String json = MAPPER.writeValueAsString(Range.open(1, 10));
        @SuppressWarnings("unchecked")
        Range<Integer> r = (Range<Integer>) MAPPER.readValue(json, Range.class);
        assertNotNull(r);
        assertEquals(Integer.valueOf(1), r.lowerEndpoint());
        assertEquals(Integer.valueOf(10), r.upperEndpoint());
    }
    
    private void testSerialization(ObjectMapper objectMapper, Range<?> range) throws IOException {
        String json = objectMapper.writeValueAsString(range);
        Range<?> rangeClone = objectMapper.readValue(json, Range.class);
        assert Objects.equal(rangeClone, range);
    }

    public void testUntyped() throws Exception
    {
        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new Untyped(Range.open(1, 10)));
        Untyped out = MAPPER.readValue(json, Untyped.class);
        assertNotNull(out);
        assertEquals(Range.class, out.range.getClass());
    }
}
