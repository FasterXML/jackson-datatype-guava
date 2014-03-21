package com.fasterxml.jackson.datatype.guava;

import java.util.*;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import com.fasterxml.jackson.core.type.TypeReference;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Optional;

public class TestOptional extends BaseTest
{
    private final ObjectMapper MAPPER = mapperWithModule();

    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalData{
        private Optional<String> myString;
    }
    
    @JsonAutoDetect(fieldVisibility=Visibility.ANY)
    public static final class OptionalGenericData<T>{
        private Optional<T> myData;
    }

    @JsonIdentityInfo(generator=ObjectIdGenerators.IntSequenceGenerator.class)
    public static class Unit
    {
//        @JsonIdentityReference(alwaysAsId=true)
        public Optional<Unit> baseUnit;
        
        public Unit() { }
        public Unit(Optional<Unit> u) { baseUnit = u; }
        
        public void link(Unit u) {
            baseUnit = Optional.of(u);
        }
    }

    /*
    /**********************************************************************
    /* Test methods
    /**********************************************************************
     */

    public void testDeserAbsent() throws Exception {
        Optional<?> value = MAPPER.readValue("null", new TypeReference<Optional<String>>() {});
        assertFalse(value.isPresent());
    }
    
    public void testDeserSimpleString() throws Exception{
        Optional<?> value = MAPPER.readValue("\"simpleString\"", new TypeReference<Optional<String>>() {});
        assertTrue(value.isPresent());
        assertEquals("simpleString", value.get());
    }
    
    public void testDeserInsideObject() throws Exception {
        OptionalData data = MAPPER.readValue("{\"myString\":\"simpleString\"}", OptionalData.class);
        assertTrue(data.myString.isPresent());
        assertEquals("simpleString", data.myString.get());
    }
    
    public void testDeserComplexObject() throws Exception {
        TypeReference<Optional<OptionalData>> type = new TypeReference<Optional<OptionalData>>() {};
        Optional<OptionalData> data = MAPPER.readValue("{\"myString\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myString.isPresent());
        assertEquals("simpleString", data.get().myString.get());
    }
    
    public void testDeserGeneric() throws Exception {
        TypeReference<Optional<OptionalGenericData<String>>> type = new TypeReference<Optional<OptionalGenericData<String>>>() {};
        Optional<OptionalGenericData<String>> data = MAPPER.readValue("{\"myData\":\"simpleString\"}", type);
        assertTrue(data.isPresent());
        assertTrue(data.get().myData.isPresent());
        assertEquals("simpleString", data.get().myData.get());
    }
    
    public void testSerAbsent() throws Exception {
        String value = MAPPER.writeValueAsString(Optional.absent());
        assertEquals("null", value);
    }
    
    public void testSerSimpleString() throws Exception {
        String value = MAPPER.writeValueAsString(Optional.of("simpleString"));
        assertEquals("\"simpleString\"", value);
    }
    
    public void testSerInsideObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(data);
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerComplexObject() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(Optional.of(data));
        assertEquals("{\"myString\":\"simpleString\"}", value);
    }
    
    public void testSerGeneric() throws Exception {
        OptionalGenericData<String> data = new OptionalGenericData<String>();
        data.myData = Optional.of("simpleString");
        String value = MAPPER.writeValueAsString(Optional.of(data));
        assertEquals("{\"myData\":\"simpleString\"}", value);
    }

    public void testSerNonNull() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = Optional.absent();
        String value = mapperWithModule().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(data);
        assertEquals("{}", value);
    }
    
    public void testSerOptNull() throws Exception {
        OptionalData data = new OptionalData();
        data.myString = null;
        String value = mapperWithModule().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(data);
        assertEquals("{}", value);
    }
    
    public void testWithTypingEnabled() throws Exception
    {
		final ObjectMapper objectMapper = mapperWithModule();
		// ENABLE TYPING
		objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);

		final OptionalData myData = new OptionalData();
		myData.myString = Optional.fromNullable("abc");
		
		final String json = objectMapper.writeValueAsString(myData);
		
		final OptionalData deserializedMyData = objectMapper.readValue(json, OptionalData.class);
		assertEquals(myData.myString, deserializedMyData.myString);
    }

    // [Issue#17]
    public void testObjectId() throws Exception
    {
        final Unit input = new Unit();
        input.link(input);
        String json = MAPPER.writeValueAsString(input);
        Unit result = MAPPER.readValue(json,  Unit.class);
        assertNotNull(result);
        assertNotNull(result.baseUnit);
        assertTrue(result.baseUnit.isPresent());
        Unit base = result.baseUnit.get();
        assertSame(result, base);
    }

    // [Issue#37]
    public void testOptionalCollection() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new GuavaModule());

        TypeReference<List<Optional<String>>> typeReference =
            new TypeReference<List<Optional<String>>>() {};

        List<Optional<String>> list = new ArrayList<Optional<String>>();
        list.add(Optional.of("2014-1-22"));
        list.add(Optional.<String>absent());
        list.add(Optional.of("2014-1-23"));

        String str = mapper.writeValueAsString(list);
        assertEquals("[\"2014-1-22\",null,\"2014-1-23\"]", str);

        List<Optional<String>> result = mapper.readValue(str, typeReference);
        assertEquals(list.size(), result.size());
        for (int i = 0; i < list.size(); ++i) {
            assertEquals("Entry #"+i, list.get(i), result.get(i));
        }
    }
}
