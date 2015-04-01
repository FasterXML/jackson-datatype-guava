package com.fasterxml.jackson.datatype.guava.failing;

import com.fasterxml.jackson.annotation.*;

import com.fasterxml.jackson.databind.*;

import com.fasterxml.jackson.datatype.guava.ModuleTestBase;

import com.google.common.base.Optional;

/**
 * Unit test for #64.
 */
public class OptionalUnwrappedTest extends ModuleTestBase
{
    static class Child {
        public String name = "Bob";
    }

    static class Parent {
        private Child child = new Child();

        @JsonUnwrapped
        public Child getChild() { return child; }
    }

    static class OptionalParent {
        @JsonUnwrapped
        public Optional<Child> child = Optional.of(new Child());
    }

    private final ObjectMapper MAPPER = mapperWithModule();

    public void testUntyped() throws Exception
    {
        ObjectWriter w = MAPPER.writerWithDefaultPrettyPrinter();
//        String jsonExp = w.writeValueAsString(new Parent());
        String jsonExp = aposToQuotes("{\n  'name' : 'Bob'\n}");
        String jsonAct = w.writeValueAsString(new OptionalParent());
        assertEquals(jsonExp, jsonAct);
    }
}
