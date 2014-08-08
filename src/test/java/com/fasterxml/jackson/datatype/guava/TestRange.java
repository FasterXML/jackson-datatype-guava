package com.fasterxml.jackson.datatype.guava;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.deser.util.RangeFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Range;

import java.io.IOException;

/**
 * Unit tests to verify serialization of Guava {@link Range}s.
 */
public class TestRange extends ModuleTestBase {

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
    public void testSerializationWithoutModule() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Range<Integer> range = RangeFactory.closed(1, 10);
        String json = mapper.writeValueAsString(range);
        assertEquals("{\"empty\":false}", json);
    }

    public void testSerialization() throws Exception
    {
        testSerialization(MAPPER, RangeFactory.open(1, 10));
        testSerialization(MAPPER, RangeFactory.openClosed(1, 10));
        testSerialization(MAPPER, RangeFactory.closedOpen(1, 10));
        testSerialization(MAPPER, RangeFactory.closed(1, 10));
        testSerialization(MAPPER, RangeFactory.atLeast(1));
        testSerialization(MAPPER, RangeFactory.greaterThan(1));
        testSerialization(MAPPER, RangeFactory.atMost(10));
        testSerialization(MAPPER, RangeFactory.lessThan(10));
        testSerialization(MAPPER, RangeFactory.all());
        testSerialization(MAPPER, RangeFactory.singleton(1));
    }

    public void testDeserialization() throws Exception
    {
        String json = MAPPER.writeValueAsString(RangeFactory.open(1, 10));
        @SuppressWarnings("unchecked")
        Range<Integer> r = (Range<Integer>) MAPPER.readValue(json, Range.class);
        assertNotNull(r);
        assertEquals(Integer.valueOf(1), r.lowerEndpoint());
        assertEquals(Integer.valueOf(10), r.upperEndpoint());
    }
    
    private void testSerialization(ObjectMapper objectMapper, Range<?> range) throws IOException
    {
        String json = objectMapper.writeValueAsString(range);
        Range<?> rangeClone = objectMapper.readValue(json, Range.class);
        assert Objects.equal(rangeClone, range);
    }

    public void testUntyped() throws Exception
    {
        String json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(new Untyped(RangeFactory.open(1, 10)));
        Untyped out = MAPPER.readValue(json, Untyped.class);
        assertNotNull(out);
        assertEquals(Range.class, out.range.getClass());
    }
}
