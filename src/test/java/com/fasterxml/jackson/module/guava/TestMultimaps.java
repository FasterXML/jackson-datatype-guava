package com.fasterxml.jackson.module.guava;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableListMultimap.Builder;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;

/**
 * Unit tests to verify handling of various {@link Multimap}s.
 *
 * @author Steven Schlansker &lt;stevenschlansker@gmail.com&gt;
 */
public class TestMultimaps extends BaseTest {
    public void testSerializers() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        Multimap<String, String> map = LinkedHashMultimap.create();
        map.put("abc", "def");
        map.put("foo", "bar");
        map.put("abc", "xyz");
        assertEquals("{\"abc\":[\"def\",\"xyz\"],\"foo\":[\"bar\"]}", mapper.writeValueAsString(map));
    }

    public void testImmutableListMultimapDeserializer() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        Multimap<String, String> map = mapper.readValue("{\"abc\":[\"def\",\"xyz\"],\"foo\":[\"bar\"]}",
                new TypeReference<ImmutableListMultimap<String, String>>() { });

        Builder<Object, Object> expected = ImmutableListMultimap.builder();
        expected.put("abc", "def");
        expected.put("abc", "xyz");
        expected.put("foo", "bar");

        assertEquals(3, map.size());
        assertEquals(expected.build(), map);
    }

    public void testSetMultimapWithBean() throws Exception {
        ObjectMapper mapper = mapperWithModule();
        SetMultimap<Long, SimpleBean> map = HashMultimap.create();

        map.put(1L, new SimpleBean("foo", 3));
        map.put(1L, new SimpleBean("bar", 4));
        map.put(2L, new SimpleBean("baz", 3));

        assertEquals(map, mapper.readValue(mapper.writeValueAsString(map),
                new TypeReference<ImmutableSetMultimap<Long, SimpleBean>>() {}));
    }

    @JsonTypeInfo(use=Id.CLASS, include=As.PROPERTY)
    public static class Super {
        @Override
        public boolean equals(Object obj) {
            return getClass().equals(obj.getClass());
        }
    }
    public static class A extends Super {}
    public static class B extends Super {}
    public void testTypedPolymorphicWriter() throws Exception {
        Multimap<Long, Super> map = ImmutableMultimap.of(1L, new A(), 2L, new B());
        ObjectMapper mapper = mapperWithModule();
        TypeReference<Multimap<Long, Super>> type = new TypeReference<Multimap<Long, Super>>() {};
        assertEquals(map, mapper.readValue(mapper.typedWriter(type).writeValueAsString(map), type));
    }



    public static class SimpleBean {
        private String string;
        private Integer integer;
        public SimpleBean() {}
        public SimpleBean(String string, Integer integer) {
            this.string = string;
            this.integer = integer;
        }
        public String getString() {
            return string;
        }
        public void setString(String string) {
            this.string = string;
        }
        public Integer getInteger() {
            return integer;
        }
        public void setInteger(Integer integer) {
            this.integer = integer;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((integer == null) ? 0 : integer.hashCode());
            result = prime * result
                    + ((string == null) ? 0 : string.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SimpleBean other = (SimpleBean) obj;
            if (integer == null) {
                if (other.integer != null)
                    return false;
            } else if (!integer.equals(other.integer))
                return false;
            if (string == null) {
                if (other.string != null)
                    return false;
            } else if (!string.equals(other.string))
                return false;
            return true;
        }
    }
}
