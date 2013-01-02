package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Before;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
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
        final Multimap<String, Boolean> map = TreeMultimap.create();
        map.put("true", Boolean.TRUE);
        map.put("false", Boolean.FALSE);
        map.put("maybe", Boolean.TRUE);
        map.put("maybe", Boolean.FALSE);

        // Test that typed writes work
        assertEquals(EXPECTED, mapper.writerWithType(new TypeReference<Multimap<String, Boolean>>() {}).writeValueAsString(map));

        // And untyped too
        final String serializedForm = mapper.writeValueAsString(map);

        assertEquals(EXPECTED, serializedForm);

        assertEquals(map, mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<TreeMultimap<String, Boolean>>() {}));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<Multimap<String, Boolean>>() {})));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<HashMultimap<String, Boolean>>() {})));
        assertEquals(map, create(mapper.<Multimap<String, Boolean>>readValue(serializedForm, new TypeReference<ImmutableMultimap<String, Boolean>>() {})));
    }

    public static enum MyEnum {
        YAY,
        BOO
    }

    public void testEnumKey() throws Exception
    {
        final TypeReference<SetMultimap<MyEnum, Integer>> type = new TypeReference<SetMultimap<MyEnum, Integer>>() {};
        final Multimap<MyEnum, Integer> map = TreeMultimap.create();

        map.put(MyEnum.YAY, 5);
        map.put(MyEnum.BOO, 2);

        final String serializedForm = mapper.writerWithType(type).writeValueAsString(map);

        assertEquals(serializedForm, mapper.writeValueAsString(map));
        assertEquals(map, mapper.readValue(serializedForm, type));
    }

    public static class Wat
    {
        private final String wat;

        @JsonCreator
        Wat(String wat)
        {
            this.wat = wat;
        }

        @JsonValue
        public String getWat()
        {
            return wat;
        }
    }

    public void testJsonValueKey() throws Exception
    {
        final TypeReference<SetMultimap<Wat, Integer>> type = new TypeReference<SetMultimap<Wat, Integer>>() {};
        final Multimap<Wat, Integer> map = HashMultimap.create();

        map.put(new Wat("3"), 5);
        map.put(new Wat("x"), 2);

        final String serializedForm = mapper.writerWithType(type).writeValueAsString(map);

        assertEquals(serializedForm, mapper.writeValueAsString(map));
        assertEquals(map, mapper.readValue(serializedForm, type));
    }
}
